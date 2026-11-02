package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.RegistroRequestDTO;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.ClienteRepository;
import com.saboresperu.restaurante.repository.DireccionClienteRepository;
import com.saboresperu.restaurante.repository.RolRepository;
import com.saboresperu.restaurante.repository.UbigeoDistritoRepository;
import com.saboresperu.restaurante.repository.UsuarioRepository;
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
    private final DireccionClienteRepository direccionClienteRepository;
    private final UbigeoDistritoRepository ubigeoDistritoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Usuario registrar(RegistroRequestDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new BusinessException("El correo ya está registrado");
        }
        if (dto.getPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new BusinessException("Rol CLIENTE no encontrado en el sistema"));

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .correo(dto.getCorreo().trim().toLowerCase())
                .contrasenaHash(passwordEncoder.encode(dto.getPassword()))
                .telefono(blankToNull(dto.getTelefono()))
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .roles(Set.of(rolCliente))
                .build();

        usuario = usuarioRepository.save(usuario);

        Cliente cliente = Cliente.builder()
                .usuario(usuario)
                .documentoTipo(blankToNull(dto.getDocumentoTipo()))
                .documentoNumero(blankToNull(dto.getDocumentoNumero()))
                .fechaRegistro(LocalDateTime.now())
                .build();
        cliente = clienteRepository.save(cliente);

        // Dirección opcional v2 (solo si hay distrito Ubigeo + calle)
        crearDireccionInicialSiCorresponde(cliente, dto);

        return usuario;
    }

    private void crearDireccionInicialSiCorresponde(Cliente cliente, RegistroRequestDTO dto) {
        String idDistrito = blankToNull(dto.getIdDistrito());
        String calle = blankToNull(dto.getCalle());

        // Compatibilidad: si el form viejo mandó solo texto, no intentamos mapear a Ubigeo
        if (idDistrito == null || calle == null) {
            return;
        }

        UbigeoDistrito distrito = ubigeoDistritoRepository.findById(idDistrito)
                .orElseThrow(() -> new ResourceNotFoundException("Distrito no válido"));

        DireccionCliente direccion = DireccionCliente.builder()
                .cliente(cliente)
                .distrito(distrito)
                .alias(blankToNull(dto.getAlias()) != null ? dto.getAlias().trim() : "Principal")
                .calle(calle.trim())
                .numero(blankToNull(dto.getNumero()))
                .referencia(blankToNull(dto.getReferencia()))
                .urbanizacion(blankToNull(dto.getUrbanizacion()))
                .lat(distrito.getLatCentro())
                .lng(distrito.getLngCentro())
                .esPrincipal(true)
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        direccionClienteRepository.save(direccion);
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}