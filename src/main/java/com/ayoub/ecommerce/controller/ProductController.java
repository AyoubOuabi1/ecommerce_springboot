package com.ayoub.ecommerce.controller;

import com.ayoub.ecommerce.Models.Category;
import com.ayoub.ecommerce.Models.Product;
import com.ayoub.ecommerce.service.CategoryService;
import com.ayoub.ecommerce.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/product")
    public String listProducts(Model model, @RequestParam(required = false) String q) {
        List<Product> products;
        if (q != null && !q.isEmpty()) {
            products = productService.searchProducts(q);
            model.addAttribute("pageTitle", "Search Results for '" + q + "'");
        } else {
            products = productService.getAvailableProducts();
            model.addAttribute("pageTitle", "All Products");
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentCategory", null);
        return "product/list";
    }

    @GetMapping("/product/category/{id}")
    public String getProductsByCategory(@PathVariable Long id, Model model) {
        Optional<Category> categoryOptional = categoryService.getCategoryById(id);
        List<Product> products = productService.getProductsByCategory(id);
        Category category = categoryOptional.orElse(null);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentCategory", category);
        model.addAttribute("pageTitle", category != null ? category.getName() : "Products");
        return "product/list";
    }

    @GetMapping("/product/{slug}")
    public String getProductDetail(@PathVariable String slug, Model model) {
        Product product = productService.getProductBySlug(slug);
        if (product == null) {
            return "redirect:/product";
        }

        // Get related products (same category)
        List<Product> relatedProducts = productService.getProductsByCategory(product.getCategory().getId())
                .stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .limit(4)
                .collect(Collectors.toList());

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        return "product/detail";
    }
}