package com.saboresperu.restaurante.util;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestInfoUtils {

    private RequestInfoUtils() {}

    public static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    public static String userAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return null;
        return ua.length() > 500 ? ua.substring(0, 500) : ua;
    }

    public static String navegador(String ua) {
        if (ua == null) return "Desconocido";
        String u = ua.toLowerCase();
        if (u.contains("edg/")) return "Edge";
        if (u.contains("chrome/") && !u.contains("edg/")) return "Chrome";
        if (u.contains("firefox/")) return "Firefox";
        if (u.contains("safari/") && !u.contains("chrome/")) return "Safari";
        if (u.contains("opera") || u.contains("opr/")) return "Opera";
        return "Otro";
    }

    public static String sistemaOperativo(String ua) {
        if (ua == null) return "Desconocido";
        String u = ua.toLowerCase();
        if (u.contains("windows")) return "Windows";
        if (u.contains("android")) return "Android";
        if (u.contains("iphone") || u.contains("ipad")) return "iOS";
        if (u.contains("mac os")) return "macOS";
        if (u.contains("linux")) return "Linux";
        return "Otro";
    }

    public static String dispositivo(String ua) {
        if (ua == null) return "Desconocido";
        String u = ua.toLowerCase();
        if (u.contains("mobile") || u.contains("android") || u.contains("iphone")) return "Mobile";
        if (u.contains("tablet") || u.contains("ipad")) return "Tablet";
        return "Desktop";
    }
}