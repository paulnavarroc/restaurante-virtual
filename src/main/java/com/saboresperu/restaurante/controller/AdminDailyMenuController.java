package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.DailyMenuCreateRequest;
import com.saboresperu.restaurante.dto.DailyMenuItemRequest;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.BranchRepository;
import com.saboresperu.restaurante.repository.ProductRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.AdminDailyMenuService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/daily-menus")
@RequiredArgsConstructor
public class AdminDailyMenuController {

    private final AdminDailyMenuService adminDailyMenuService;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private Integer currentUserId() {
        return userRepository.findByEmail(securityUtils.currentEmail())
                .map(User::getId)
                .orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_VIEW')")
    public String list(Model model) {
        model.addAttribute("titulo", "Menús del día");
        model.addAttribute("menus", adminDailyMenuService.listAll());
        return "admin/daily-menus/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_MANAGE')")
    public String createForm(Model model) {
        model.addAttribute("titulo", "Nuevo menú del día");
        model.addAttribute("form", new DailyMenuCreateRequest(null, LocalDate.now()));
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        return "admin/daily-menus/create";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_MANAGE')")
    public String create(@Valid @ModelAttribute("form") DailyMenuCreateRequest form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Nuevo menú del día");
            model.addAttribute("branches", branchRepository.findByIsActiveTrue());
            return "admin/daily-menus/create";
        }
        try {
            Integer id = adminDailyMenuService.createDraft(form);
            ra.addFlashAttribute("mensaje", "Borrador creado");
            return "redirect:/admin/daily-menus/" + id;
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Nuevo menú del día");
            model.addAttribute("branches", branchRepository.findByIsActiveTrue());
            return "admin/daily-menus/create";
        }
    }

    @PostMapping("/{id}/items/{itemId}/shifts")
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_MANAGE')")
    public String setShifts(@PathVariable Integer id,
                            @PathVariable Integer itemId,
                            @RequestParam(required = false) List<Integer> shiftIds,
                            RedirectAttributes ra) {
        try {
            adminDailyMenuService.setItemShifts(id, itemId, shiftIds != null ? shiftIds : List.of());
            ra.addFlashAttribute("mensaje", "Turnos actualizados");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/daily-menus/" + id;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_VIEW')")
    public String detail(@PathVariable Integer id, Model model) {
        var menu = adminDailyMenuService.getDetail(id);
        model.addAttribute("titulo", "Menú del día");
        model.addAttribute("menu", menu);
        model.addAttribute("branchShifts", adminDailyMenuService.listBranchShifts(menu.branchId()));
        model.addAttribute("products", productRepository.findAllActiveWithDetails());
        model.addAttribute("itemForm", new DailyMenuItemRequest(null, null, null, true));
        return "admin/daily-menus/detail";
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_MANAGE')")
    public String addItem(@PathVariable Integer id,
                          @Valid @ModelAttribute("itemForm") DailyMenuItemRequest form,
                          RedirectAttributes ra) {
        try {
            adminDailyMenuService.addItem(id, form);
            ra.addFlashAttribute("mensaje", "Producto agregado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/daily-menus/" + id;
    }

    @PostMapping("/{id}/items/{itemId}/remove")
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_MANAGE')")
    public String removeItem(@PathVariable Integer id,
                             @PathVariable Integer itemId,
                             RedirectAttributes ra) {
        try {
            adminDailyMenuService.removeItem(id, itemId);
            ra.addFlashAttribute("mensaje", "Producto quitado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/daily-menus/" + id;
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_PUBLISH')")
    public String publish(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminDailyMenuService.publish(id, currentUserId());
            ra.addFlashAttribute("mensaje", "Menú publicado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/daily-menus/" + id;
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_MANAGE')")
    public String close(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminDailyMenuService.close(id);
            ra.addFlashAttribute("mensaje", "Menú cerrado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/daily-menus/" + id;
    }

    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAuthority('PERM_DAILY_MENU_MANAGE')")
    public String copy(@PathVariable Integer id,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
                       RedirectAttributes ra) {
        try {
            Integer newId = adminDailyMenuService.copyToDate(id, targetDate);
            ra.addFlashAttribute("mensaje", "Menú copiado como borrador para " + targetDate);
            return "redirect:/admin/daily-menus/" + newId;
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/daily-menus/" + id;
        }
    }
}