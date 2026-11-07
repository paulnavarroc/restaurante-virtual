package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SecurityConfig")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityConfig {
    @Id
    @Column(name = "IdSecurityConfig")
    private Integer id;

    @Column(name = "SecurityVersion", nullable = false)
    private Integer securityVersion;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}