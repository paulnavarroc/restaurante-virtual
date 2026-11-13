package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.config.MediaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageStorageService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final MediaProperties mediaProperties;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > mediaProperties.getMediaMaxSizeBytes()) {
            throw new IllegalArgumentException("La imagen supera el tamaño máximo permitido");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato no permitido (usa JPG, PNG, WEBP o GIF)");
        }

        String ext = extension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Paths.get(mediaProperties.getMediaRoot())
                .toAbsolutePath()
                .normalize()
                .resolve(mediaProperties.getMediaProductosDir());
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen", e);
        }

        return "/media/" + mediaProperties.getMediaProductosDir() + "/" + filename;
    }

    private static String extension(String original, String contentType) {
        if (original != null) {
            int i = original.lastIndexOf('.');
            if (i >= 0 && i < original.length() - 1) {
                String e = original.substring(i).toLowerCase();
                if (e.matches("\\.(jpe?g|png|webp|gif)")) {
                    return e.equals(".jpeg") ? ".jpg" : e;
                }
            }
        }
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}