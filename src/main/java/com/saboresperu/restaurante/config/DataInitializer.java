package com.saboresperu.restaurante.config;

import com.saboresperu.restaurante.entity.GeoDistrict;
import com.saboresperu.restaurante.repository.BranchRepository;
import com.saboresperu.restaurante.repository.GeoDistrictRepository;
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

    private final BranchRepository branchRepository;
    private final GeoDistrictRepository geoDistrictRepository;

    @Override
    public void run(String... args) {
        long branchs = branchRepository.count();
        long districts = geoDistrictRepository.count();
        log.info("=== DEV profile ===");
        log.info("Sedes en BD: {}", branchs);
        log.info("Distritos Ubigeo: {}", districts);
        if (branchs == 0) {
            log.warn("No hay sedes. Ejecuta el seed SQL de Fase 1 (Cocina Miraflores).");
        }
        if (districts == 0) {
            log.warn("No hay distritos. Ejecuta el seed Ubigeo Lima.");
        }
    }
}