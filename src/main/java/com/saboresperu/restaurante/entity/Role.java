package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRole")
    private Integer id;

    @Column(name = "Name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "Description", length = 200)
    private String description;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}