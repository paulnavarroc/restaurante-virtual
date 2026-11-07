package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OrderStatuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdOrderStatus")
    private Integer id;

    @Column(name = "Code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "Name", nullable = false, length = 80)
    private String name;

    @Column(name = "SortOrder", nullable = false)
    private Integer sortOrder;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;
}