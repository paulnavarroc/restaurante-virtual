package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MenusDia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdMenuDia")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSede", nullable = false)
    private Sede sede;

    @Column(name = "Fecha", nullable = false)
    private LocalDate fecha;

    /** BORRADOR, PUBLICADO, CERRADO */
    @Column(name = "Estado", nullable = false, length = 20)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUsuarioPublicacion")
    private Usuario usuarioPublicacion;

    @Column(name = "FechaPublicacion")
    private LocalDateTime fechaPublicacion;

    @OneToMany(mappedBy = "menuDia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuDiaItem> items = new ArrayList<>();
}