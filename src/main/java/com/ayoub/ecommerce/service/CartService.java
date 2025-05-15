package com.ayoub.ecommerce.service;

import com.ayoub.ecommerce.Models.Cart;
import com.ayoub.ecommerce.Models.CartItem;
import com.ayoub.ecommerce.Models.Product;
import com.ayoub.ecommerce.repository.CartItemRepository;
import com.ayoub.ecommerce.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public Cart getOrCreateCart(String sessionId) {
        Optional<Cart> existingCart = cartRepository.findBySessionId(sessionId);

        if (existingCart.isPresent()) {
            return existingCart.get();
        } else {
            Cart newCart = new Cart();
            newCart.setSessionId(sessionId);
            newCart.setCreatedAt(LocalDateTime.now());
            return cartRepository.save(newCart);
        }
    }

    @Transactional
    public Cart addItemToCart(Long productId, int quantity, String sessionId) {
        Cart cart = getOrCreateCart(sessionId);
        Optional<Product> productOpt = Optional.ofNullable(productService.getProductById(productId));

        if (productOpt.isPresent() && productOpt.get().isAvailable()) {
            Product product = productOpt.get();

            // Check if product already in cart
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Update quantity
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                cartItemRepository.save(item);
            } else {
                // Add new item
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                cart.getItems().add(newItem);
                cartItemRepository.save(newItem);
            }

            return cartRepository.save(cart);
        }

        return cart;
    }

    @Transactional
    public Cart updateCartItem(Long itemId, int quantity, String sessionId) {
        Cart cart = getOrCreateCart(sessionId);

        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            cart.getItems().removeIf(item -> item.getId().equals(itemId));
            cartItemRepository.deleteById(itemId);
        } else {
            // Update quantity
            cart.getItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setQuantity(quantity);
                        cartItemRepository.save(item);
                    });
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String sessionId) {
        Optional<Cart> cartOpt = cartRepository.findBySessionId(sessionId);
        cartOpt.ifPresent(cart -> {
            cartItemRepository.deleteByCart_Id(cart.getId());
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}