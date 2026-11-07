package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.Order;
import com.saboresperu.restaurante.entity.Payment;
import com.saboresperu.restaurante.entity.PaymentMethod;

import java.util.List;

public interface PaymentService {
    List<PaymentMethod> listActiveMethods();

    Payment registerForOrder(Order order, Integer paymentMethodId, Integer registeredByUserId);
}