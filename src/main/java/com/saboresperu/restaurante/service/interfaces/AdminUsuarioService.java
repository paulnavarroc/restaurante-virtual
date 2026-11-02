package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.UsuarioAdminFormDTO;
import com.saboresperu.restaurante.dto.UsuarioAdminListDTO;

import java.util.List;

public interface AdminUsuarioService {
    List<UsuarioAdminListDTO> listar(String rolFiltro);
    UsuarioAdminFormDTO obtenerForm(Integer id);
    Integer crear(UsuarioAdminFormDTO form);
    void actualizar(Integer id, UsuarioAdminFormDTO form);
    void setActivo(Integer id, boolean activo);
}