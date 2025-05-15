package com.ayoub.ecommerce.controller;

import com.ayoub.ecommerce.Models.Cart;
import com.ayoub.ecommerce.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        String sessionId = getOrCreateSessionId(session);
        Cart cart = cartService.getOrCreateCart(sessionId);
        model.addAttribute("cart", cart);
        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {

        String sessionId = getOrCreateSessionId(session);
        cartService.addItemToCart(productId, quantity, sessionId);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCartItem(
            @RequestParam Long itemId,
            @RequestParam int quantity,
            HttpSession session) {

        String sessionId = getOrCreateSessionId(session);
        cartService.updateCartItem(itemId, quantity, sessionId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        String sessionId = getOrCreateSessionId(session);
        cartService.clearCart(sessionId);
        return "redirect:/cart";
    }

    private String getOrCreateSessionId(HttpSession session) {
        String sessionId = (String) session.getAttribute("cartSessionId");
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("cartSessionId", sessionId);
        }
        return sessionId;
    }
}