package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.BranchAdminRowDto;
import com.saboresperu.restaurante.dto.BranchFormDto;
import com.saboresperu.restaurante.entity.Branch;
import com.saboresperu.restaurante.repository.BranchRepository;
import com.saboresperu.restaurante.service.interfaces.AdminBranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBranchServiceImpl implements AdminBranchService {

    private final BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BranchAdminRowDto> listAll() {
        return branchRepository.findAllByOrderByNameAsc().stream()
                .map(b -> new BranchAdminRowDto(
                        b.getId(),
                        b.getName(),
                        b.getAddress(),
                        b.getPhone(),
                        b.getCoverageRadiusKm(),
                        Boolean.TRUE.equals(b.getIsActive()),
                        b.getTenantKey()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchFormDto getForm(Integer id) {
        Branch b = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));
        return new BranchFormDto(
                b.getId(),
                b.getName(),
                b.getAddress(),
                b.getLat(),
                b.getLng(),
                b.getCoverageRadiusKm(),
                b.getPhone(),
                b.getTenantKey(),
                Boolean.TRUE.equals(b.getIsActive())
        );
    }

    @Override
    @Transactional
    public Integer create(BranchFormDto form) {
        String name = form.name().trim();
        if (branchRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Ya existe una sede con ese nombre");
        }
        Branch b = Branch.builder()
                .name(name)
                .address(form.address().trim())
                .lat(coord(form.lat()))
                .lng(coord(form.lng()))
                .coverageRadiusKm(money2(form.coverageRadiusKm()))
                .phone(blankToNull(form.phone()))
                .tenantKey(blankToNull(form.tenantKey()))
                .isActive(form.active())
                .createdAt(LocalDateTime.now())
                .build();
        return branchRepository.save(b).getId();
    }

    @Override
    @Transactional
    public void update(Integer id, BranchFormDto form) {
        Branch b = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));
        String name = form.name().trim();
        if (branchRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Ya existe una sede con ese nombre");
        }
        b.setName(name);
        b.setAddress(form.address().trim());
        b.setLat(coord(form.lat()));
        b.setLng(coord(form.lng()));
        b.setCoverageRadiusKm(money2(form.coverageRadiusKm()));
        b.setPhone(blankToNull(form.phone()));
        b.setTenantKey(blankToNull(form.tenantKey()));
        b.setIsActive(form.active());
        branchRepository.save(b);
    }

    @Override
    @Transactional
    public void setActive(Integer id, boolean active) {
        Branch b = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));
        b.setIsActive(active);
        branchRepository.save(b);
    }

    private static BigDecimal coord(BigDecimal v) {
        return v.setScale(7, RoundingMode.HALF_UP);
    }

    private static BigDecimal money2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}