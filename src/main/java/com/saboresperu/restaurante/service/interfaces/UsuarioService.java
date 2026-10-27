package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.RegistroRequestDTO;
import com.saboresperu.restaurante.entity.Usuario;

public interface UsuarioService {
    Usuario registrar(RegistroRequestDTO dto);
    Usuario buscarPorCorreo(String correo);
    Usuario buscarPorId(Integer id);
}