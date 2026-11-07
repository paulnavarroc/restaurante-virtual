package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RolePermissionGroups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RolePermissionGroupId.class)
public class RolePermissionGroup {

    @Id
    @Column(name = "IdRole")
    private Integer roleId;

    @Id
    @Column(name = "IdPermissionGroup")
    private Integer permissionGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRole", insertable = false, updatable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPermissionGroup", insertable = false, updatable = false)
    private PermissionGroup permissionGroup;
}