package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.PromoTypes;
import com.saboresperu.restaurante.dto.CouponFormDto;
import com.saboresperu.restaurante.dto.CouponRowDto;
import com.saboresperu.restaurante.entity.Coupon;
import com.saboresperu.restaurante.repository.CouponRepository;
import com.saboresperu.restaurante.service.interfaces.AdminCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCouponServiceImpl implements AdminCouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CouponRowDto> listAll() {
        return couponRepository.findAllByOrderByStartsAtDesc().stream()
                .map(c -> new CouponRowDto(
                        c.getId(),
                        c.getCode(),
                        c.getDescription(),
                        c.getDiscountType(),
                        c.getDiscountValue(),
                        c.getMaxUses(),
                        c.getUsedCount() != null ? c.getUsedCount() : 0,
                        c.getStartsAt(),
                        c.getEndsAt(),
                        Boolean.TRUE.equals(c.getIsActive())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CouponFormDto getForm(Integer id) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cupón no encontrado"));
        return new CouponFormDto(
                c.getId(),
                c.getCode(),
                c.getDescription(),
                c.getDiscountType(),
                c.getDiscountValue(),
                c.getMinOrderAmount(),
                c.getMaxUses(),
                c.getStartsAt(),
                c.getEndsAt(),
                Boolean.TRUE.equals(c.getIsActive())
        );
    }

    @Override
    @Transactional
    public Integer create(CouponFormDto form) {
        validate(form, null);
        Coupon c = new Coupon();
        c.setUsedCount(0);
        apply(c, form);
        return couponRepository.save(c).getId();
    }

    @Override
    @Transactional
    public void update(Integer id, CouponFormDto form) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cupón no encontrado"));
        validate(form, id);
        apply(c, form);
        couponRepository.save(c);
    }

    @Override
    @Transactional
    public void setActive(Integer id, boolean active) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cupón no encontrado"));
        c.setIsActive(active);
        couponRepository.save(c);
    }

    private void apply(Coupon c, CouponFormDto form) {
        c.setCode(form.code().trim().toUpperCase());
        c.setDescription(blankToNull(form.description()));
        c.setDiscountType(form.discountType());
        c.setDiscountValue(form.discountValue().setScale(2, RoundingMode.HALF_UP));
        c.setMinOrderAmount(form.minOrderAmount() != null
                ? form.minOrderAmount().setScale(2, RoundingMode.HALF_UP) : null);
        c.setMaxUses(form.maxUses());
        c.setStartsAt(form.startsAt());
        c.setEndsAt(form.endsAt());
        c.setIsActive(form.active());
        if (c.getUsedCount() == null) {
            c.setUsedCount(0);
        }
    }

    private void validate(CouponFormDto form, Integer currentId) {
        if (form.endsAt().isBefore(form.startsAt())) {
            throw new IllegalArgumentException("La fecha fin debe ser posterior al inicio");
        }
        if (!PromoTypes.PERCENT.equals(form.discountType())
                && !PromoTypes.AMOUNT.equals(form.discountType())) {
            throw new IllegalArgumentException("Tipo de descuento no válido");
        }
        if (PromoTypes.PERCENT.equals(form.discountType())
                && form.discountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El porcentaje no puede superar 100");
        }
        if (form.maxUses() != null && form.maxUses() < 1) {
            throw new IllegalArgumentException("Máximo de usos debe ser al menos 1");
        }

        String code = form.code().trim().toUpperCase();
        couponRepository.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new IllegalArgumentException("Ya existe un cupón con ese código");
            }
        });
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}