package com.saboresperu.restaurante.entity;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionGroupItemId implements Serializable {
    private Integer permissionGroupId;
    private Integer permissionId;
}