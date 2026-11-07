package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPermission")
    private Integer id;

    @Column(name = "Code", nullable = false, unique = true, length = 80)
    private String code;

    @Column(name = "Module", nullable = false, length = 80)
    private String module;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    @Column(name = "Description", length = 300)
    private String description;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;
}
