package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CartItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCartItem")
    private Integer id;

    @Column(name = "IdCustomer", nullable = false)
    private Integer customerId;

    @Column(name = "IdProduct", nullable = false)
    private Integer productId;

    @Column(name = "IdDailyMenuItem")
    private Integer dailyMenuItemId;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "UnitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "ProductName", nullable = false, length = 150)
    private String productName;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}