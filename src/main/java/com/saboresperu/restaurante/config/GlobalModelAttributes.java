package com.saboresperu.restaurante.config;

import com.saboresperu.restaurante.domain.SecuritySessionKeys;
import com.saboresperu.restaurante.dto.MenuItemDto;
import com.saboresperu.restaurante.service.interfaces.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final CartService cartService;

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        return (uri == null || uri.isEmpty()) ? "/" : uri;
    }

    @ModelAttribute("adminMenu")
    public List<MenuItemDto> adminMenu(HttpSession session) {
        return filterMenu(session, "ADMIN", "OPERATIONS");
    }

    @ModelAttribute("kitchenMenu")
    public List<MenuItemDto> kitchenMenu(HttpSession session) {
        return filterMenu(session, "KITCHEN");
    }

    @ModelAttribute("courierMenu")
    public List<MenuItemDto> courierMenu(HttpSession session) {
        return filterMenu(session, "COURIER");
    }

    @ModelAttribute("customerMenu")
    public List<MenuItemDto> customerMenu(HttpSession session) {
        return filterMenu(session, "CUSTOMER");
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        if (session == null) return 0;
        return cartService.countItems(session);
    }

    @SuppressWarnings("unchecked")
    private List<MenuItemDto> filterMenu(HttpSession session, String... contexts) {
        if (session == null) return List.of();
        Object raw = session.getAttribute(SecuritySessionKeys.MENU_ITEMS);
        if (!(raw instanceof List<?> list)) return List.of();
        return list.stream()
                .filter(MenuItemDto.class::isInstance)
                .map(MenuItemDto.class::cast)
                .filter(m -> {
                    for (String c : contexts) {
                        if (c.equals(m.context())) return true;
                    }
                    return false;
                })
                .toList();
    }
}