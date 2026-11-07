package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.RecipeItemFormDto;
import com.saboresperu.restaurante.repository.IngredientRepository;
import com.saboresperu.restaurante.service.interfaces.AdminRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/products/{productId}/recipe")
@RequiredArgsConstructor
public class AdminRecipeController {

    private final AdminRecipeService adminRecipeService;
    private final IngredientRepository ingredientRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_RECIPES_MANAGE')")
    public String edit(@PathVariable Integer productId, Model model) {
        model.addAttribute("titulo", "Receta");
        model.addAttribute("recipe", adminRecipeService.getByProductId(productId));
        model.addAttribute("items", adminRecipeService.listItems(productId));
        model.addAttribute("ingredients",
                ingredientRepository.findByIsActiveTrueOrderByNameAsc());
        return "admin/products/recipe";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_RECIPES_MANAGE')")
    public String save(@PathVariable Integer productId,
                       @RequestParam List<Integer> ingredientId,
                       @RequestParam List<BigDecimal> quantity,
                       RedirectAttributes ra) {
        try {
            List<RecipeItemFormDto> items = new ArrayList<>();
            for (int i = 0; i < ingredientId.size(); i++) {
                if (ingredientId.get(i) == null) continue;
                BigDecimal q = quantity.get(i);
                if (q == null || q.compareTo(BigDecimal.ZERO) <= 0) continue;
                items.add(new RecipeItemFormDto(ingredientId.get(i), q));
            }
            adminRecipeService.saveRecipe(productId, items);
            ra.addFlashAttribute("mensaje", "Receta guardada");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/recipe";
    }
}