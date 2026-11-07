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
    @Column(name = "IdUser")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "IdUser")
    private User user;

    @Column(name = "PermissionMode", nullable = false, length = 20)
    private String permissionMode;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
