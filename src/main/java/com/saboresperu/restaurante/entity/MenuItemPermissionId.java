package com.saboresperu.restaurante.entity;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemPermissionId implements Serializable {
    private Integer menuItemId;
    private Integer permissionId;
}