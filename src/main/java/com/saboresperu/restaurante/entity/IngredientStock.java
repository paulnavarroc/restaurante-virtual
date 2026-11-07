package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "IngredientStocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdIngredientStock")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdIngredient", nullable = false)
    private Ingredient ingredient;

    @Column(name = "QuantityAvailable", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityAvailable;

    @Column(name = "QuantityReserved", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityReserved;

    @Column(name = "AverageCost", precision = 12, scale = 4)
    private BigDecimal averageCost;

    @Column(name = "LastCost", precision = 12, scale = 4)
    private BigDecimal lastCost;

    @Column(name = "MinStockAlert", precision = 12, scale = 4)
    private BigDecimal minStockAlert;

    @Version
    @Column(name = "Version", nullable = false)
    private Integer version;
}
