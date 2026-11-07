package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdOrder")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdCustomer", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdAddress", nullable = false)
    private CustomerAddress address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdOrderStatus", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "OrderedAt", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "Subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "DeliveryFee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "Discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "Notes", length = 500)
    private String notes;

    @Column(name = "CouponCode", length = 40)
    private String couponCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
