package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.PermissionGroupFormDto;
import com.saboresperu.restaurante.dto.RoleAdminFormDto;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.AdminAclService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/admin/security")
@RequiredArgsConstructor
public class AdminAclController {

    private final AdminAclService adminAclService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private Integer actorId() {
        return userRepository.findByEmail(securityUtils.currentEmail())
                .map(User::getId).orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_ROLES_VIEW','PERM_PERMISSIONS_VIEW')")
    public String index(Model model) {
        model.addAttribute("roles", adminAclService.listRoles());
        model.addAttribute("groups", adminAclService.listGroups());
        model.addAttribute("permissions", adminAclService.listPermissions());
        return "admin/security/index";
    }

    @GetMapping("/roles/new")
    @PreAuthorize("hasAuthority('PERM_ROLES_MANAGE')")
    public String newRole(Model model) {
        model.addAttribute("form", new RoleAdminFormDto(
                null, null, null, true, Set.of(), Set.of()));
        model.addAttribute("allGroups", adminAclService.listGroups());
        model.addAttribute("allPermissions", adminAclService.listPermissions());
        return "admin/security/role-form";
    }

    @GetMapping("/roles/{id}/edit")
    @PreAuthorize("hasAuthority('PERM_ROLES_MANAGE')")
    public String editRole(@PathVariable Integer id, Model model) {
        model.addAttribute("form", adminAclService.getRoleForm(id));
        model.addAttribute("allGroups", adminAclService.listGroups());
        model.addAttribute("allPermissions", adminAclService.listPermissions());
        return "admin/security/role-form";
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('PERM_ROLES_MANAGE')")
    public String createRole(@Valid @ModelAttribute("form") RoleAdminFormDto form,
                             BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("allGroups", adminAclService.listGroups());
            model.addAttribute("allPermissions", adminAclService.listPermissions());
            return "admin/security/role-form";
        }
        try {
            adminAclService.createRole(form, actorId());
            ra.addFlashAttribute("mensaje", "Rol creado");
            return "redirect:/admin/security";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("allGroups", adminAclService.listGroups());
            model.addAttribute("allPermissions", adminAclService.listPermissions());
            return "admin/security/role-form";
        }
    }

    @PostMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('PERM_ROLES_MANAGE')")
    public String updateRole(@PathVariable Integer id,
                             @Valid @ModelAttribute("form") RoleAdminFormDto form,
                             BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("allGroups", adminAclService.listGroups());
            model.addAttribute("allPermissions", adminAclService.listPermissions());
            return "admin/security/role-form";
        }
        try {
            adminAclService.updateRole(id, form, actorId());
            ra.addFlashAttribute("mensaje", "Rol actualizado");
            return "redirect:/admin/security";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("allGroups", adminAclService.listGroups());
            model.addAttribute("allPermissions", adminAclService.listPermissions());
            return "admin/security/role-form";
        }
    }

    @GetMapping("/groups/new")
    @PreAuthorize("hasAuthority('PERM_PERMISSIONS_MANAGE')")
    public String newGroup(Model model) {
        model.addAttribute("form", new PermissionGroupFormDto(
                null, null, null, null, true, Set.of()));
        model.addAttribute("allPermissions", adminAclService.listPermissions());
        return "admin/security/group-form";
    }

    @GetMapping("/groups/{id}/edit")
    @PreAuthorize("hasAuthority('PERM_PERMISSIONS_MANAGE')")
    public String editGroup(@PathVariable Integer id, Model model) {
        model.addAttribute("form", adminAclService.getGroupForm(id));
        model.addAttribute("allPermissions", adminAclService.listPermissions());
        return "admin/security/group-form";
    }

    @PostMapping("/groups")
    @PreAuthorize("hasAuthority('PERM_PERMISSIONS_MANAGE')")
    public String createGroup(@Valid @ModelAttribute("form") PermissionGroupFormDto form,
                              BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("allPermissions", adminAclService.listPermissions());
            return "admin/security/group-form";
        }
        try {
            adminAclService.createGroup(form, actorId());
            ra.addFlashAttribute("mensaje", "Grupo creado");
            return "redirect:/admin/security";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("allPermissions", adminAclService.listPermissions());
            return "admin/security/group-form";
        }
    }

    @PostMapping("/groups/{id}")
    @PreAuthorize("hasAuthority('PERM_PERMISSIONS_MANAGE')")
    public String updateGroup(@PathVariable Integer id,
                              @Valid @ModelAttribute("form") PermissionGroupFormDto form,
                              BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("allPermissions", adminAclService.listPermissions());
            return "admin/security/group-form";
        }
        try {
            adminAclService.updateGroup(id, form, actorId());
            ra.addFlashAttribute("mensaje", "Grupo actualizado");
            return "redirect:/admin/security";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("allPermissions", adminAclService.listPermissions());
            return "admin/security/group-form";
        }
    }
}