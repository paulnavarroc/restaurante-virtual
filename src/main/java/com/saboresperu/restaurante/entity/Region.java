package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Regions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRegion")
    private Integer id;

    @Column(name = "Name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "Description", length = 300)
    private String description;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;
}