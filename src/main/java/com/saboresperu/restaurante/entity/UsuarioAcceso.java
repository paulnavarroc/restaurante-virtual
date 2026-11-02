package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "UsuarioAccesos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAcceso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdAcceso")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdUsuario", nullable = false)
    private Usuario usuario;

    @Column(name = "Evento", nullable = false, length = 30)
    private String evento;

    @Column(name = "Exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "Ip", length = 45)
    private String ip;

    @Column(name = "UserAgent", length = 500)
    private String userAgent;

    @Column(name = "Navegador", length = 80)
    private String navegador;

    @Column(name = "SistemaOperativo", length = 80)
    private String sistemaOperativo;

    @Column(name = "Dispositivo", length = 40)
    private String dispositivo;

    @Column(name = "SessionId", length = 100)
    private String sessionId;

    @Column(name = "Detalle", length = 300)
    private String detalle;

    @Column(name = "Fecha", nullable = false)
    private LocalDateTime fecha;
}
