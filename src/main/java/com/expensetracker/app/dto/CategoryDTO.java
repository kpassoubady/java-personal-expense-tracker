package com.expensetracker.app.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Category entity.
 * 
 * This DTO provides a clean interface for category data transfer
 * between the client and server, without exposing entity internals.
 */
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex color code")
    private String color = "#007bff";

    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon = "fas fa-tag";

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // For summary information
    private int expenseCount;

    // Default constructor
    public CategoryDTO() {
    }

    // Constructor with main fields
    public CategoryDTO(String name, String description, String color, String icon) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.icon = icon;
    }

    // Constructor with all fields including metadata
    public CategoryDTO(Long id, String name, String description, String color, String icon, 
                      LocalDateTime createdAt, LocalDateTime updatedAt, int expenseCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.icon = icon;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expenseCount = expenseCount;
    }

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(int expenseCount) {
        this.expenseCount = expenseCount;
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", icon='" + icon + '\'' +
                ", description='" + description + '\'' +
                ", expenseCount=" + expenseCount +
                '}';
    }
}