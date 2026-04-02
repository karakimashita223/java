package com.monsterloco.service;

import com.monsterloco.model.CartItem;
import com.monsterloco.model.MonsterDrink;
import com.monsterloco.model.PriceHistory;
import com.monsterloco.repository.CartItemRepository;
import com.monsterloco.repository.MonsterDrinkRepository;
import com.monsterloco.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final MonsterDrinkRepository drinkRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public List<CartItemWithPrice> getAllCartItems() {
        List<CartItem> items = cartItemRepository.findAllByOrderByCreatedAtDesc();
        return items.stream()
                .map(item -> {
                    Optional<PriceHistory> latestPrice = priceHistoryRepository.findLatestPriceByDrinkId(item.getDrink().getId());
                    Double price = latestPrice.map(PriceHistory::getPrice).orElse(null);
                    Double subtotal = price != null ? price * item.getQuantity() : null;
                    return new CartItemWithPrice(item, price, subtotal);
                })
                .toList();
    }

    public CartSummary getCartSummary() {
        List<CartItemWithPrice> items = getAllCartItems();
        int totalItems = items.stream()
                .mapToInt(item -> item.cartItem().getQuantity())
                .sum();
        Double totalPrice = items.stream()
                .filter(item -> item.subtotal() != null)
                .mapToDouble(CartItemWithPrice::subtotal)
                .sum();
        return new CartSummary(items, totalItems, totalPrice);
    }

    @Transactional
    public CartItemWithPrice addToCart(Long drinkId, Integer quantity) {
        MonsterDrink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new IllegalArgumentException("Drink not found with id: " + drinkId));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByDrinkId(drinkId);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity if already in cart
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                    .drink(drink)
                    .quantity(quantity)
                    .build();
        }

        cartItem = cartItemRepository.save(cartItem);

        Optional<PriceHistory> latestPrice = priceHistoryRepository.findLatestPriceByDrinkId(drinkId);
        Double price = latestPrice.map(PriceHistory::getPrice).orElse(null);
        Double subtotal = price != null ? price * cartItem.getQuantity() : null;

        return new CartItemWithPrice(cartItem, price, subtotal);
    }

    @Transactional
    public CartItemWithPrice updateQuantity(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + cartItemId));

        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);

        Optional<PriceHistory> latestPrice = priceHistoryRepository.findLatestPriceByDrinkId(cartItem.getDrink().getId());
        Double price = latestPrice.map(PriceHistory::getPrice).orElse(null);
        Double subtotal = price != null ? price * cartItem.getQuantity() : null;

        return new CartItemWithPrice(cartItem, price, subtotal);
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new IllegalArgumentException("Cart item not found with id: " + cartItemId);
        }
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart() {
        cartItemRepository.deleteAll();
    }

    public boolean isInCart(Long drinkId) {
        return cartItemRepository.existsByDrinkId(drinkId);
    }

    public record CartItemWithPrice(
            CartItem cartItem,
            Double unitPrice,
            Double subtotal
    ) {}

    public record CartSummary(
            List<CartItemWithPrice> items,
            int totalItems,
            Double totalPrice
    ) {}
}
