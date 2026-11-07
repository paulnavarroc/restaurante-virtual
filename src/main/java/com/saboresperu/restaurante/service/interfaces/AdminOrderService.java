package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.AdminOrderDetailDto;
import com.saboresperu.restaurante.dto.AdminOrderRowDto;

import java.util.List;

public interface AdminOrderService {
    List<AdminOrderRowDto> search(String statusCode, Integer branchId, int limit);
    AdminOrderDetailDto getDetail(Integer orderId);
    void updateStatus(Integer orderId, String newStatusCode, Integer userId);
}