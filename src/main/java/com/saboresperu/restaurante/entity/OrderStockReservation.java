package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "OrderStockReservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStockReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdReservation")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdOrder", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdIngredient", nullable = false)
    private Ingredient ingredient;

    @Column(name = "Quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}