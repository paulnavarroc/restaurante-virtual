package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MovimientosStock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdMovimiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSede", nullable = false)
    private Sede sede;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdInsumo", nullable = false)
    private Insumo insumo;

    /** ENTRADA, SALIDA, AJUSTE, RESERVA, CONSUMO, LIBERACION */
    @Column(name = "Tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "Cantidad", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "CostoUnitario", precision = 12, scale = 4)
    private BigDecimal costoUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUsuario")
    private Usuario usuario;

    @Column(name = "Observacion", length = 400)
    private String observacion;

    @Column(name = "Fecha", nullable = false)
    private LocalDateTime fecha;
}