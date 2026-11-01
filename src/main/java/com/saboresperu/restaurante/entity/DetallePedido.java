package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DetallePedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdDetallePedido")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdPedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdProducto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdMenuDiaItem")
    private MenuDiaItem menuDiaItem;

    @Column(name = "Cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "PrecioLista", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioLista;

    @Column(name = "PrecioUnitarioFinal", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitarioFinal;

    @Column(name = "DescuentoUnitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuentoUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPromocion")
    private Promocion promocion;

    @Column(name = "Subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}