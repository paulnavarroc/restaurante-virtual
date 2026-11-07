package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.PromoScopes;
import com.saboresperu.restaurante.domain.PromoTypes;
import com.saboresperu.restaurante.dto.PromotionFormDto;
import com.saboresperu.restaurante.dto.PromotionRowDto;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.AdminPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminPromotionServiceImpl implements AdminPromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionProductRepository promotionProductRepository;
    private final PromotionCategoryRepository promotionCategoryRepository;
    private final PromotionAuditLogRepository promotionAuditLogRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PromotionRowDto> listAll() {
        return promotionRepository.findAllByOrderByStartsAtDesc().stream()
                .map(p -> new PromotionRowDto(
                        p.getId(),
                        p.getCode(),
                        p.getName(),
                        p.getPromoType(),
                        p.getScope(),
                        p.getStartsAt(),
                        p.getEndsAt(),
                        Boolean.TRUE.equals(p.getIsAutomatic()),
                        Boolean.TRUE.equals(p.getIsActive())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionFormDto getForm(Integer id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));

        List<Integer> productIds = promotionProductRepository.findByPromotionId(id).stream()
                .map(PromotionProduct::getProductId)
                .toList();
        List<Integer> categoryIds = promotionCategoryRepository.findByPromotionId(id).stream()
                .map(PromotionCategory::getCategoryId)
                .toList();

        return new PromotionFormDto(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getDescription(),
                p.getPromoType(),
                p.getDiscountPercent(),
                p.getDiscountAmount(),
                p.getScope(),
                p.getStartsAt(),
                p.getEndsAt(),
                p.getPriority(),
                Boolean.TRUE.equals(p.getIsStackable()),
                Boolean.TRUE.equals(p.getIsAutomatic()),
                p.getMinOrderAmount(),
                p.getMaxDiscountAmount(),
                p.getBranch() != null ? p.getBranch().getId() : null,
                Boolean.TRUE.equals(p.getIsActive()),
                productIds,
                categoryIds
        );
    }

    @Override
    @Transactional
    public Integer create(PromotionFormDto form, Integer userId) {
        validate(form);
        Promotion p = new Promotion();
        p.setCreatedAt(LocalDateTime.now());
        if (userId != null) {
            userRepository.findById(userId).ifPresent(p::setCreatedBy);
        }
        apply(p, form);
        p = promotionRepository.save(p);
        syncTargets(p.getId(), form);
        audit(p, userId, "CREATE", "Promoción creada");
        return p.getId();
    }

    @Override
    @Transactional
    public void update(Integer id, PromotionFormDto form, Integer userId) {
        validate(form);
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));
        apply(p, form);
        promotionRepository.save(p);
        syncTargets(id, form);
        audit(p, userId, "UPDATE", "Promoción actualizada");
    }

    @Override
    @Transactional
    public void setActive(Integer id, boolean active, Integer userId) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));
        p.setIsActive(active);
        promotionRepository.save(p);
        audit(p, userId, active ? "ACTIVATE" : "DEACTIVATE", null);
    }

    private void apply(Promotion p, PromotionFormDto form) {
        p.setCode(blankToNull(form.code()) != null ? form.code().trim().toUpperCase() : null);
        p.setName(form.name().trim());
        p.setDescription(blankToNull(form.description()));
        p.setPromoType(form.promoType());
        p.setDiscountPercent(form.discountPercent() != null
                ? form.discountPercent().setScale(2, RoundingMode.HALF_UP) : null);
        p.setDiscountAmount(form.discountAmount() != null
                ? form.discountAmount().setScale(2, RoundingMode.HALF_UP) : null);
        p.setScope(form.scope());
        p.setStartsAt(form.startsAt());
        p.setEndsAt(form.endsAt());
        p.setPriority(form.priority() != null ? form.priority() : 100);
        p.setIsStackable(form.stackable());
        p.setIsAutomatic(form.automatic());
        p.setMinOrderAmount(form.minOrderAmount());
        p.setMaxDiscountAmount(form.maxDiscountAmount());
        p.setIsActive(form.active());

        if (form.branchId() != null) {
            Branch b = branchRepository.findById(form.branchId())
                    .orElseThrow(() -> new IllegalArgumentException("Sede no válida"));
            p.setBranch(b);
        } else {
            p.setBranch(null);
        }
    }

    private void syncTargets(Integer promotionId, PromotionFormDto form) {
        promotionProductRepository.deleteByPromotionId(promotionId);
        promotionCategoryRepository.deleteByPromotionId(promotionId);

        if (PromoScopes.PRODUCT.equals(form.scope()) && form.productIds() != null) {
            Set<Integer> ids = new HashSet<>(form.productIds());
            for (Integer pid : ids) {
                if (!productRepository.existsById(pid)) {
                    throw new IllegalArgumentException("Producto no válido: " + pid);
                }
                promotionProductRepository.save(PromotionProduct.builder()
                        .promotionId(promotionId)
                        .productId(pid)
                        .build());
            }
        }
        if (PromoScopes.CATEGORY.equals(form.scope()) && form.categoryIds() != null) {
            Set<Integer> ids = new HashSet<>(form.categoryIds());
            for (Integer cid : ids) {
                if (!categoryRepository.existsById(cid)) {
                    throw new IllegalArgumentException("Categoría no válida: " + cid);
                }
                promotionCategoryRepository.save(PromotionCategory.builder()
                        .promotionId(promotionId)
                        .categoryId(cid)
                        .build());
            }
        }
    }

    private void validate(PromotionFormDto form) {
        if (form.endsAt().isBefore(form.startsAt())) {
            throw new IllegalArgumentException("La fecha fin debe ser posterior al inicio");
        }
        if (PromoTypes.PERCENT.equals(form.promoType())) {
            if (form.discountPercent() == null
                    || form.discountPercent().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Indica un porcentaje válido");
            }
        } else if (PromoTypes.AMOUNT.equals(form.promoType())) {
            if (form.discountAmount() == null
                    || form.discountAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Indica un monto de descuento válido");
            }
        } else {
            throw new IllegalArgumentException("Tipo de promoción no válido");
        }
        if (PromoScopes.PRODUCT.equals(form.scope())
                && (form.productIds() == null || form.productIds().isEmpty())) {
            throw new IllegalArgumentException("Selecciona al menos un producto");
        }
        if (PromoScopes.CATEGORY.equals(form.scope())
                && (form.categoryIds() == null || form.categoryIds().isEmpty())) {
            throw new IllegalArgumentException("Selecciona al menos una categoría");
        }
    }

    private void audit(Promotion p, Integer userId, String action, String detail) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        promotionAuditLogRepository.save(PromotionAuditLog.builder()
                .promotion(p)
                .user(user)
                .action(action)
                .detail(detail)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}