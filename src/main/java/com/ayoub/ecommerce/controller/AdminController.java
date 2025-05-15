package com.ayoub.ecommerce.controller;

import com.ayoub.ecommerce.Models.Category;
import com.ayoub.ecommerce.Models.Order;
import com.ayoub.ecommerce.Models.OrderStatus;
import com.ayoub.ecommerce.Models.Product;
import com.ayoub.ecommerce.service.CategoryService;
import com.ayoub.ecommerce.service.OrderService;
import com.ayoub.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CategoryService categoryService;
    private final ProductService productService;

    private final OrderService orderService;

    @Autowired
    public AdminController(CategoryService categoryService,
                          ProductService productService,
                          OrderService orderService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Product> products = productService.getAllProducts();
        List<Category> categories = categoryService.getAllCategories();
        List<Order> orders = orderService.getAllOrders();

        model.addAttribute("productCount", products.size());
        model.addAttribute("categoryCount", categories.size());
        model.addAttribute("ordersCount", orders.size());
        BigDecimal totalRevenue = orders.stream()
                .filter(Order::isPaid)
                .map(order -> order.getTotal() != null ? order.getTotal() : order.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalRevenue", totalRevenue);

        List<Product> recentProducts = products.stream()
                .sorted(Comparator.comparing(Product::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentProducts", recentProducts);

        List<Order> recentOrders = orders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentOrders", recentOrders);

        return "admin/index";
    }

    // Category Management
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories/list";
    }

    @GetMapping("/categories/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @GetMapping("/categories/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id).orElse(new Category());
        model.addAttribute("category", category);
        return "admin/categories/form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute Category category,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }

        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("message", "Category saved successfully");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("message", "Category deleted successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Cannot delete category. It may have products.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/categories";
    }

    // Product Management
    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/products/list";
    }

    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/form";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute Product product,
                             BindingResult bindingResult,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/products/form";
        }

        try {
            productService.saveProduct(product, imageFile);
            redirectAttributes.addFlashAttribute("message", "Product saved successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Failed to upload image: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Product deleted successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to delete product: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String listOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", "orders");
        return "admin/orders/list";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);
        model.addAttribute("currentPage", "orders");
        return "admin/orders/detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, OrderStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("message", "Order status updated successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to update order status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/mark-paid")
    public String markOrderAsPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            order.setPaid(true);
            orderService.saveOrder(order);

            redirectAttributes.addFlashAttribute("message", "Order marked as paid successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to update payment status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/reports")
    public String showReports(Model model) {
        List<Order> orders = orderService.getAllOrders();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        if (!orders.isEmpty()) {
            totalRevenue = orders.stream()
                    .filter(Order::isPaid)
                    .map(order -> order.getTotal() != null ? order.getTotal() : order.getTotalPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Add recent orders (last 10)
        List<Order> recentOrders = orders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("ordersCount", orders.size());
        model.addAttribute("recentOrders", recentOrders);

        return "admin/reports/index";
    }
}