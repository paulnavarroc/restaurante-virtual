package com.saboresperu.restaurante.config;

import com.saboresperu.restaurante.repository.SedeRepository;
import com.saboresperu.restaurante.repository.UbigeoDistritoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SedeRepository sedeRepository;
    private final UbigeoDistritoRepository ubigeoDistritoRepository;

    @Override
    public void run(String... args) {
        long sedes = sedeRepository.count();
        long distritos = ubigeoDistritoRepository.count();
        log.info("=== DEV profile ===");
        log.info("Sedes en BD: {}", sedes);
        log.info("Distritos Ubigeo: {}", distritos);
        if (sedes == 0) {
            log.warn("No hay sedes. Ejecuta el seed SQL de Fase 1 (Cocina Miraflores).");
        }
        if (distritos == 0) {
            log.warn("No hay distritos. Ejecuta el seed Ubigeo Lima.");
        }
        log.info("Seed Java v2: catálogos vienen del SQL. Usuarios demo se agregarán en fases siguientes.");
    }
}