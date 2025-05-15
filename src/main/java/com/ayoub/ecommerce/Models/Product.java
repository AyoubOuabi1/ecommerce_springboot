package com.ayoub.ecommerce.Models;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String slug;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private boolean available;
    @ManyToOne
    private Category category;

}

