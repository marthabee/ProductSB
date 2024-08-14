package com.boostmytool.beststore.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

public class ProductDto {

    private Long id; // Add this field for the product ID

    @NotEmpty(message = "The name is required")
    private String name;

    @Min(value = 0, message = "The price must be a positive value")
    private double price;

    private double discountPrice;

    @Size(min = 10, max = 2000, message = "The description should be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "The image file is required")
    private MultipartFile imageFile; // Change to MultipartFile for file handling

    @NotNull(message = "The category is required")
    private Products.Category category;

    @NotNull(message = "The status is required")
    private Products.Status status;
     private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }
    
    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
    

    public Products.Category getCategory() {
        return category;
    }

    public void setCategory(Products.Category category) {
        this.category = category;
    }

    public Products.Status getStatus() {
        return status;
    }

    public void setStatus(Products.Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
