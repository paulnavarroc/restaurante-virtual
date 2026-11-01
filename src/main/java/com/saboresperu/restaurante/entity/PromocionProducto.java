package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "PromocionProductos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PromocionProducto.PromocionProductoId.class)
public class PromocionProducto {

    @Id
    @Column(name = "IdPromocion")
    private Integer idPromocion;

    @Id
    @Column(name = "IdProducto")
    private Integer idProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPromocion", insertable = false, updatable = false)
    private Promocion promocion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProducto", insertable = false, updatable = false)
    private Producto producto;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromocionProductoId implements Serializable {
        private Integer idPromocion;
        private Integer idProducto;
    }
}