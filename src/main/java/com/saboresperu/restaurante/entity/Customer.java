package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCustomer")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdUser", nullable = false, unique = true)
    private User user;

    @Column(name = "DocumentType", length = 20)
    private String documentType;

    @Column(name = "DocumentNumber", length = 30)
    private String documentNumber;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}