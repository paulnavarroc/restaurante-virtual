package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.BranchOfferDto;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final DailyMenuRepository dailyMenuRepository;
    private final BranchRepository branchRepository;
    private final BranchShiftResolver branchShiftResolver;

    @Override
    @Transactional(readOnly = true)
    public List<ProductCardDto> listTodayMenu(Integer branchId, Integer regionId, Integer categoryId, String q) {
        String query = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);

        if (branchId != null) {
            return listForSingleBranch(branchId, regionId, categoryId, query);
        }
        return listAggregatedAllBranches(regionId, categoryId, query);
    }

    private List<ProductCardDto> listForSingleBranch(Integer branchId, Integer regionId, Integer categoryId, String query) {

        Optional<DailyMenu> menuOpt = dailyMenuRepository.findPublishedWithProducts(branchId, LocalDate.now());
        if (menuOpt.isEmpty()) return List.of();

        Optional<BranchShift> current = branchShiftResolver.currentShift(branchId, LocalTime.now());

        return menuOpt.get().getItems().stream()
                .filter(i -> i.getProduct() != null)
                .filter(i -> Boolean.TRUE.equals(i.getProduct().getIsActive()))
                .filter(i -> branchShiftResolver.itemVisibleInShift(i.getShifts(), current.orElse(null)))
                .filter(i -> filterCategory(i, categoryId))
                .filter(i -> filterRegion(i, regionId))
                .filter(i -> filterQuery(i, query))
                .sorted(Comparator.comparing(i -> i.getProduct().getName(), String.CASE_INSENSITIVE_ORDER))
                .map(i -> toCard(i, menuOpt.get().getBranch()))
                .toList();
    }

    private List<ProductCardDto> listAggregatedAllBranches(Integer regionId, Integer categoryId, String query) {

        List<DailyMenu> menus = dailyMenuRepository.findAllPublishedWithProducts(LocalDate.now());
        if (menus.isEmpty()) return List.of();

        // Clave: productId + salePrice
        record Key(Integer productId, String priceKey) {}
        Map<Key, List<ItemWithBranch>> groups = new LinkedHashMap<>();

        for (DailyMenu menu : menus) {
            Branch branch = menu.getBranch();
            Optional<BranchShift> current = branchShiftResolver.currentShift(branch.getId(), LocalTime.now());

            for (DailyMenuItem item : menu.getItems()) {
                if (item.getProduct() == null || !Boolean.TRUE.equals(item.getProduct().getIsActive())) {
                    continue;
                }
                if (!branchShiftResolver.itemVisibleInShift(item.getShifts(), current.orElse(null))) {
                    continue;
                }
                if (!filterCategory(item, categoryId) || !filterRegion(item, regionId) || !filterQuery(item, query)) {
                    continue;
                }
                BigDecimal sale = effectiveSale(item);
                Key key = new Key(item.getProduct().getId(), sale.toPlainString());
                groups.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new ItemWithBranch(item, branch));
            }
        }

        List<ProductCardDto> result = new ArrayList<>();
        for (List<ItemWithBranch> group : groups.values()) {
            result.add(toAggregatedCard(group));
        }
        result.sort(Comparator.comparing(ProductCardDto::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private record ItemWithBranch(DailyMenuItem item, Branch branch) {}

    private ProductCardDto toAggregatedCard(List<ItemWithBranch> group) {
        DailyMenuItem first = group.get(0).item();
        Product p = first.getProduct();
        BigDecimal list = money(p.getBasePrice());
        BigDecimal sale = effectiveSale(first);
        boolean onPromo = sale.compareTo(list) < 0;

        List<BranchOfferDto> offers = new ArrayList<>();
        boolean anyAvailable = false;
        boolean allSoldOut = true;
        Integer minRemaining = null;

        for (ItemWithBranch ib : group) {
            DailyMenuItem item = ib.item();
            boolean flagAvailable = Boolean.TRUE.equals(item.getIsAvailable());
            Integer max = item.getMaxQuota();
            int sold = item.getSoldQuantity() != null ? item.getSoldQuantity() : 0;
            Integer remaining = max != null ? Math.max(0, max - sold) : null;
            boolean soldOut = !flagAvailable || (remaining != null && remaining == 0);
            if (!soldOut) {
                anyAvailable = true;
                allSoldOut = false;
            }
            if (remaining != null) {
                minRemaining = minRemaining == null ? remaining : Math.min(minRemaining, remaining);
            }
            offers.add(new BranchOfferDto(
                    ib.branch().getId(),
                    ib.branch().getName(),
                    item.getId(),
                    remaining,
                    !soldOut
            ));
        }

        Integer singleDmi = offers.size() == 1 ? offers.get(0).dailyMenuItemId() : null;

        return new ProductCardDto(
                p.getId(),
                singleDmi,
                p.getName(),
                p.getDescription(),
                list,
                sale,
                onPromo,
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getRegion() != null ? p.getRegion().getName() : null,
                p.getPrepTimeMinutes(),
                anyAvailable,
                allSoldOut,
                minRemaining,
                offers
        );
    }

    private BigDecimal effectiveSale(DailyMenuItem item) {
        Product p = item.getProduct();
        BigDecimal list = money(p.getBasePrice());
        return item.getPriceOverride() != null ? money(item.getPriceOverride()) : list;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPublishedMenuToday(Integer branchId) {
        if (branchId != null) {
            return dailyMenuRepository.findPublishedWithProducts(branchId, LocalDate.now()).isPresent();
        }
        return !dailyMenuRepository.findAllPublishedWithProducts(LocalDate.now()).isEmpty();
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

    private ProductCardDto toCard(DailyMenuItem item, Branch branch) {
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

        List<BranchOfferDto> branches = List.of();
        if (branch != null) {
            branches = List.of(new BranchOfferDto(
                    branch.getId(),
                    branch.getName(),
                    item.getId(),
                    remaining,
                    flagAvailable && !soldOut
            ));
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
                remaining,
                branches
        );
    }

    private static BigDecimal money(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}