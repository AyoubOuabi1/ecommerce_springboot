package com.ayoub.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.ayoub.ecommerce.service.CategoryService;
import com.ayoub.ecommerce.service.ProductService;

@Controller
public class HomeController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentCategory", null);
        model.addAttribute("pageTitle", "All Products"); // Add a specific page title
        return "product/list";
    }
}