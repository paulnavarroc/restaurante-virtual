package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPayment")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdOrder", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdPaymentMethod", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "Amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "ExternalReference", length = 120)
    private String externalReference;

    @Column(name = "PaidAt")
    private LocalDateTime paidAt;

    @Column(name = "Notes", length = 300)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRegisteredByUser")
    private User registeredBy;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
