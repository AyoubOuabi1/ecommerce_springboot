package com.ayoub.ecommerce.service;

import com.ayoub.ecommerce.Models.Cart;
import com.ayoub.ecommerce.Models.Order;
import com.ayoub.ecommerce.Models.OrderItem;
import com.ayoub.ecommerce.Models.OrderStatus;
import com.ayoub.ecommerce.repository.OrderItemRepository;
import com.ayoub.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;


    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc();
    }

    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findRecentOrders(limit);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            // If status is DELIVERED, mark as paid
            if (status == OrderStatus.DELIVERED) {
                order.setPaid(true);
            }
            orderRepository.save(order);
        });
    }
    @Transactional
    public Order createOrderFromCart(String sessionId, Order orderInfo) {
        Cart cart = cartService.getOrCreateCart(sessionId);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        Order order = new Order();
        order.setSessionId(sessionId);
        order.setUser(cart.getUser());
        order.setFullName(orderInfo.getFullName());
        order.setEmail(orderInfo.getEmail());
        order.setAddress(orderInfo.getAddress());
        order.setPostalCode(orderInfo.getPostalCode());
        order.setCity(orderInfo.getCity());
        order.setCountry(orderInfo.getCountry());
        order.setPhone(orderInfo.getPhone());
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Create order items from cart items
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItemRepository.save(orderItem);
            savedOrder.getOrderItems().add(orderItem);
        });

        cartService.clearCart(sessionId);

        return orderRepository.save(savedOrder);
    }
}