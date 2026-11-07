package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdIngredient")
    private Integer id;

    @Column(name = "Name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "Unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "IsUnlimitedStock", nullable = false)
    private Boolean isUnlimitedStock;

    @Column(name = "MinStockAlert", precision = 12, scale = 4)
    private BigDecimal minStockAlert;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;
}