package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.StockMovementTypes;
import com.saboresperu.restaurante.dto.StockAdjustRequest;
import com.saboresperu.restaurante.dto.StockEntryRequest;
import com.saboresperu.restaurante.dto.StockRowDto;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.AdminStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStockServiceImpl implements AdminStockService {

    private final IngredientStockRepository ingredientStockRepository;
    private final IngredientRepository ingredientRepository;
    private final BranchRepository branchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StockRowDto> listByBranch(Integer branchId) {
        List<IngredientStock> rows = branchId != null
                ? ingredientStockRepository.findByBranchIdWithDetails(branchId)
                : ingredientStockRepository.findAllWithDetails();

        return rows.stream().map(this::toRow).toList();
    }

    @Override
    @Transactional
    public void registerEntry(StockEntryRequest request, Integer userId) {
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));
        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));

        if (Boolean.TRUE.equals(ingredient.getIsUnlimitedStock())) {
            throw new IllegalArgumentException("Este insumo no maneja stock limitado");
        }

        BigDecimal qty = scale(request.quantity());
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        IngredientStock stock = ingredientStockRepository
                .findByBranchIdAndIngredientId(branch.getId(), ingredient.getId())
                .orElseGet(() -> ingredientStockRepository.save(IngredientStock.builder()
                        .branch(branch)
                        .ingredient(ingredient)
                        .quantityAvailable(BigDecimal.ZERO)
                        .quantityReserved(BigDecimal.ZERO)
                        .minStockAlert(ingredient.getMinStockAlert())
                        .version(0)
                        .build()));

        BigDecimal available = nz(stock.getQuantityAvailable());
        BigDecimal newAvailable = scale(available.add(qty));
        stock.setQuantityAvailable(newAvailable);

        BigDecimal unitCost = request.unitCost() != null ? scale(request.unitCost()) : null;
        if (unitCost != null && unitCost.compareTo(BigDecimal.ZERO) >= 0) {
            stock.setLastCost(unitCost);
            stock.setAverageCost(recalcAverage(available, stock.getAverageCost(), qty, unitCost));
        }
        ingredientStockRepository.save(stock);

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        stockMovementRepository.save(StockMovement.builder()
                .branch(branch)
                .ingredient(ingredient)
                .movementType(StockMovementTypes.IN)
                .quantity(qty)
                .unitCost(unitCost)
                .user(user)
                .notes(blankToNull(request.notes()) != null ? request.notes() : "Entrada de stock")
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public void adjust(StockAdjustRequest request, Integer userId) {
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));
        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));

        IngredientStock stock = ingredientStockRepository
                .findByBranchIdAndIngredientId(branch.getId(), ingredient.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existe stock para ajustar"));

        BigDecimal newQty = scale(request.newAvailableQuantity());
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El disponible no puede ser negativo");
        }

        BigDecimal reserved = nz(stock.getQuantityReserved());
        // Opcional: impedir disponible < 0 respecto a lógica de negocio; reserved se mantiene
        BigDecimal oldAvailable = nz(stock.getQuantityAvailable());
        BigDecimal delta = scale(newQty.subtract(oldAvailable).abs());

        stock.setQuantityAvailable(newQty);
        ingredientStockRepository.save(stock);

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        stockMovementRepository.save(StockMovement.builder()
                .branch(branch)
                .ingredient(ingredient)
                .movementType(StockMovementTypes.ADJUST)
                .quantity(delta.compareTo(BigDecimal.ZERO) == 0 ? scale(BigDecimal.valueOf(0.0001)) : delta)
                .user(user)
                .notes(blankToNull(request.notes()) != null
                        ? request.notes()
                        : ("Ajuste: " + oldAvailable + " → " + newQty + " (reserved=" + reserved + ")"))
                .createdAt(LocalDateTime.now())
                .build());
    }

    private StockRowDto toRow(IngredientStock s) {
        BigDecimal available = nz(s.getQuantityAvailable());
        BigDecimal alert = s.getMinStockAlert() != null
                ? s.getMinStockAlert()
                : (s.getIngredient() != null ? s.getIngredient().getMinStockAlert() : null);
        boolean low = alert != null && available.compareTo(alert) <= 0;

        return new StockRowDto(
                s.getId(),
                s.getBranch().getId(),
                s.getBranch().getName(),
                s.getIngredient().getId(),
                s.getIngredient().getName(),
                s.getIngredient().getUnit(),
                available,
                nz(s.getQuantityReserved()),
                s.getAverageCost(),
                s.getLastCost(),
                alert,
                low
        );
    }

    private BigDecimal recalcAverage(BigDecimal oldQty, BigDecimal oldAvg,
                                     BigDecimal inQty, BigDecimal unitCost) {
        BigDecimal oq = nz(oldQty);
        BigDecimal oa = oldAvg != null ? oldAvg : unitCost;
        if (oq.compareTo(BigDecimal.ZERO) <= 0) {
            return scale(unitCost);
        }
        BigDecimal totalCost = oq.multiply(oa).add(inQty.multiply(unitCost));
        BigDecimal totalQty = oq.add(inQty);
        if (totalQty.compareTo(BigDecimal.ZERO) == 0) {
            return scale(unitCost);
        }
        return totalCost.divide(totalQty, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(4, RoundingMode.HALF_UP);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}