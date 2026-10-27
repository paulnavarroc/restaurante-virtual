package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.ProductoMasVendidoDTO;
import com.saboresperu.restaurante.dto.ReporteVentaDTO;
import com.saboresperu.restaurante.entity.DetallePedido;
import com.saboresperu.restaurante.entity.Pedido;
import com.saboresperu.restaurante.repository.PedidoRepository;
import com.saboresperu.restaurante.service.interfaces.ReporteService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final PedidoRepository pedidoRepository;

    @Override
    public List<ReporteVentaDTO> obtenerVentas() {
        return pedidoRepository.findAll().stream()
                .map(p -> ReporteVentaDTO.builder()
                        .pedidoId(p.getId())
                        .fecha(p.getFechaPedido())
                        .estado(p.getEstadoPedido().getNombre())
                        .subtotal(p.getSubtotal())
                        .delivery(p.getCostoDelivery())
                        .descuento(p.getDescuento())
                        .total(p.getTotal())
                        .direccion(p.getDireccion().getDireccionCompleta())
                        .build())
                .sorted(Comparator.comparing(ReporteVentaDTO::getFecha).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoMasVendidoDTO> obtenerProductosMasVendidos() {
        Map<Integer, ProductoMasVendidoDTO> mapa = new HashMap<>();

        for (Pedido pedido : pedidoRepository.findAll()) {
            // Solo contar pedidos no cancelados
            if ("CANCELADO".equals(pedido.getEstadoPedido().getNombre())) continue;

            for (DetallePedido d : pedido.getDetalles()) {
                Integer id = d.getProducto().getId();
                mapa.compute(id, (k, v) -> {
                    if (v == null) {
                        return ProductoMasVendidoDTO.builder()
                                .productoId(id)
                                .nombre(d.getProducto().getNombre())
                                .region(d.getProducto().getRegion().getNombre())
                                .cantidadVendida((long) d.getCantidad())
                                .montoTotal(d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                                .build();
                    }
                    v.setCantidadVendida(v.getCantidadVendida() + d.getCantidad());
                    v.setMontoTotal(v.getMontoTotal().add(
                            d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
                    ));
                    return v;
                });
            }
        }

        return mapa.values().stream()
                .sorted(Comparator.comparing(ProductoMasVendidoDTO::getCantidadVendida).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public byte[] generarExcelVentas() {
        List<ReporteVentaDTO> ventas = obtenerVentas();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ventas");

            // Estilo encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Encabezados
            Row header = sheet.createRow(0);
            String[] columnas = {"ID Pedido", "Fecha", "Estado", "Subtotal", "Delivery", "Descuento", "Total", "Dirección"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowIdx = 1;
            for (ReporteVentaDTO v : ventas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(v.getPedidoId());
                row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().toString() : "");
                row.createCell(2).setCellValue(v.getEstado());
                row.createCell(3).setCellValue(v.getSubtotal().doubleValue());
                row.createCell(4).setCellValue(v.getDelivery().doubleValue());
                row.createCell(5).setCellValue(v.getDescuento().doubleValue());
                row.createCell(6).setCellValue(v.getTotal().doubleValue());
                row.createCell(7).setCellValue(v.getDireccion());
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel de ventas", e);
        }
    }

    @Override
    public byte[] generarExcelProductosMasVendidos() {
        List<ProductoMasVendidoDTO> productos = obtenerProductosMasVendidos();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Productos más vendidos");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row header = sheet.createRow(0);
            String[] columnas = {"ID", "Producto", "Región", "Cantidad vendida", "Monto total (S/)"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ProductoMasVendidoDTO p : productos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getProductoId());
                row.createCell(1).setCellValue(p.getNombre());
                row.createCell(2).setCellValue(p.getRegion());
                row.createCell(3).setCellValue(p.getCantidadVendida());
                row.createCell(4).setCellValue(p.getMontoTotal().doubleValue());
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel de productos", e);
        }
    }
}