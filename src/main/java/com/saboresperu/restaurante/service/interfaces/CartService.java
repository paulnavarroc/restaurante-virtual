package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CartDto;
import jakarta.servlet.http.HttpSession;

public interface CartService {
    CartDto getCart(HttpSession session);
    void addItem(HttpSession session, Integer productId, Integer dailyMenuItemId, int quantity);
    void updateQuantity(HttpSession session, Integer productId, Integer dailyMenuItemId, int quantity);
    void removeItem(HttpSession session, Integer productId, Integer dailyMenuItemId);
    void clear(HttpSession session);
    int countItems(HttpSession session);
}