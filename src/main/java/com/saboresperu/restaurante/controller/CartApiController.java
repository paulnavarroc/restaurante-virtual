package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.AddToCartRequest;
import com.saboresperu.restaurante.dto.CartApiResponse;
import com.saboresperu.restaurante.dto.CartDto;
import com.saboresperu.restaurante.dto.CartItemDto;
import com.saboresperu.restaurante.service.interfaces.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    @GetMapping
    public CartApiResponse get(HttpSession session) {
        return toResponse(true, null, session);
    }

    @PostMapping("/items")
    public ResponseEntity<CartApiResponse> add(@Valid @RequestBody AddToCartRequest request,
                                               HttpSession session) {
        try {
            cartService.addItem(session, request.productId(), request.dailyMenuItemId(), request.quantity());
            return ResponseEntity.ok(toResponse(true, "Producto agregado", session));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(toResponse(false, ex.getMessage(), session));
        }
    }

    @PostMapping("/items/update")
    public ResponseEntity<CartApiResponse> update(@RequestBody Map<String, Object> body,
                                                  HttpSession session) {
        try {
            Integer productId = toInt(body.get("productId"));
            Integer dailyMenuItemId = body.get("dailyMenuItemId") != null
                    ? toInt(body.get("dailyMenuItemId")) : null;
            int quantity = body.get("quantity") != null ? toInt(body.get("quantity")) : 0;
            cartService.updateQuantity(session, productId, dailyMenuItemId, quantity);
            return ResponseEntity.ok(toResponse(true, "Carrito actualizado", session));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(toResponse(false, ex.getMessage(), session));
        }
    }

    @PostMapping("/items/remove")
    public ResponseEntity<CartApiResponse> remove(@RequestBody Map<String, Object> body,
                                                  HttpSession session) {
        Integer productId = toInt(body.get("productId"));
        Integer dailyMenuItemId = body.get("dailyMenuItemId") != null
                ? toInt(body.get("dailyMenuItemId")) : null;
        cartService.removeItem(session, productId, dailyMenuItemId);
        return ResponseEntity.ok(toResponse(true, "Producto eliminado", session));
    }

    private CartApiResponse toResponse(boolean ok, String message, HttpSession session) {
        CartDto cart = cartService.getCart(session);
        var lines = cart.getItems().stream()
                .map(i -> new CartApiResponse.CartLineQtyDto(
                        i.getProductId(),
                        i.getDailyMenuItemId(),
                        i.getQuantity()))
                .toList();
        return new CartApiResponse(ok, message, cart.getTotalQuantity(), lines);
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        return Integer.valueOf(o.toString());
    }
}