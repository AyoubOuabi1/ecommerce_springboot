package com.ayoub.ecommerce.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Product product;

    private BigDecimal price;
    private int quantity;

    public BigDecimal getCost() {
        return price.multiply(new BigDecimal(quantity));
    }

    // Getters, setters, constructors
}
