package com.saboresperu.restaurante.entity;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionGroupId implements Serializable {
    private Integer roleId;
    private Integer permissionGroupId;
}