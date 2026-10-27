package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.ProductoMasVendidoDTO;
import com.saboresperu.restaurante.dto.ReporteVentaDTO;
import com.saboresperu.restaurante.service.interfaces.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public String index(Model model) {
        List<ReporteVentaDTO> ventas = reporteService.obtenerVentas();
        List<ProductoMasVendidoDTO> topProductos = reporteService.obtenerProductosMasVendidos();

        model.addAttribute("ventas", ventas);
        model.addAttribute("topProductos", topProductos);
        model.addAttribute("titulo", "Reportes");
        return "admin/reportes";
    }

    @GetMapping("/ventas/excel")
    public ResponseEntity<byte[]> descargarVentasExcel() {
        byte[] excel = reporteService.generarExcelVentas();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-ventas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> descargarProductosExcel() {
        byte[] excel = reporteService.generarExcelProductosMasVendidos();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos-mas-vendidos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}