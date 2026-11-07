package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PermissionGroupItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PermissionGroupItemId.class)
public class PermissionGroupItem {

    @Id
    @Column(name = "IdPermissionGroup")
    private Integer permissionGroupId;

    @Id
    @Column(name = "IdPermission")
    private Integer permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPermissionGroup", insertable = false, updatable = false)
    private PermissionGroup permissionGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPermission", insertable = false, updatable = false)
    private Permission permission;
}