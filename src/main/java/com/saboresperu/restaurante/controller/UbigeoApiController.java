package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.UbigeoOptionDTO;
import com.saboresperu.restaurante.service.interfaces.UbigeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ubigeo")
@RequiredArgsConstructor
public class UbigeoApiController {

    private final UbigeoService ubigeoService;

    @GetMapping("/departamentos")
    public List<UbigeoOptionDTO> departamentos() {
        return ubigeoService.listarDepartamentos();
    }

    @GetMapping("/provincias")
    public List<UbigeoOptionDTO> provincias(@RequestParam String departamentoId) {
        return ubigeoService.listarProvincias(departamentoId);
    }

    @GetMapping("/distritos")
    public List<UbigeoOptionDTO> distritos(@RequestParam String provinciaId) {
        return ubigeoService.listarDistritos(provinciaId);
    }
}