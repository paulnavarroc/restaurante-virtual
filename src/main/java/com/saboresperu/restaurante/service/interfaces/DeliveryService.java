package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.Delivery;
import com.saboresperu.restaurante.entity.Order;

import java.util.List;

public interface DeliveryService {
    void ensurePendingDelivery(Order order);
    List<Delivery> listPendingForBranch(Integer branchId);
    List<Delivery> listActiveForCourier(Integer courierId);
    Delivery claim(Integer deliveryId, Integer courierId, Integer branchId);
    Delivery startRoute(Integer deliveryId, Integer courierId);
    Delivery markDelivered(Integer deliveryId, Integer courierId);
    Delivery markFailed(Integer deliveryId, Integer courierId, String notes);
}