package com.expensetracker.app.validation;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.service.CategoryService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validator implementation for UniqueCategoryName annotation.
 * Validates that a category name is unique across all categories.
 */
@Component
public class UniqueCategoryNameValidator implements ConstraintValidator<UniqueCategoryName, Category> {

    @Autowired
    private CategoryService categoryService;

    @Override
    public void initialize(UniqueCategoryName constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Category category, ConstraintValidatorContext context) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        // If categoryService is not available (e.g., during application startup), skip validation
        if (categoryService == null) {
            return true;
        }

        try {
            // Check if category name already exists
            boolean exists = categoryService.existsByName(category.getName());
            
            // If updating an existing category, allow the same name for the same ID
            if (exists && category.getId() != null) {
                Optional<Category> existingCategory = categoryService.findByName(category.getName());
                if (existingCategory.isPresent() && existingCategory.get().getId().equals(category.getId())) {
                    return true; // Same category, name unchanged
                }
            }
            
            return !exists;
        } catch (Exception e) {
            // If there's an exception (e.g., during startup), allow the validation to pass
            return true;
        }
    }
}