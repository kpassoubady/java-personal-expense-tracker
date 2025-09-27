package com.expensetracker.app.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Expense entity.
 * 
 * This DTO provides a flatter structure with categoryId field
 * as requested, which is useful for form submissions and API responses.
 */
public class ExpenseDTO {

    private Long id;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String categoryName; // For display purposes

    // Default constructor
    public ExpenseDTO() {
    }

    // Constructor with all fields
    public ExpenseDTO(Long id, String description, BigDecimal amount, 
                     LocalDate expenseDate, Long categoryId, String categoryName) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "ExpenseDTO{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", expenseDate=" + expenseDate +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}