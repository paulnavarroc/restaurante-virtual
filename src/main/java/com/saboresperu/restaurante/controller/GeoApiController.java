package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.GeoOptionDto;
import com.saboresperu.restaurante.repository.GeoDepartmentRepository;
import com.saboresperu.restaurante.repository.GeoDistrictRepository;
import com.saboresperu.restaurante.repository.GeoProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoApiController {

    private final GeoDepartmentRepository departmentRepository;
    private final GeoProvinceRepository provinceRepository;
    private final GeoDistrictRepository districtRepository;

    @GetMapping("/departments")
    public List<GeoOptionDto> departments() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(d -> new GeoOptionDto(d.getId(), d.getName()))
                .toList();
    }

    @GetMapping("/provinces")
    public List<GeoOptionDto> provinces(@RequestParam String departmentId) {
        return provinceRepository.findByDepartmentIdOrderByNameAsc(departmentId).stream()
                .map(p -> new GeoOptionDto(p.getId(), p.getName()))
                .toList();
    }

    @GetMapping("/districts")
    public List<GeoOptionDto> districts(@RequestParam String provinceId) {
        return districtRepository.findByProvinceIdOrderByNameAsc(provinceId).stream()
                .map(d -> new GeoOptionDto(d.getId(), d.getName()))
                .toList();
    }
}