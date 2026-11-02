package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.EventosAcceso;
import com.saboresperu.restaurante.entity.Usuario;
import com.saboresperu.restaurante.entity.UsuarioAcceso;
import com.saboresperu.restaurante.repository.UsuarioAccesoRepository;
import com.saboresperu.restaurante.repository.UsuarioRepository;
import com.saboresperu.restaurante.service.interfaces.AuditoriaAccesoService;
import com.saboresperu.restaurante.util.RequestInfoUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaAccesoServiceImpl implements AuditoriaAccesoService {

    private final UsuarioAccesoRepository accesoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void registrarLoginOk(Usuario usuario, HttpServletRequest request) {
        String ua = RequestInfoUtils.userAgent(request);
        String ip = RequestInfoUtils.clientIp(request);
        LocalDateTime ahora = LocalDateTime.now();

        accesoRepository.save(build(usuario, usuario.getCorreo(), EventosAcceso.LOGIN_OK,
                true, request, ua, ip, null));

        usuario.setUltimoAcceso(ahora);
        usuario.setUltimaIp(ip);
        usuario.setUltimoUserAgent(ua);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void registrarLoginFail(String correo, HttpServletRequest request, String detalle) {
        String ua = RequestInfoUtils.userAgent(request);
        String ip = RequestInfoUtils.clientIp(request);
        accesoRepository.save(build(null, correo, EventosAcceso.LOGIN_FAIL,
                false, request, ua, ip, detalle));
    }

    @Override
    @Transactional
    public void registrarLogout(Usuario usuario, HttpServletRequest request) {
        if (usuario == null) return;
        String ua = RequestInfoUtils.userAgent(request);
        String ip = RequestInfoUtils.clientIp(request);
        accesoRepository.save(build(usuario, usuario.getCorreo(), EventosAcceso.LOGOUT,
                true, request, ua, ip, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioAcceso> listarRecientes(int limit) {
        return accesoRepository.findRecientes(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioAcceso> listarPorUsuario(Integer usuarioId, int limit) {
        return accesoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId, PageRequest.of(0, limit));
    }

    private UsuarioAcceso build(Usuario usuario, String correo, String evento, boolean ok,
                                HttpServletRequest request, String ua, String ip, String detalle) {
        String sessionId = null;
        HttpSession session = request.getSession(false);
        if (session != null) sessionId = session.getId();

        return UsuarioAcceso.builder()
                .usuario(usuario)
                .correoIntentado(correo)
                .evento(evento)
                .exitoso(ok)
                .ip(ip)
                .userAgent(ua)
                .navegador(RequestInfoUtils.navegador(ua))
                .sistemaOperativo(RequestInfoUtils.sistemaOperativo(ua))
                .dispositivo(RequestInfoUtils.dispositivo(ua))
                .sessionId(sessionId)
                .detalle(detalle)
                .fecha(LocalDateTime.now())
                .build();
    }
}