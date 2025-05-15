package com.ayoub.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${upload.path:src/main/resources/static/images/products}")
    private String uploadPath;

    public String uploadProductImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        // Create directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Generate unique filename
        String uuid = UUID.randomUUID().toString();
        String originalFilename = imageFile.getOriginalFilename();
        String fileName = uuid + "_" + (originalFilename != null ? originalFilename : "image.jpg");

        // Save file
        Path destinationPath = Paths.get(uploadPath, fileName);
        Files.write(destinationPath, imageFile.getBytes());

        // Return the URL path for the image
        return "/images/products/" + fileName;
    }

    public boolean deleteProductImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }

        try {
            // Extract filename from the URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadPath, fileName);

            // Delete the file if it exists
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}