package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PromocionProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromocionProductoRepository extends JpaRepository<PromocionProducto, PromocionProducto.PromocionProductoId> {
    List<PromocionProducto> findByIdPromocion(Integer idPromocion);
    boolean existsByIdPromocionAndIdProducto(Integer idPromocion, Integer idProducto);
    void deleteByIdPromocion(Integer idPromocion);
}