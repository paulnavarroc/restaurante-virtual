package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.Order;

public interface StockService {
    void reserveForOrder(Order order);
    void consumeReservations(Order order);
    void releaseReservations(Order order);
}