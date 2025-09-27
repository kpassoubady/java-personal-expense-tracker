package com.expensetracker.app.service;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.exception.EntityNotFoundException;
import com.expensetracker.app.exception.ValidationException;
import com.expensetracker.app.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing categories.
 * 
 * Provides business logic for CRUD operations on categories,
 * including validation and data integrity checks.
 */
@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Get all categories ordered by name.
     * 
     * @return List of all categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    /**
     * Find category by ID.
     * 
     * @param id the category ID
     * @return Optional containing the category if found
     * @throws ValidationException if id is null
     */
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        if (id == null) {
            throw new ValidationException("Category ID cannot be null");
        }
        return categoryRepository.findById(id);
    }

    /**
     * Get category by ID or throw exception if not found.
     * 
     * @param id the category ID
     * @return the category
     * @throws EntityNotFoundException if category not found
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category", id));
    }

    /**
     * Find category by name (case-insensitive).
     * 
     * @param name the category name
     * @return Optional containing the category if found
     */
    @Transactional(readOnly = true)
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name);
    }

    /**
     * Save a new category or update an existing one.
     * 
     * @param category the category to save
     * @return the saved category
     * @throws ValidationException if validation fails
     */
    public Category saveCategory(Category category) {
        validateCategory(category);
        
        // Check for duplicate names (case-insensitive)
        if (category.getId() == null) { // New category
            Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCase(category.getName());
            if (existingCategory.isPresent()) {
                throw new ValidationException("name", category.getName(), 
                    "Category with this name already exists");
            }
        } else { // Updating existing category
            Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCase(category.getName());
            if (existingCategory.isPresent() && !existingCategory.get().getId().equals(category.getId())) {
                throw new ValidationException("name", category.getName(), 
                    "Category with this name already exists");
            }
        }
        
        try {
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new ValidationException("Failed to save category: " + e.getMessage(), e);
        }
    }

    /**
     * Comprehensive category validation.
     * 
     * @param category the category to validate
     * @throws ValidationException if validation fails
     */
    private void validateCategory(Category category) {
        if (category == null) {
            throw new ValidationException("Category cannot be null");
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new ValidationException("name", category.getName(), 
                "Category name is required and cannot be empty");
        }
        
        if (category.getName().length() > 100) {
            throw new ValidationException("name", category.getName(), 
                "Category name must not exceed 100 characters");
        }
        
        // Validate color format if provided
        if (category.getColor() != null && !category.getColor().matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
            throw new ValidationException("color", category.getColor(), 
                "Color must be a valid hex color code (e.g., #FF0000 or #F00)");
        }
        
        // Validate icon length if provided
        if (category.getIcon() != null && category.getIcon().length() > 50) {
            throw new ValidationException("icon", category.getIcon(), 
                "Icon must not exceed 50 characters");
        }
        
        // Validate description length if provided
        if (category.getDescription() != null && category.getDescription().length() > 255) {
            throw new ValidationException("description", category.getDescription(), 
                "Description must not exceed 255 characters");
        }
    }

    /**
     * Delete a category by ID.
     * 
     * @param id the category ID to delete
     * @throws EntityNotFoundException if category not found
     * @throws ValidationException if category has associated expenses or id is null
     */
    public void deleteCategory(Long id) {
        if (id == null) {
            throw new ValidationException("Category ID cannot be null for deletion");
        }
        
        Category category = getCategoryById(id);
        
        // Check if category has expenses
        if (category.hasExpenses()) {
            throw new ValidationException("category", category.getName(), 
                "Cannot delete category because it has associated expenses. " +
                "Please reassign or delete the expenses first.");
        }
        
        try {
            categoryRepository.deleteById(id);
        } catch (Exception e) {
            throw new ValidationException("Failed to delete category with ID " + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Force delete a category and reassign its expenses to another category.
     * 
     * @param categoryId the category ID to delete
     * @param replacementCategoryId the category ID to reassign expenses to
     * @throws EntityNotFoundException if either category not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    public void deleteCategoryWithReassignment(Long categoryId, Long replacementCategoryId) {
        if (categoryId == null || replacementCategoryId == null) {
            throw new ValidationException("Category IDs cannot be null");
        }
        
        if (categoryId.equals(replacementCategoryId)) {
            throw new ValidationException("Cannot reassign expenses to the same category being deleted");
        }
        
        Category categoryToDelete = getCategoryById(categoryId);
        Category replacementCategory = getCategoryById(replacementCategoryId);
        
        // Reassign all expenses to the replacement category
        if (categoryToDelete.hasExpenses()) {
            categoryToDelete.getExpenses().forEach(expense -> {
                expense.setCategory(replacementCategory);
            });
        }
        
        try {
            categoryRepository.deleteById(categoryId);
        } catch (Exception e) {
            throw new ValidationException("Failed to delete category with reassignment: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a category exists by name.
     * 
     * @param name the category name to check
     * @return true if category exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean categoryExists(String name) {
        return categoryRepository.findByNameIgnoreCase(name).isPresent();
    }

    /**
     * Get category statistics for reporting.
     *
     * @return List of arrays containing category statistics [name, expenseCount, totalAmount]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCategoryStatistics() {
        return categoryRepository.getCategoryStatistics();
    }

    /**
     * Create default categories if none exist.
     */
    public void createDefaultCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = Arrays.asList(
                new Category("Food & Dining", "Restaurants, groceries, food delivery", "#28a745", "fas fa-utensils"),
                new Category("Transportation", "Car, gas, public transport, taxi", "#007bff", "fas fa-car"),
                new Category("Entertainment", "Movies, games, concerts, hobbies", "#e83e8c", "fas fa-film"),
                new Category("Bills & Utilities", "Rent, electricity, water, internet", "#fd7e14", "fas fa-bolt"),
                new Category("Personal Care", "Health, gym, beauty, medicine", "#dc3545", "fas fa-heart")
            );

            categoryRepository.saveAll(defaultCategories);
        }
    }

    /**
     * Get total number of categories.
     *
     * @return the count of categories
     */
    @Transactional(readOnly = true)
    public long getCategoryCount() {
        return categoryRepository.count();
    }

    /**
     * Search categories by name or description.
     *
     * @param searchText the text to search for
     * @return List of matching categories
     * @throws ValidationException if searchText is null or empty
     */
    @Transactional(readOnly = true)
    public List<Category> searchCategories(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            throw new ValidationException("Search text cannot be null or empty");
        }
        return categoryRepository.findByNameOrDescriptionContainingIgnoreCase(searchText.trim());
    }
}
