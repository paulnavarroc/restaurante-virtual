package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "StockMovements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdStockMovement")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdIngredient", nullable = false)
    private Ingredient ingredient;

    @Column(name = "MovementType", nullable = false, length = 20)
    private String movementType;

    @Column(name = "Quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(name = "UnitCost", precision = 12, scale = 4)
    private BigDecimal unitCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdOrder")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser")
    private User user;

    @Column(name = "Notes", length = 400)
    private String notes;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
