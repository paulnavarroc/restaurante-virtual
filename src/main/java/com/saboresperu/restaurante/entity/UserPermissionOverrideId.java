package com.saboresperu.restaurante.entity;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionOverrideId implements Serializable {
    private Integer userId;
    private Integer permissionId;
}