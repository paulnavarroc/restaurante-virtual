package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.domain.PermissionModes;
import com.saboresperu.restaurante.dto.UserAdminFormDto;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.BranchRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.AdminAclService;
import com.saboresperu.restaurante.service.interfaces.AdminUserService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final BranchRepository branchRepository;
    private final AdminAclService adminAclService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_USERS_VIEW')")
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("titulo", "Usuarios");
        model.addAttribute("users", adminUserService.search(q));
        model.addAttribute("q", q);
        return "admin/users/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERM_USERS_CREATE')")
    public String createForm(Model model) {
        model.addAttribute("titulo", "Nuevo usuario");
        model.addAttribute("form", new UserAdminFormDto(
                null, null, null, null, null, null,
                true, new HashSet<>(), null, null,
                PermissionModes.INHERIT, Set.of()
        ));
        addCatalogs(model);
        return "admin/users/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_USERS_CREATE')")
    public String create(@Valid @ModelAttribute("form") UserAdminFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Nuevo usuario");
            addCatalogs(model);
            return "admin/users/form";
        }
        try {
            adminUserService.create(form, actorId());
            ra.addFlashAttribute("mensaje", "Usuario creado");
            return "redirect:/admin/users";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Nuevo usuario");
            addCatalogs(model);
            return "admin/users/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERM_USERS_EDIT')")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("titulo", "Editar usuario");
        model.addAttribute("form", adminUserService.getForm(id));
        model.addAttribute("preview", adminUserService.previewEffective(id));
        model.addAttribute("allPermissions", adminAclService.listPermissions());
        addCatalogs(model);
        return "admin/users/form";
    }

    @GetMapping("/{id}/permissions/preview")
    @PreAuthorize("hasAuthority('PERM_USERS_EDIT')")
    public String previewAjax(@PathVariable Integer id, Model model) {
        // opcional: fragmento Thymeleaf; o reutilizar edit con ?tab=preview
        model.addAttribute("preview", adminUserService.previewEffective(id));
        return "admin/users/permission-preview :: preview";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_USERS_EDIT')")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") UserAdminFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Editar usuario");
            addCatalogs(model);
            return "admin/users/form";
        }
        try {
            adminUserService.update(id, form, actorId());
            ra.addFlashAttribute("mensaje", "Usuario actualizado");
            return "redirect:/admin/users";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Editar usuario");
            addCatalogs(model);
            return "admin/users/form";
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PERM_USERS_EDIT')")
    public String deactivate(@PathVariable Integer id, RedirectAttributes ra) {
        adminUserService.setActive(id, false);
        ra.addFlashAttribute("mensaje", "Usuario desactivado");
        return "redirect:/admin/users";
    }

    private void addCatalogs(Model model) {
        model.addAttribute("roles", adminUserService.listRoles());
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
    }

    private Integer actorId() {
        return userRepository.findByEmail(securityUtils.currentEmail())
                .map(User::getId)
                .orElse(null);
    }
}