package com.saboresperu.restaurante.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {
    /**
     * @return ruta pública relativa, ej. /media/productos/12_uuid.jpg
     */
    String guardarImagenProducto(Integer productoId, MultipartFile file);

    void eliminarSiEsLocal(String imagenUrl);
}