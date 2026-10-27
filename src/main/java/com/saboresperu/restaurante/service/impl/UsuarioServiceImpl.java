package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.RegistroRequestDTO;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final DireccionRepository direccionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Usuario registrar(RegistroRequestDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new BusinessException("El correo ya está registrado");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .correo(dto.getCorreo())
                .contrasenaHash(passwordEncoder.encode(dto.getPassword()))
                .telefono(dto.getTelefono())
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .roles(Set.of(rolCliente))
                .build();

        usuario = usuarioRepository.save(usuario);

        Cliente cliente = Cliente.builder()
                .usuario(usuario)
                .documentoTipo(dto.getDocumentoTipo())
                .documentoNumero(dto.getDocumentoNumero())
                .fechaRegistro(LocalDateTime.now())
                .build();
        cliente = clienteRepository.save(cliente);

        // Dirección inicial (opcional)
        if (dto.getDireccionCompleta() != null && !dto.getDireccionCompleta().isBlank()) {
            Direccion direccion = Direccion.builder()
                    .cliente(cliente)
                    .direccionCompleta(dto.getDireccionCompleta())
                    .distrito(dto.getDistrito() != null ? dto.getDistrito() : "Lima")
                    .provincia(dto.getProvincia() != null ? dto.getProvincia() : "Lima")
                    .departamento(dto.getDepartamento() != null ? dto.getDepartamento() : "Lima")
                    .esPrincipal(true)
                    .activo(true)
                    .build();
            direccionRepository.save(direccion);
        }

        return usuario;
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}