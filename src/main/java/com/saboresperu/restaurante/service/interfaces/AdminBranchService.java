package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.BranchAdminRowDto;
import com.saboresperu.restaurante.dto.BranchFormDto;

import java.util.List;

public interface AdminBranchService {
    List<BranchAdminRowDto> listAll();
    BranchFormDto getForm(Integer id);
    Integer create(BranchFormDto form);
    void update(Integer id, BranchFormDto form);
    void setActive(Integer id, boolean active);
}