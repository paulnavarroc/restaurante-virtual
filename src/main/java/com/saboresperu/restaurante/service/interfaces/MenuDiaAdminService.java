package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.*;

import java.util.List;

public interface MenuDiaAdminService {

    List<MenuDiaAdminDTO> listarPorSede(Integer sedeId);

    MenuDiaAdminDTO obtener(Integer menuDiaId);

    MenuDiaAdminDTO crearBorrador(CrearMenuDiaRequestDTO request);

    MenuDiaAdminDTO agregarItem(Integer menuDiaId, AgregarMenuItemRequestDTO request);

    void quitarItem(Integer menuDiaId, Integer menuDiaItemId);

    void marcarDisponible(Integer menuDiaItemId, boolean disponible);

    MenuDiaAdminDTO publicar(Integer menuDiaId, Integer usuarioId);

    MenuDiaAdminDTO cerrar(Integer menuDiaId);
}