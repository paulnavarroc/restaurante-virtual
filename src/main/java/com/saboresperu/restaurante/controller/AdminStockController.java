package com.saboresperu.restaurante.controller.admin;

import com.saboresperu.restaurante.dto.StockAdjustRequest;
import com.saboresperu.restaurante.dto.StockEntryRequest;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.BranchRepository;
import com.saboresperu.restaurante.repository.IngredientRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.AdminStockService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/stock")
@RequiredArgsConstructor
public class AdminStockController {

    private final AdminStockService adminStockService;
    private final BranchRepository branchRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private Integer currentUserId() {
        return userRepository.findByEmail(securityUtils.currentEmail())
                .map(User::getId)
                .orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_STOCK_VIEW')")
    public String list(@RequestParam(required = false) Integer branchId, Model model) {
        model.addAttribute("titulo", "Stock");
        model.addAttribute("rows", adminStockService.listByBranch(branchId));
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        model.addAttribute("branchId", branchId);
        return "admin/stock/list";
    }

    @GetMapping("/entry")
    @PreAuthorize("hasAuthority('PERM_STOCK_ENTRY')")
    public String entryForm(Model model) {
        model.addAttribute("titulo", "Entrada de stock");
        model.addAttribute("form", new StockEntryRequest(null, null, null, null, null));
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        model.addAttribute("ingredients", ingredientRepository.findByIsActiveTrueOrderByNameAsc());
        return "admin/stock/entry";
    }

    @PostMapping("/entry")
    @PreAuthorize("hasAuthority('PERM_STOCK_ENTRY')")
    public String entrySubmit(@Valid @ModelAttribute("form") StockEntryRequest form,
                              BindingResult bindingResult,
                              RedirectAttributes ra,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Entrada de stock");
            model.addAttribute("branches", branchRepository.findByIsActiveTrue());
            model.addAttribute("ingredients", ingredientRepository.findByIsActiveTrueOrderByNameAsc());
            return "admin/stock/entry";
        }
        try {
            adminStockService.registerEntry(form, currentUserId());
            ra.addFlashAttribute("mensaje", "Entrada registrada");
            return "redirect:/admin/stock?branchId=" + form.branchId();
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Entrada de stock");
            model.addAttribute("branches", branchRepository.findByIsActiveTrue());
            model.addAttribute("ingredients", ingredientRepository.findByIsActiveTrueOrderByNameAsc());
            return "admin/stock/entry";
        }
    }

    @GetMapping("/adjust")
    @PreAuthorize("hasAuthority('PERM_STOCK_ADJUST')")
    public String adjustForm(@RequestParam Integer branchId,
                             @RequestParam Integer ingredientId,
                             Model model) {
        model.addAttribute("titulo", "Ajuste de stock");
        model.addAttribute("form", new StockAdjustRequest(branchId, ingredientId, null, null));
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        model.addAttribute("ingredients", ingredientRepository.findByIsActiveTrueOrderByNameAsc());
        return "admin/stock/adjust";
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('PERM_STOCK_ADJUST')")
    public String adjustSubmit(@Valid @ModelAttribute("form") StockAdjustRequest form,
                               BindingResult bindingResult,
                               RedirectAttributes ra,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Ajuste de stock");
            model.addAttribute("branches", branchRepository.findByIsActiveTrue());
            model.addAttribute("ingredients", ingredientRepository.findByIsActiveTrueOrderByNameAsc());
            return "admin/stock/adjust";
        }
        try {
            adminStockService.adjust(form, currentUserId());
            ra.addFlashAttribute("mensaje", "Ajuste registrado");
            return "redirect:/admin/stock?branchId=" + form.branchId();
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Ajuste de stock");
            model.addAttribute("branches", branchRepository.findByIsActiveTrue());
            model.addAttribute("ingredients", ingredientRepository.findByIsActiveTrueOrderByNameAsc());
            return "admin/stock/adjust";
        }
    }
}