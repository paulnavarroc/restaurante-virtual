package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ProductoMasVendidoDTO;
import com.saboresperu.restaurante.dto.ReporteVentaDTO;
import java.util.List;

public interface ReporteService {
    List<ReporteVentaDTO> obtenerVentas();
    List<ProductoMasVendidoDTO> obtenerProductosMasVendidos();
    byte[] generarExcelVentas();
    byte[] generarExcelProductosMasVendidos();
}