package com.saboresperu.restaurante.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class MediaProperties {
    private String mediaRoot = "./uploads";
    private String mediaProductosDir = "productos";
    private long mediaMaxSizeBytes = 5_242_880L;
}