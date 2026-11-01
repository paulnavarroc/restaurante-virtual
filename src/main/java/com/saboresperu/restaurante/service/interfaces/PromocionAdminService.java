package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.*;

import java.util.List;

public interface PromocionAdminService {
    List<PromocionListDTO> listar();
    PromocionFormDTO obtenerForm(Integer id);
    Integer crear(PromocionFormDTO form, Integer usuarioId);
    void actualizar(Integer id, PromocionFormDTO form, Integer usuarioId);
    void activar(Integer id, boolean activa, Integer usuarioId);

    List<CuponListDTO> listarCupones();
    Integer crearCupon(CuponFormDTO form);
    void activarCupon(Integer id, boolean activo);
}