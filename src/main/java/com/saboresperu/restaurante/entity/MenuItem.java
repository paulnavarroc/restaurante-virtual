package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MenuItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdMenuItem")
    private Integer id;

    @Column(name = "Code", nullable = false, unique = true, length = 80)
    private String code;

    @Column(name = "Label", nullable = false, length = 120)
    private String label;

    @Column(name = "Route", nullable = false, length = 200)
    private String route;

    @Column(name = "Icon", length = 50)
    private String icon;

    @Column(name = "MenuGroup", length = 80)
    private String menuGroup;

    @Column(name = "Context", nullable = false, length = 20)
    private String context;

    @Column(name = "SortOrder", nullable = false)
    private Integer sortOrder;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;
}