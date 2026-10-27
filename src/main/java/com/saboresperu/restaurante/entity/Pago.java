package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Pagos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPago")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdMetodoPago", nullable = false)
    private MetodoPago metodoPago;

    @Column(name = "Monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "Estado", nullable = false, length = 30)
    private String estado;          // PENDIENTE, APROBADO, RECHAZADO, DEVUELTO

    @Column(name = "Referencia", length = 150)
    private String referencia;

    @Column(name = "FechaPago")
    private LocalDateTime fechaPago;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;
}