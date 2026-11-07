package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdProduct")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdCategory", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRegion")
    private Region region;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    @Column(name = "Description", length = 1000)
    private String description;

    @Column(name = "BasePrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @Column(name = "PrepTimeMinutes")
    private Integer prepTimeMinutes;

    @Column(name = "IsUnlimitedStock", nullable = false)
    private Boolean isUnlimitedStock;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
