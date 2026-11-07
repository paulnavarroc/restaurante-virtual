package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CouponFormDto;
import com.saboresperu.restaurante.dto.CouponRowDto;

import java.util.List;

public interface AdminCouponService {
    List<CouponRowDto> listAll();
    CouponFormDto getForm(Integer id);
    Integer create(CouponFormDto form);
    void update(Integer id, CouponFormDto form);
    void setActive(Integer id, boolean active);
}