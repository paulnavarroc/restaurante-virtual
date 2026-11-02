package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.EstadosRepartidor;
import com.saboresperu.restaurante.dto.UsuarioAdminFormDTO;
import com.saboresperu.restaurante.dto.UsuarioAdminListDTO;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.AdminUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUsuarioServiceImpl implements AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final PersonalCocinaRepository personalCocinaRepository;
    private final RepartidorRepository repartidorRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioAdminListDTO> listar(String rolFiltro) {
        List<Usuario> usuarios = (rolFiltro == null || rolFiltro.isBlank())
                ? usuarioRepository.findAllWithRoles()
                : usuarioRepository.findByRolNombre(rolFiltro.trim().toUpperCase());

        return usuarios.stream().map(this::toListDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioAdminFormDTO obtenerForm(Integer id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        String rol = u.getRoles().stream().map(Rol::getNombre).findFirst().orElse("");
        UsuarioAdminFormDTO.UsuarioAdminFormDTOBuilder b = UsuarioAdminFormDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .correo(u.getCorreo())
                .telefono(u.getTelefono())
                .rol(rol)
                .activo(u.getActivo());

        personalCocinaRepository.findByUsuarioId(id).ifPresent(p -> {
            b.sedeId(p.getSede().getId());
            b.cargo(p.getCargo());
        });
        repartidorRepository.findByUsuarioId(id).ifPresent(r -> {
            b.sedeId(r.getSede().getId());
            b.vehiculo(r.getVehiculo());
        });
        return b.build();
    }

    @Override
    @Transactional
    public Integer crear(UsuarioAdminFormDTO form) {
        if (usuarioRepository.existsByCorreo(form.getCorreo().trim().toLowerCase())) {
            throw new BusinessException("El correo ya está registrado");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria");
        }
        String rolNombre = form.getRol().trim().toUpperCase();
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new BusinessException("Rol no válido: " + rolNombre));

        if (requiereSede(rolNombre) && form.getSedeId() == null) {
            throw new BusinessException("Debe indicar sede para " + rolNombre);
        }

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario u = Usuario.builder()
                .nombre(form.getNombre().trim())
                .apellido(form.getApellido().trim())
                .correo(form.getCorreo().trim().toLowerCase())
                .contrasenaHash(passwordEncoder.encode(form.getPassword()))
                .telefono(form.getTelefono())
                .activo(form.getActivo() == null || form.getActivo())
                .fechaRegistro(LocalDateTime.now())
                .roles(roles)
                .build();
        u = usuarioRepository.save(u);

        crearPerfilSegunRol(u, form, rolNombre);
        return u.getId();
    }

    @Override
    @Transactional
    public void actualizar(Integer id, UsuarioAdminFormDTO form) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        u.setNombre(form.getNombre().trim());
        u.setApellido(form.getApellido().trim());
        u.setTelefono(form.getTelefono());
        if (form.getActivo() != null) u.setActivo(form.getActivo());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            u.setContrasenaHash(passwordEncoder.encode(form.getPassword()));
        }
        usuarioRepository.save(u);

        String rolNombre = form.getRol() != null ? form.getRol().toUpperCase() : null;
        if (rolNombre != null && requiereSede(rolNombre) && form.getSedeId() != null) {
            Sede sede = sedeRepository.findById(form.getSedeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
            if ("CHEF".equals(rolNombre)) {
                personalCocinaRepository.findByUsuarioId(id).ifPresentOrElse(p -> {
                    p.setSede(sede);
                    p.setCargo(form.getCargo());
                    personalCocinaRepository.save(p);
                }, () -> {});
            }
            if ("REPARTIDOR".equals(rolNombre)) {
                repartidorRepository.findByUsuarioId(id).ifPresent(r -> {
                    r.setSede(sede);
                    r.setVehiculo(form.getVehiculo());
                    repartidorRepository.save(r);
                });
            }
        }
    }

    @Override
    @Transactional
    public void setActivo(Integer id, boolean activo) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        u.setActivo(activo);
        usuarioRepository.save(u);
    }

    private void crearPerfilSegunRol(Usuario u, UsuarioAdminFormDTO form, String rol) {
        switch (rol) {
            case "CLIENTE" -> clienteRepository.save(Cliente.builder()
                    .usuario(u)
                    .fechaRegistro(LocalDateTime.now())
                    .build());
            case "CHEF" -> {
                Sede sede = sedeRepository.findById(form.getSedeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
                personalCocinaRepository.save(PersonalCocina.builder()
                        .usuario(u)
                        .sede(sede)
                        .cargo(form.getCargo() != null ? form.getCargo() : "Chef")
                        .activo(true)
                        .fechaAsignacion(LocalDateTime.now())
                        .build());
            }
            case "REPARTIDOR" -> {
                Sede sede = sedeRepository.findById(form.getSedeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
                repartidorRepository.save(Repartidor.builder()
                        .usuario(u)
                        .sede(sede)
                        .telefono(u.getTelefono())
                        .vehiculo(form.getVehiculo() != null ? form.getVehiculo() : "Moto")
                        .estado(EstadosRepartidor.DISPONIBLE)
                        .activo(true)
                        .build());
            }
            default -> { /* admin / ops: solo usuario+rol */ }
        }
    }

    private boolean requiereSede(String rol) {
        return "CHEF".equals(rol) || "REPARTIDOR".equals(rol);
    }

    private UsuarioAdminListDTO toListDTO(Usuario u) {
        String sede = personalCocinaRepository.findByUsuarioId(u.getId())
                .map(p -> p.getSede().getNombre())
                .or(() -> repartidorRepository.findByUsuarioId(u.getId())
                        .map(r -> r.getSede().getNombre()))
                .orElse(null);

        return UsuarioAdminListDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .correo(u.getCorreo())
                .telefono(u.getTelefono())
                .activo(u.getActivo())
                .roles(u.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList()))
                .sedeNombre(sede)
                .fechaRegistro(u.getFechaRegistro())
                .ultimoAcceso(u.getUltimoAcceso())
                .ultimaIp(u.getUltimaIp())
                .build();
    }
}