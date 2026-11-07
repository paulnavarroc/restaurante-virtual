package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.PaymentStatuses;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.PaymentMethodRepository;
import com.saboresperu.restaurante.repository.PaymentRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    /** Métodos que se consideran cobrados al crear el pedido (sin pasarela). */
    private static final Set<String> IMMEDIATE_PAID = Set.of("CASH");

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> listActiveMethods() {
        return paymentMethodRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Override
    @Transactional
    public Payment registerForOrder(Order order, Integer paymentMethodId, Integer registeredByUserId) {
        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no válido"));

        if (!Boolean.TRUE.equals(method.getIsActive())) {
            throw new IllegalArgumentException("Método de pago inactivo");
        }

        String status = IMMEDIATE_PAID.contains(method.getCode())
                ? PaymentStatuses.PAID
                : PaymentStatuses.PENDING;

        User registeredBy = registeredByUserId != null
                ? userRepository.findById(registeredByUserId).orElse(null)
                : null;

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .amount(order.getTotal())
                .status(status)
                .paidAt(PaymentStatuses.PAID.equals(status) ? LocalDateTime.now() : null)
                .notes(null)
                .registeredBy(registeredBy)
                .createdAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }
}