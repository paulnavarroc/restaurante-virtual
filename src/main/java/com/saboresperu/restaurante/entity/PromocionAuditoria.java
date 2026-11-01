package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PromocionAuditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdAuditoria")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdPromocion", nullable = false)
    private Promocion promocion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdUsuario", nullable = false)
    private Usuario usuario;

    @Column(name = "Campo", nullable = false, length = 100)
    private String campo;

    @Column(name = "ValorAnterior", length = 500)
    private String valorAnterior;

    @Column(name = "ValorNuevo", length = 500)
    private String valorNuevo;

    @Column(name = "Fecha", nullable = false)
    private LocalDateTime fecha;
}