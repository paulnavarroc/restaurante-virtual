package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "UserPermissionOverrides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserPermissionOverrideId.class)
public class UserPermissionOverride {

    @Id
    @Column(name = "IdUser")
    private Integer userId;

    @Id
    @Column(name = "IdPermission")
    private Integer permissionId;

    /** ALLOW | DENY */
    @Column(name = "Effect", nullable = false, length = 10)
    private String effect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPermission", insertable = false, updatable = false)
    private Permission permission;
}