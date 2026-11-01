package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "PromocionCategorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PromocionCategoria.PromocionCategoriaId.class)
public class PromocionCategoria {

    @Id
    @Column(name = "IdPromocion")
    private Integer idPromocion;

    @Id
    @Column(name = "IdCategoria")
    private Integer idCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPromocion", insertable = false, updatable = false)
    private Promocion promocion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCategoria", insertable = false, updatable = false)
    private Categoria categoria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromocionCategoriaId implements Serializable {
        private Integer idPromocion;
        private Integer idCategoria;
    }
}