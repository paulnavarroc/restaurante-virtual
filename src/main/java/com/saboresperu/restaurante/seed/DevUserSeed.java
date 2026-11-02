package com.saboresperu.restaurante.seed;

import com.saboresperu.restaurante.domain.EstadosRepartidor;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class DevUserSeed implements CommandLineRunner {

    private static final String PASSWORD = "123456";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final DireccionClienteRepository direccionClienteRepository;
    private final UbigeoDistritoRepository ubigeoDistritoRepository;
    private final SedeRepository sedeRepository;
    private final RepartidorRepository repartidorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== DevUserSeed: usuarios demo ===");

        ensureRoles();

        Usuario admin = ensureUsuario(
                "admin@saboresperu.com", "Carlos", "Admin",
                "999000001", "ADMINISTRADOR");

        ensureUsuario(
                "ops@saboresperu.com", "Lucia", "Operaciones",
                "999000002", "OPERACIONES");

        ensureUsuario(
                "cocina@saboresperu.com", "Marco", "Chef",
                "999000003", "CHEF");

        Usuario repartidorUser = ensureUsuario(
                "repartidor@saboresperu.com", "Pedro", "Repartidor",
                "999000004", "REPARTIDOR");

        Usuario clienteUser = ensureUsuario(
                "cliente@saboresperu.com", "Ana", "Cliente",
                "999111222", "CLIENTE");

        ensureClienteYDireccion(clienteUser);
        ensureRepartidor(repartidorUser);

        log.info("Usuarios demo listos (password de todos: {})", PASSWORD);
        log.info("  admin@saboresperu.com      → ADMINISTRADOR");
        log.info("  ops@saboresperu.com        → OPERACIONES");
        log.info("  cocina@saboresperu.com     → CHEF");
        log.info("  repartidor@saboresperu.com → REPARTIDOR");
        log.info("  cliente@saboresperu.com    → CLIENTE");
        if (admin != null) {
            // evita warning unused
        }
    }

    // -------------------------------------------------------------------------
    // Roles
    // -------------------------------------------------------------------------

    private void ensureRoles() {
        ensureRol("CLIENTE", "Cliente final que realiza pedidos");
        ensureRol("CHEF", "Personal de cocina");
        ensureRol("REPARTIDOR", "Personal de delivery");
        ensureRol("ADMINISTRADOR", "Administrador del sistema");
        ensureRol("OPERACIONES", "Gestión de menú, stock y promociones");
        ensureRol("SUPERVISOR", "Supervisor / autorizaciones especiales");
    }

    private Rol ensureRol(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol rol = Rol.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .activo(true)
                    .build();
            rol = rolRepository.save(rol);
            log.info("Rol creado: {}", nombre);
            return rol;
        });
    }

    private Rol requireRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "Rol no encontrado: " + nombre + ". Revisa el seed de Roles."));
    }

    // -------------------------------------------------------------------------
    // Usuario genérico
    // -------------------------------------------------------------------------

    private Usuario ensureUsuario(String correo, String nombre, String apellido,
                                  String telefono, String nombreRol) {
        if (usuarioRepository.existsByCorreo(correo)) {
            log.debug("Usuario ya existe: {}", correo);
            return usuarioRepository.findByCorreo(correo).orElseThrow();
        }

        Rol rol = requireRol(nombreRol);
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario u = Usuario.builder()
                .nombre(nombre)
                .apellido(apellido)
                .correo(correo)
                .contrasenaHash(passwordEncoder.encode(PASSWORD))
                .telefono(telefono)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .roles(roles)
                .build();

        u = usuarioRepository.save(u);
        log.info("Usuario creado: {} ({})", correo, nombreRol);
        return u;
    }

    // -------------------------------------------------------------------------
    // Cliente + dirección
    // -------------------------------------------------------------------------

    private void ensureClienteYDireccion(Usuario usuario) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Cliente c = Cliente.builder()
                            .usuario(usuario)
                            .documentoTipo("DNI")
                            .documentoNumero("12345678")
                            .fechaRegistro(LocalDateTime.now())
                            .build();
                    c = clienteRepository.save(c);
                    log.info("Cliente creado para {}", usuario.getCorreo());
                    return c;
                });

        List<DireccionCliente> dirs =
                direccionClienteRepository.findByClienteIdAndActivaTrueOrderByEsPrincipalDesc(cliente.getId());
        if (!dirs.isEmpty()) {
            return;
        }

        UbigeoDistrito miraflores = ubigeoDistritoRepository.findById("150122")
                .orElseGet(() -> ubigeoDistritoRepository.findAll().stream().findFirst().orElse(null));

        if (miraflores == null) {
            log.warn("No hay distritos Ubigeo. No se creó dirección para el cliente demo. "
                    + "Ejecuta el seed SQL de Ubigeo.");
            return;
        }

        direccionClienteRepository.save(DireccionCliente.builder()
                .cliente(cliente)
                .distrito(miraflores)
                .alias("Casa")
                .calle("Av. Benavides")
                .numero("500")
                .referencia("Cerca al óvalo")
                .lat(miraflores.getLatCentro() != null
                        ? miraflores.getLatCentro()
                        : new BigDecimal("-12.1250000"))
                .lng(miraflores.getLngCentro() != null
                        ? miraflores.getLngCentro()
                        : new BigDecimal("-77.0200000"))
                .esPrincipal(true)
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build());

        log.info("Dirección demo creada para cliente@saboresperu.com (distrito {})",
                miraflores.getId());
    }

    // -------------------------------------------------------------------------
    // Repartidor
    // -------------------------------------------------------------------------

    private void ensureRepartidor(Usuario usuario) {
        if (repartidorRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            return;
        }

        List<Sede> sedes = sedeRepository.findByActivaTrue();
        if (sedes.isEmpty()) {
            log.warn("No hay sedes activas. No se creó perfil Repartidor. "
                    + "Ejecuta el seed SQL de sede (Miraflores).");
            return;
        }

        Sede sede = sedes.get(0);

        repartidorRepository.save(Repartidor.builder()
                .usuario(usuario)
                .sede(sede)
                .telefono(usuario.getTelefono())
                .vehiculo("Moto")
                .estado(EstadosRepartidor.DISPONIBLE)
                .activo(true)
                .ultimaLat(sede.getLat() != null ? sede.getLat() : new BigDecimal("-12.1200000"))
                .ultimaLng(sede.getLng() != null ? sede.getLng() : new BigDecimal("-77.0300000"))
                .build());

        log.info("Repartidor creado en sede {} ({})", sede.getId(), sede.getNombre());
    }
}