package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PaymentMethods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPaymentMethod")
    private Integer id;

    @Column(name = "Code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "Name", nullable = false, length = 80)
    private String name;

    @Column(name = "IsOnline", nullable = false)
    private Boolean isOnline;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "SortOrder", nullable = false)
    private Integer sortOrder;
}
