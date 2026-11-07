package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PromotionAuditLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPromotionAudit")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdPromotion", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser")
    private User user;

    @Column(name = "Action", nullable = false, length = 40)
    private String action;

    @Column(name = "Detail", columnDefinition = "NVARCHAR(MAX)")
    private String detail;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
