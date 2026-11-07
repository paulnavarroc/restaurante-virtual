package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdDelivery")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdOrder", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCourier")
    private Courier courier;

    @Column(name = "Status", nullable = false, length = 30)
    private String status;

    @Column(name = "AssignedAt")
    private LocalDateTime assignedAt;

    @Column(name = "PickedUpAt")
    private LocalDateTime pickedUpAt;

    @Column(name = "DeliveredAt")
    private LocalDateTime deliveredAt;

    @Column(name = "Notes", length = 300)
    private String notes;
}