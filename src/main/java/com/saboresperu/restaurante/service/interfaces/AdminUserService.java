package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.EffectivePermissionPreviewDto;
import com.saboresperu.restaurante.dto.RoleOptionDto;
import com.saboresperu.restaurante.dto.UserAdminFormDto;
import com.saboresperu.restaurante.dto.UserAdminRowDto;

import java.util.List;

public interface AdminUserService {
    List<UserAdminRowDto> search(String query);
    UserAdminFormDto getForm(Integer id);
    Integer create(UserAdminFormDto form, Integer actorUserId);
    void update(Integer id, UserAdminFormDto form, Integer actorUserId);
    void setActive(Integer id, boolean active);
    List<RoleOptionDto> listRoles();
    EffectivePermissionPreviewDto previewEffective(Integer userId);
}