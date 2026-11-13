package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CartDto;
import com.saboresperu.restaurante.dto.CartSanitizeResult;
import jakarta.servlet.http.HttpSession;

public interface CartService {
    CartDto getCart(HttpSession session);
    void addItem(HttpSession session, Integer productId, Integer dailyMenuItemId, int quantity);
    void updateQuantity(HttpSession session, Integer productId, Integer dailyMenuItemId, int quantity);
    void removeItem(HttpSession session, Integer productId, Integer dailyMenuItemId);
    void clear(HttpSession session);
    int countItems(HttpSession session);
    CartSanitizeResult mergeAndLoadForCustomer(HttpSession session, Integer customerId, Integer branchId);
    CartSanitizeResult refreshForCustomer(HttpSession session, Integer customerId, Integer branchId);
    void persistSessionCart(Integer customerId, HttpSession session);
    void clearPersisted(Integer customerId, HttpSession session);
}