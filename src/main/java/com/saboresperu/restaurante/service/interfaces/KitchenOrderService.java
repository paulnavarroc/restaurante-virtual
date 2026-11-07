package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.Order;

import java.util.List;

public interface KitchenOrderService {
    List<Order> listActiveForBranch(Integer branchId);
    void updateStatus(Integer orderId, Integer branchId, String newStatusCode, Integer userId);
}