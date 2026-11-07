package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SecurityAuditLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdSecurityAudit")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser")
    private User user;

    @Column(name = "Action", nullable = false, length = 50)
    private String action;

    @Column(name = "EntityType", nullable = false, length = 50)
    private String entityType;

    @Column(name = "EntityId", length = 50)
    private String entityId;

    @Column(name = "Detail", columnDefinition = "NVARCHAR(MAX)")
    private String detail;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}