package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.StockMovementTypes;
import com.saboresperu.restaurante.domain.StockReservationStatuses;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final RecipeRepository recipeRepository;
    private final IngredientStockRepository ingredientStockRepository;
    private final OrderStockReservationRepository reservationRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public void reserveForOrder(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        Integer branchId = order.getBranch().getId();
        Map<Integer, BigDecimal> neededByIngredient = new HashMap<>();

        for (OrderItem line : order.getItems()) {
            Product product = line.getProduct();
            if (product == null || Boolean.TRUE.equals(product.getIsUnlimitedStock())) {
                continue;
            }

            Recipe recipe = recipeRepository.findActiveByProductIdWithItems(product.getId())
                    .orElse(null);
            if (recipe == null || recipe.getItems() == null || recipe.getItems().isEmpty()) {
                // Sin receta: no se descuenta stock de insumos
                continue;
            }

            BigDecimal qtyOrdered = BigDecimal.valueOf(line.getQuantity());
            for (RecipeItem ri : recipe.getItems()) {
                Ingredient ingredient = ri.getIngredient();
                if (ingredient == null || Boolean.TRUE.equals(ingredient.getIsUnlimitedStock())) {
                    continue;
                }
                BigDecimal need = ri.getQuantity().multiply(qtyOrdered);
                neededByIngredient.merge(ingredient.getId(), need, BigDecimal::add);
            }
        }

        for (Map.Entry<Integer, BigDecimal> e : neededByIngredient.entrySet()) {
            Integer ingredientId = e.getKey();
            BigDecimal need = scale(e.getValue());

            IngredientStock stock = ingredientStockRepository
                    .findByBranchIdAndIngredientId(branchId, ingredientId)
                    .orElseThrow(() -> new IllegalArgumentException("No hay registro de stock para el insumo id=" + ingredientId + " en la sede del pedido"));

            BigDecimal available = stock.getQuantityAvailable() != null ? stock.getQuantityAvailable() : BigDecimal.ZERO;

            if (available.compareTo(need) < 0) {
                String name = stock.getIngredient() != null
                        ? stock.getIngredient().getName()
                        : String.valueOf(ingredientId);
                throw new IllegalArgumentException("Stock insuficiente de \"" + name + "\". Disponible: " + available + ", requerido: " + need);
            }

            stock.setQuantityAvailable(scale(available.subtract(need)));
            BigDecimal reserved = stock.getQuantityReserved() != null
                    ? stock.getQuantityReserved() : BigDecimal.ZERO;
            stock.setQuantityReserved(scale(reserved.add(need)));
            ingredientStockRepository.save(stock);

            Ingredient ingredientRef = stock.getIngredient();
            reservationRepository.save(OrderStockReservation.builder()
                    .order(order)
                    .branch(order.getBranch())
                    .ingredient(ingredientRef)
                    .quantity(need)
                    .status(StockReservationStatuses.RESERVED)
                    .createdAt(LocalDateTime.now())
                    .build());

            stockMovementRepository.save(StockMovement.builder()
                    .branch(order.getBranch())
                    .ingredient(ingredientRef)
                    .movementType(StockMovementTypes.RESERVE)
                    .quantity(need)
                    .order(order)
                    .notes("Reserva pedido #" + order.getId())
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    @Override
    @Transactional
    public void consumeReservations(Order order) {
        List<OrderStockReservation> list =
                reservationRepository.findByOrderIdAndStatus(order.getId(), StockReservationStatuses.RESERVED);

        for (OrderStockReservation res : list) {
            IngredientStock stock = ingredientStockRepository
                    .findByBranchIdAndIngredientId(res.getBranch().getId(), res.getIngredient().getId())
                    .orElseThrow(() -> new IllegalStateException("Stock no encontrado al consumir reserva"));

            BigDecimal reserved = stock.getQuantityReserved() != null
                    ? stock.getQuantityReserved() : BigDecimal.ZERO;
            if (reserved.compareTo(res.getQuantity()) < 0) {
                throw new IllegalStateException("Reserva inconsistente al consumir");
            }
            stock.setQuantityReserved(scale(reserved.subtract(res.getQuantity())));
            ingredientStockRepository.save(stock);

            res.setStatus(StockReservationStatuses.CONSUMED);
            reservationRepository.save(res);

            stockMovementRepository.save(StockMovement.builder()
                    .branch(res.getBranch())
                    .ingredient(res.getIngredient())
                    .movementType(StockMovementTypes.CONSUME)
                    .quantity(res.getQuantity())
                    .order(order)
                    .notes("Consumo pedido #" + order.getId())
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    @Override
    @Transactional
    public void releaseReservations(Order order) {
        List<OrderStockReservation> list =
                reservationRepository.findByOrderIdAndStatus(order.getId(), StockReservationStatuses.RESERVED);

        for (OrderStockReservation res : list) {
            IngredientStock stock = ingredientStockRepository
                    .findByBranchIdAndIngredientId(res.getBranch().getId(), res.getIngredient().getId())
                    .orElseThrow(() -> new IllegalStateException("Stock no encontrado al liberar reserva"));

            BigDecimal reserved = stock.getQuantityReserved() != null
                    ? stock.getQuantityReserved() : BigDecimal.ZERO;
            BigDecimal available = stock.getQuantityAvailable() != null
                    ? stock.getQuantityAvailable() : BigDecimal.ZERO;

            if (reserved.compareTo(res.getQuantity()) < 0) {
                throw new IllegalStateException("Reserva inconsistente al liberar");
            }

            stock.setQuantityReserved(scale(reserved.subtract(res.getQuantity())));
            stock.setQuantityAvailable(scale(available.add(res.getQuantity())));
            ingredientStockRepository.save(stock);

            res.setStatus(StockReservationStatuses.RELEASED);
            reservationRepository.save(res);

            stockMovementRepository.save(StockMovement.builder()
                    .branch(res.getBranch())
                    .ingredient(res.getIngredient())
                    .movementType(StockMovementTypes.RELEASE)
                    .quantity(res.getQuantity())
                    .order(order)
                    .notes("Liberación pedido #" + order.getId())
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(4, RoundingMode.HALF_UP);
    }
}