package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.ProductCardDto;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.BranchRepository;
import com.saboresperu.restaurante.repository.DailyMenuRepository;
import com.saboresperu.restaurante.service.interfaces.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final DailyMenuRepository dailyMenuRepository;
    private final BranchRepository branchRepository;
    private final BranchShiftResolver branchShiftResolver;

    @Override
    @Transactional(readOnly = true)
    public List<ProductCardDto> listTodayMenu(Integer branchId, Integer regionId,
                                              Integer categoryId, String q) {
        Integer resolvedBranchId = resolveBranchId(branchId);
        if (resolvedBranchId == null) {
            return List.of();
        }

        Optional<DailyMenu> menuOpt = dailyMenuRepository.findPublishedWithProducts(resolvedBranchId, LocalDate.now());

        if (menuOpt.isEmpty()) {
            return List.of();
        }

        String query = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);

        Optional<BranchShift> current = branchShiftResolver.currentShift(resolvedBranchId, LocalTime.now());

        return menuOpt.get().getItems().stream()
                .filter(i -> i.getProduct() != null)
                .filter(i -> Boolean.TRUE.equals(i.getProduct().getIsActive()))
                .filter(i -> branchShiftResolver.itemVisibleInShift(i.getShifts(), current.orElse(null)))
                .filter(i -> filterCategory(i, categoryId))
                .filter(i -> filterRegion(i, regionId))
                .filter(i -> filterQuery(i, query))
                .sorted(Comparator.comparing(i -> i.getProduct().getName(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toCard)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPublishedMenuToday(Integer branchId) {
        Integer resolvedBranchId = resolveBranchId(branchId);
        if (resolvedBranchId == null) {
            return false;
        }
        return dailyMenuRepository
                .findPublishedWithProducts(resolvedBranchId, LocalDate.now())
                .isPresent();
    }

    private Integer resolveBranchId(Integer branchId) {
        if (branchId != null) {
            return branchId;
        }
        return branchRepository.findByIsActiveTrue().stream()
                .findFirst()
                .map(Branch::getId)
                .orElse(null);
    }

    private boolean filterCategory(DailyMenuItem i, Integer categoryId) {
        if (categoryId == null) return true;
        return i.getProduct().getCategory() != null
                && categoryId.equals(i.getProduct().getCategory().getId());
    }

    private boolean filterRegion(DailyMenuItem i, Integer regionId) {
        if (regionId == null) return true;
        return i.getProduct().getRegion() != null
                && regionId.equals(i.getProduct().getRegion().getId());
    }

    private boolean filterQuery(DailyMenuItem i, String query) {
        if (query == null) return true;
        String name = i.getProduct().getName();
        return name != null && name.toLowerCase(Locale.ROOT).contains(query);
    }

    private ProductCardDto toCard(DailyMenuItem item) {
        Product p = item.getProduct();
        BigDecimal list = money(p.getBasePrice());
        BigDecimal sale = item.getPriceOverride() != null
                ? money(item.getPriceOverride())
                : list;
        boolean onPromo = sale.compareTo(list) < 0;

        boolean flagAvailable = Boolean.TRUE.equals(item.getIsAvailable());
        Integer max = item.getMaxQuota();
        int sold = item.getSoldQuantity() != null ? item.getSoldQuantity() : 0;
        Integer remaining = null;
        boolean soldOut = false;

        if (max != null) {
            remaining = Math.max(0, max - sold);
            if (remaining == 0) {
                soldOut = true;
            }
        }
        if (!flagAvailable) {
            soldOut = true;
        }

        return new ProductCardDto(
                p.getId(),
                item.getId(),
                p.getName(),
                p.getDescription(),
                list,
                sale,
                onPromo,
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getRegion() != null ? p.getRegion().getName() : null,
                p.getPrepTimeMinutes(),
                flagAvailable && !soldOut,
                soldOut,
                remaining
        );
    }

    private static BigDecimal money(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}