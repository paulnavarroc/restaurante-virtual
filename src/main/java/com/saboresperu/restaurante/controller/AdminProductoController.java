package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.ProductoFormDTO;
import com.saboresperu.restaurante.repository.CategoriaRepository;
import com.saboresperu.restaurante.repository.RegionRepository;
import com.saboresperu.restaurante.service.interfaces.ProductoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
public class AdminProductoController {

    private final ProductoAdminService productoAdminService;
    private final CategoriaRepository categoriaRepository;
    private final RegionRepository regionRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoAdminService.listarTodos());
        return "admin/productos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", ProductoFormDTO.builder()
                .activo(true)
                .stockIlimitado(false)
                .build());
        cargarCatalogos(model);
        return "admin/form-producto";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("producto", productoAdminService.obtenerForm(id));
        cargarCatalogos(model);
        return "admin/form-producto";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("producto") ProductoFormDTO form,
                        BindingResult br,
                        Model model,
                        RedirectAttributes ra) {
        if (br.hasErrors()) {
            cargarCatalogos(model);
            return "admin/form-producto";
        }
        try {
            Integer id = productoAdminService.crear(form);
            ra.addFlashAttribute("mensaje", "Producto creado");
            return "redirect:/admin/productos/" + id + "/editar";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            cargarCatalogos(model);
            return "admin/form-producto";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("producto") ProductoFormDTO form,
                             BindingResult br,
                             Model model,
                             RedirectAttributes ra) {
        if (br.hasErrors()) {
            cargarCatalogos(model);
            return "admin/form-producto";
        }
        try {
            productoAdminService.actualizar(id, form);
            ra.addFlashAttribute("mensaje", "Producto actualizado");
            return "redirect:/admin/productos";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            cargarCatalogos(model);
            return "admin/form-producto";
        }
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Integer id, RedirectAttributes ra) {
        productoAdminService.desactivar(id);
        ra.addFlashAttribute("mensaje", "Producto desactivado");
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Integer id, RedirectAttributes ra) {
        productoAdminService.activar(id);
        ra.addFlashAttribute("mensaje", "Producto activado");
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/imagen")
    public String subirImagen(@PathVariable Integer id,
                              @RequestParam("archivo") MultipartFile archivo,
                              RedirectAttributes ra) {
        try {
            productoAdminService.actualizarImagen(id, archivo);
            ra.addFlashAttribute("mensaje", "Imagen actualizada");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/productos/" + id + "/editar";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("categorias", categoriaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("regiones", regionRepository.findByActivoTrueOrderByNombreAsc());
    }
}