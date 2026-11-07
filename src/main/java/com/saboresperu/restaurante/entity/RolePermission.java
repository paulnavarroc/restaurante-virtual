package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RolePermissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RolePermissionId.class)
public class RolePermission {

    @Id
    @Column(name = "IdRole")
    private Integer roleId;

    @Id
    @Column(name = "IdPermission")
    private Integer permissionId;

    /** ALLOW | DENY */
    @Column(name = "Effect", nullable = false, length = 10)
    private String effect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRole", insertable = false, updatable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPermission", insertable = false, updatable = false)
    private Permission permission;
}