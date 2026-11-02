package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.Usuario;
import com.saboresperu.restaurante.entity.UsuarioAcceso;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AuditoriaAccesoService {
    void registrarLoginOk(Usuario usuario, HttpServletRequest request);
    void registrarLoginFail(String correo, HttpServletRequest request, String detalle);
    void registrarLogout(Usuario usuario, HttpServletRequest request);
    List<UsuarioAcceso> listarRecientes(int limit);
    List<UsuarioAcceso> listarPorUsuario(Integer usuarioId, int limit);
}