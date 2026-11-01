package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.MenuDiaDTO;
import java.time.LocalDate;

public interface MenuDiaService {
    MenuDiaDTO obtenerPublicado(Integer sedeId, LocalDate fecha);
    void marcarItemAgotado(Integer menuDiaItemId, boolean disponible);
}