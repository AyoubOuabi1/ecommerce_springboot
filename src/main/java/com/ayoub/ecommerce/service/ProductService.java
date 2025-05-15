package com.ayoub.ecommerce.service;

import com.ayoub.ecommerce.Models.Product;
import com.ayoub.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final FileUploadService fileUploadService;

    @Autowired
    public ProductService(ProductRepository productRepository, FileUploadService fileUploadService) {
        this.productRepository = productRepository;
        this.fileUploadService = fileUploadService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    public Product saveProduct(Product product, MultipartFile imageFile) throws IOException {
        // Handle image upload if file is provided
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if updating a product with an existing image
            if (product.getId() != null && product.getImageUrl() != null) {
                fileUploadService.deleteProductImage(product.getImageUrl());
            }

            // Upload new image and set URL
            String imageUrl = fileUploadService.uploadProductImage(imageFile);
            product.setImageUrl(imageUrl);
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        if (product != null && product.getImageUrl() != null) {
            fileUploadService.deleteProductImage(product.getImageUrl());
        }
        productRepository.deleteById(id);
    }



    public List<Product> getAvailableProducts() {
        return productRepository.findByAvailable(true);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_Id(categoryId);
    }


    public List<Product> searchProducts(String query) {
        String searchTerm = "%" + query.toLowerCase() + "%";
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query);
    }
}