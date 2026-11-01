package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.config.MediaProperties;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.service.interfaces.MediaStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaStorageServiceImpl implements MediaStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final MediaProperties mediaProperties;

    @Override
    public String guardarImagenProducto(Integer productoId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("No se recibió archivo");
        }
        if (file.getSize() > mediaProperties.getMediaMaxSizeBytes()) {
            throw new BusinessException("La imagen supera el tamaño máximo permitido (5 MB)");
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "img";
        String ext = extension(original);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("Formato no permitido. Usa JPG, PNG, WEBP o GIF");
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("Tipo de contenido no permitido");
        }

        Path dir = Paths.get(mediaProperties.getMediaRoot(), mediaProperties.getMediaProductosDir())
                .toAbsolutePath()
                .normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException("No se pudo crear el directorio de medios");
        }

        String filename = productoId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
                + "." + ext;
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) {
            throw new BusinessException("Ruta de archivo inválida");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error guardando imagen", e);
            throw new BusinessException("Error al guardar la imagen");
        }

        return "/media/" + mediaProperties.getMediaProductosDir() + "/" + filename;
    }

    @Override
    public void eliminarSiEsLocal(String imagenUrl) {
        if (imagenUrl == null || !imagenUrl.startsWith("/media/")) {
            return;
        }
        String relative = imagenUrl.substring("/media/".length());
        Path file = Paths.get(mediaProperties.getMediaRoot()).resolve(relative).normalize();
        Path root = Paths.get(mediaProperties.getMediaRoot()).toAbsolutePath().normalize();
        if (!file.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("No se pudo eliminar archivo {}", file);
        }
    }

    private String extension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) return "";
        return name.substring(i + 1).toLowerCase(Locale.ROOT);
    }
}