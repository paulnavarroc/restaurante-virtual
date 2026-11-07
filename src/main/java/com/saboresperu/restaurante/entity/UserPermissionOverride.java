package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "UserPermissionOverrides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserPermissionOverride.Pk.class)
public class UserPermissionOverride {

    @Id
    @Column(name = "IdUser", nullable = false)
    private Integer userId;

    @Id
    @Column(name = "IdPermission", nullable = false)
    private Integer permissionId;

    @Column(name = "Effect", nullable = false, length = 10)
    private String effect; // ALLOW | DENY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPermission", insertable = false, updatable = false)
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser", insertable = false, updatable = false)
    private User user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Integer userId;
        private Integer permissionId;
    }
}