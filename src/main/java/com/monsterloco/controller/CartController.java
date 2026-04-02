package com.monsterloco.controller;

import com.monsterloco.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartService.CartSummary> getCart() {
        return ResponseEntity.ok(cartService.getCartSummary());
    }

    @PostMapping("/{drinkId}")
    public ResponseEntity<?> addToCart(
            @PathVariable Long drinkId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            CartService.CartItemWithPrice item = cartService.addToCart(drinkId, quantity);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        try {
            CartService.CartItemWithPrice item = cartService.updateQuantity(cartItemId, quantity);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId) {
        try {
            cartService.removeFromCart(cartItemId);
            return ResponseEntity.ok(new SuccessResponse("Item removed from cart"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(new SuccessResponse("Cart cleared"));
    }

    @GetMapping("/contains/{drinkId}")
    public ResponseEntity<Boolean> isInCart(@PathVariable Long drinkId) {
        return ResponseEntity.ok(cartService.isInCart(drinkId));
    }

    public record ErrorResponse(String error) {}

    public record SuccessResponse(String message) {}
}
