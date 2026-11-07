package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "UserPermissionConfigs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermissionConfig {

    @Id
    @Column(name = "IdUser", nullable = false)
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "IdUser")
    private User user;

    /**
     * INHERIT | RESTRICT | CUSTOM
     * (ver PermissionModes)
     */
    @Column(name = "PermissionMode", nullable = false, length = 20)
    private String permissionMode;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
        if (this.permissionMode == null || this.permissionMode.isBlank()) {
            this.permissionMode = "INHERIT";
        } else {
            this.permissionMode = this.permissionMode.trim().toUpperCase();
        }
    }
}