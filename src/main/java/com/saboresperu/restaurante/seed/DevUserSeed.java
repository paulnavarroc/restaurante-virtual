package com.saboresperu.restaurante.seed;

import com.saboresperu.restaurante.domain.EstadosRepartidor;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevUserSeed implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final DireccionClienteRepository direccionClienteRepository;
    private final UbigeoDistritoRepository ubigeoDistritoRepository;
    private final PasswordEncoder passwordEncoder;
    private final RepartidorRepository repartidorRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByCorreo("cliente@saboresperu.com")) {
            return;
        }
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow();

        Usuario u = Usuario.builder()
                .nombre("Ana")
                .apellido("Cliente")
                .correo("cliente@saboresperu.com")
                .contrasenaHash(passwordEncoder.encode("123456"))
                .telefono("999111222")
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .roles(Set.of(rolCliente))
                .build();
        u = usuarioRepository.save(u);

        Cliente c = clienteRepository.save(Cliente.builder()
                .usuario(u)
                .fechaRegistro(LocalDateTime.now())
                .build());

        UbigeoDistrito miraflores = ubigeoDistritoRepository.findById("150122")
                .orElseThrow();

        direccionClienteRepository.save(DireccionCliente.builder()
                .cliente(c)
                .distrito(miraflores)
                .alias("Casa")
                .calle("Av. Benavides 500")
                .numero("500")
                .referencia("Cerca al óvalo")
                .lat(new BigDecimal("-12.1250000"))
                .lng(new BigDecimal("-77.0200000"))
                .esPrincipal(true)
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build());
/*
        Repartidor rep = Repartidor.builder()
                .usuario(usuarioRepartidor)
                .sede(sedeMiraflores)
                .telefono("999888777")
                .estado(EstadosRepartidor.DISPONIBLE)
                .activo(true)
                .build();
        repartidorRepository.save(rep);
 */

        log.info("Usuario demo cliente@saboresperu.com / 123456 creado");
    }
}
