package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MenuItemPermissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(MenuItemPermissionId.class)
public class MenuItemPermission {

    @Id
    @Column(name = "IdMenuItem")
    private Integer menuItemId;

    @Id
    @Column(name = "IdPermission")
    private Integer permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdMenuItem", insertable = false, updatable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPermission", insertable = false, updatable = false)
    private Permission permission;
}