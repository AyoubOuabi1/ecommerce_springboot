package com.ayoub.ecommerce.controller;

import com.ayoub.ecommerce.Models.Cart;
import com.ayoub.ecommerce.Models.Order;
import com.ayoub.ecommerce.service.CartService;
import com.ayoub.ecommerce.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping
    public String checkout(HttpSession session, Model model) {
        String sessionId = (String) session.getAttribute("cartSessionId");
        if (sessionId == null) {
            return "redirect:/cart";
        }

        Cart cart = cartService.getOrCreateCart(sessionId);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("order", new Order());
        return "checkout/form";
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @ModelAttribute Order order,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        String sessionId = (String) session.getAttribute("cartSessionId");
        if (sessionId == null) {
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            Cart cart = cartService.getOrCreateCart(sessionId);
            model.addAttribute("cart", cart);
            return "checkout/form";
        }

        try {
            Order createdOrder = orderService.createOrderFromCart(sessionId, order);
            return "redirect:/orders/confirmation/" + createdOrder.getId();
        } catch (IllegalStateException e) {
            return "redirect:/cart";
        }
    }
}