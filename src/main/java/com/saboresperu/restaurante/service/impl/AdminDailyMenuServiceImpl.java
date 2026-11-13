package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.DailyMenuStatuses;
import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.AdminDailyMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDailyMenuServiceImpl implements AdminDailyMenuService {

    private final DailyMenuRepository dailyMenuRepository;
    private final DailyMenuItemRepository dailyMenuItemRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BranchShiftRepository branchShiftRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DailyMenuRowDto> listAll() {
        return dailyMenuRepository.findAllWithBranch().stream()
                .map(m -> new DailyMenuRowDto(
                        m.getId(),
                        m.getBranch().getName(),
                        m.getMenuDate(),
                        m.getStatus(),
                        m.getItems() != null ? m.getItems().size() : 0
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMenuDetailDto getDetail(Integer id) {
        DailyMenu m = dailyMenuRepository.findByIdWithItemsAndProduct(id)
                .orElseThrow(() -> new IllegalArgumentException("Menú no encontrado"));
        List<DailyMenuItemRowDto> items = m.getItems() == null ? List.of()
                : m.getItems().stream().map(this::toItemRow).toList();
        return new DailyMenuDetailDto(
                m.getId(),
                m.getBranch().getId(),
                m.getBranch().getName(),
                m.getMenuDate(),
                m.getStatus(),
                items
        );
    }

    @Override
    @Transactional
    public Integer createDraft(DailyMenuCreateRequest request) {
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));

        dailyMenuRepository.findByBranchIdAndMenuDate(branch.getId(), request.menuDate())
                .ifPresent(m -> {
                    throw new IllegalArgumentException("Ya existe un menú para esa sede y fecha");
                });

        DailyMenu menu = DailyMenu.builder()
                .branch(branch)
                .menuDate(request.menuDate())
                .status(DailyMenuStatuses.DRAFT)
                .items(new ArrayList<>())
                .build();
        return dailyMenuRepository.save(menu).getId();
    }

    @Override
    @Transactional
    public void addItem(Integer dailyMenuId, DailyMenuItemRequest request) {
        DailyMenu menu = requireEditable(dailyMenuId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("El producto no está activo");
        }

        boolean exists = menu.getItems() != null && menu.getItems().stream()
                .anyMatch(i -> i.getProduct().getId().equals(product.getId()));
        if (exists) {
            throw new IllegalArgumentException("El producto ya está en el menú");
        }

        DailyMenuItem item = DailyMenuItem.builder()
                .dailyMenu(menu)
                .product(product)
                .isAvailable(request.available())
                .priceOverride(request.priceOverride() != null
                        ? money(request.priceOverride()) : null)
                .maxQuota(request.maxQuota())
                .soldQuantity(0)
                .build();
        dailyMenuItemRepository.save(item);
    }

    @Override
    @Transactional
    public void updateItem(Integer dailyMenuId, Integer itemId, DailyMenuItemRequest request) {
        DailyMenu menu = requireEditable(dailyMenuId);
        DailyMenuItem item = menu.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ítem no encontrado"));

        item.setIsAvailable(request.available());
        item.setPriceOverride(request.priceOverride() != null
                ? money(request.priceOverride()) : null);
        item.setMaxQuota(request.maxQuota());
        dailyMenuItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeItem(Integer dailyMenuId, Integer itemId) {
        DailyMenu menu = requireEditable(dailyMenuId);
        DailyMenuItem item = menu.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ítem no encontrado"));
        menu.getItems().remove(item);
        dailyMenuItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void publish(Integer dailyMenuId, Integer userId) {
        DailyMenu menu = dailyMenuRepository.findByIdWithItemsAndProduct(dailyMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Menú no encontrado"));

        if (DailyMenuStatuses.PUBLISHED.equals(menu.getStatus())) {
            throw new IllegalArgumentException("El menú ya está publicado");
        }
        // Permitir publicar desde DRAFT o desde CLOSED (reapertura + publicación)
        if (!DailyMenuStatuses.DRAFT.equals(menu.getStatus())
                && !DailyMenuStatuses.CLOSED.equals(menu.getStatus())) {
            throw new IllegalArgumentException("Estado no válido para publicar: " + menu.getStatus());
        }

        LocalDate today = LocalDate.now();
        if (menu.getMenuDate() == null) {
            throw new IllegalArgumentException("El menú no tiene fecha");
        }
        if (menu.getMenuDate().isBefore(today)) {
            throw new IllegalArgumentException(
                    "No se puede publicar un menú con fecha pasada (" + menu.getMenuDate() + ")");
        }
        // para solo limitar a publicar el día actual, descomentar
        // if (!menu.getMenuDate().equals(today)) {
        //     throw new IllegalArgumentException("Solo se puede publicar el menú de hoy");
        // }

        if (menu.getItems() == null || menu.getItems().isEmpty()) {
            throw new IllegalArgumentException("No puedes publicar un menú sin productos");
        }

        // Opcional: un solo PUBLISHED “activo” por sede para HOY
        // (otros menús de la misma sede y misma fecha no deberían existir por unique)
        if (menu.getMenuDate().equals(today)) {
            dailyMenuRepository
                    .findPublishedWithItems(menu.getBranch().getId(), today, DailyMenuStatuses.PUBLISHED)
                    .ifPresent(other -> {
                        if (!other.getId().equals(menu.getId())) {
                            other.setStatus(DailyMenuStatuses.CLOSED);
                            dailyMenuRepository.save(other);
                        }
                    });
        }

        menu.setStatus(DailyMenuStatuses.PUBLISHED);
        menu.setPublishedAt(LocalDateTime.now());
        if (userId != null) {
            userRepository.findById(userId).ifPresent(menu::setPublishedBy);
        }
        dailyMenuRepository.save(menu);
    }

    @Override
    @Transactional
    public void unpublish(Integer dailyMenuId) {
        DailyMenu menu = dailyMenuRepository.findById(dailyMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Menú no encontrado"));
        if (!DailyMenuStatuses.PUBLISHED.equals(menu.getStatus())) {
            throw new IllegalArgumentException("Solo se puede despublicar un menú publicado");
        }
        menu.setStatus(DailyMenuStatuses.DRAFT);
        dailyMenuRepository.save(menu);
    }

    @Override
    @Transactional
    public void reopen(Integer dailyMenuId) {
        DailyMenu menu = dailyMenuRepository.findById(dailyMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Menú no encontrado"));
        if (!DailyMenuStatuses.CLOSED.equals(menu.getStatus())) {
            throw new IllegalArgumentException("Solo se puede reabrir un menú cerrado");
        }
        menu.setStatus(DailyMenuStatuses.DRAFT);
        dailyMenuRepository.save(menu);
    }

    @Override
    @Transactional
    public void close(Integer dailyMenuId) {
        DailyMenu menu = dailyMenuRepository.findById(dailyMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Menú no encontrado"));
        if (DailyMenuStatuses.CLOSED.equals(menu.getStatus())) {
            throw new IllegalArgumentException("El menú ya está cerrado");
        }
        menu.setStatus(DailyMenuStatuses.CLOSED);
        dailyMenuRepository.save(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchShiftDto> listBranchShifts(Integer branchId) {
        return branchShiftRepository.findByBranchIdAndIsActiveTrue(branchId).stream()
                .map(s -> new BranchShiftDto(
                        s.getId(),
                        s.getShift(),
                        shiftLabel(s.getShift()),
                        s.getStartTime(),
                        s.getEndTime(),
                        Boolean.TRUE.equals(s.getIsActive())
                ))
                .toList();
    }

    @Override
    @Transactional
    public void setItemShifts(Integer dailyMenuId, Integer itemId, List<Integer> branchShiftIds) {
        DailyMenu menu = requireEditable(dailyMenuId);
        DailyMenuItem item = menu.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ítem no encontrado"));

        // validar que los shifts sean de la misma sede
        Integer branchId = menu.getBranch().getId();
        Set<BranchShift> selected = new HashSet<>();
        if (branchShiftIds != null) {
            for (Integer sid : branchShiftIds) {
                BranchShift shift = branchShiftRepository.findById(sid)
                        .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));
                if (!shift.getBranch().getId().equals(branchId)) {
                    throw new IllegalArgumentException("Turno de otra sede");
                }
                selected.add(shift);
            }
        }
        item.getShifts().clear();
        item.getShifts().addAll(selected);
        dailyMenuItemRepository.save(item);
    }

    @Override
    @Transactional
    public Integer copyToDate(Integer sourceMenuId, java.time.LocalDate targetDate) {
        if (targetDate == null) {
            throw new IllegalArgumentException("Indica la fecha destino");
        }

        DailyMenu source = dailyMenuRepository.findByIdWithItemsAndProduct(sourceMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Menú origen no encontrado"));

        Integer branchId = source.getBranch().getId();

        dailyMenuRepository.findByBranchIdAndMenuDate(branchId, targetDate)
                .ifPresent(m -> {
                    throw new IllegalArgumentException(
                            "Ya existe un menú para esa sede el " + targetDate);
                });

        DailyMenu target = DailyMenu.builder()
                .branch(source.getBranch())
                .menuDate(targetDate)
                .status(DailyMenuStatuses.DRAFT)
                .items(new ArrayList<>())
                .build();
        target = dailyMenuRepository.save(target);

        if (source.getItems() == null || source.getItems().isEmpty()) {
            return target.getId();
        }

        // Turnos de la sede (para remapear por código de turno)
        List<BranchShift> branchShifts = branchShiftRepository.findByBranchIdAndIsActiveTrue(branchId);

        for (DailyMenuItem srcItem : source.getItems()) {
            // Forzar carga de producto / turnos dentro de la transacción
            Product product = srcItem.getProduct();
            if (product == null || !Boolean.TRUE.equals(product.getIsActive())) {
                continue;
            }

            DailyMenuItem copy = DailyMenuItem.builder()
                    .dailyMenu(target)
                    .product(product)
                    .isAvailable(Boolean.TRUE.equals(srcItem.getIsAvailable()))
                    .priceOverride(srcItem.getPriceOverride())
                    .maxQuota(srcItem.getMaxQuota())
                    .soldQuantity(0) // siempre 0 en el día nuevo
                    .shifts(new HashSet<>())
                    .build();

            if (srcItem.getShifts() != null && !srcItem.getShifts().isEmpty()) {
                Set<String> codes = srcItem.getShifts().stream()
                        .map(BranchShift::getShift)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                for (BranchShift bs : branchShifts) {
                    if (codes.contains(bs.getShift())) {
                        copy.getShifts().add(bs);
                    }
                }
            }

            dailyMenuItemRepository.save(copy);
            target.getItems().add(copy);
        }

        return target.getId();
    }

    private static String shiftLabel(String code) {
        return switch (code == null ? "" : code) {
            case "BREAKFAST" -> "Desayuno";
            case "LUNCH" -> "Almuerzo";
            case "DINNER" -> "Cena";
            default -> code;
        };
    }


    private DailyMenu requireEditable(Integer id) {
        DailyMenu menu = dailyMenuRepository.findByIdWithItemsAndProduct(id)
                .orElseThrow(() -> new IllegalArgumentException("Menú no encontrado"));
        if (DailyMenuStatuses.CLOSED.equals(menu.getStatus())) {
            throw new IllegalArgumentException("El menú cerrado no se puede editar");
        }
        return menu;
    }

    private DailyMenuItemRowDto toItemRow(DailyMenuItem i) {
        BigDecimal base = i.getProduct().getBasePrice();
        BigDecimal effective = i.getPriceOverride() != null ? i.getPriceOverride() : base;

        List<String> labels = i.getShifts() == null ? List.of()
                : i.getShifts().stream()
                .map(s -> shiftLabel(s.getShift()))
                .sorted()
                .toList();

        return new DailyMenuItemRowDto(
                i.getId(),
                i.getProduct().getId(),
                i.getProduct().getName(),
                base,
                i.getPriceOverride(),
                effective,
                i.getMaxQuota(),
                i.getSoldQuantity() != null ? i.getSoldQuantity() : 0,
                Boolean.TRUE.equals(i.getIsAvailable()),
                labels
        );
    }

    private static BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}