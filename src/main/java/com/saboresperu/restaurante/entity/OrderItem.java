package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdOrderItem")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdOrder", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdProduct", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdDailyMenuItem")
    private DailyMenuItem dailyMenuItem;

    @Column(name = "ProductNameSnapshot", nullable = false, length = 150)
    private String productNameSnapshot;

    @Column(name = "UnitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "LineDiscount", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineDiscount;

    @Column(name = "LineTotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "Notes", length = 300)
    private String notes;
}
