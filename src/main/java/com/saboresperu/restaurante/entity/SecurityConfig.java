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

    /** Siempre 1 (constraint CK_SecurityConfig_Single). */
    @Id
    @Column(name = "IdSecurityConfig", nullable = false)
    private Integer id;

    @Column(name = "SecurityVersion", nullable = false)
    private Integer securityVersion;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
        if (this.id == null) {
            this.id = 1;
        }
        if (this.securityVersion == null) {
            this.securityVersion = 1;
        }
    }
}