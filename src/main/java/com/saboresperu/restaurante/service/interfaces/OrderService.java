package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CartDto;
import com.saboresperu.restaurante.dto.CheckoutPreviewDto;
import com.saboresperu.restaurante.dto.CheckoutRequestDto;
import com.saboresperu.restaurante.entity.Order;

import java.util.List;

public interface OrderService {
    CheckoutPreviewDto preview(Integer customerId, Integer addressId, String couponCode, CartDto cart);
    Order placeOrder(Integer customerId, CheckoutRequestDto request, CartDto cart, Integer userId);
    void cancelOrder(Integer orderId, Integer customerId, String reason);
    List<Order> listByCustomerId(Integer customerId);
    Order getByIdAndCustomerId(Integer orderId, Integer customerId);
}