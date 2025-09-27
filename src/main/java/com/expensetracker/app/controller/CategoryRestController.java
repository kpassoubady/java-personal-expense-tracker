package com.expensetracker.app.controller;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.exception.EntityNotFoundException;
import com.expensetracker.app.exception.ValidationException;
import com.expensetracker.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Category operations.
 *
 * Provides RESTful endpoints for managing expense categories including
 * CRUD operations, search functionality, and statistics.
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CategoryRestController {

    @Autowired
    private CategoryService categoryService;

    /**
     * GET /api/categories - Return all categories with expense counts
     *
     * @return ResponseEntity with list of categories and their statistics
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            List<Object[]> categoryStats = categoryService.getCategoryStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);
            response.put("statistics", categoryStats);
            response.put("total", categories.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve categories");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/categories/{id} - Return single category with ResponseEntity
     *
     * @param id the category ID
     * @return ResponseEntity with category or error response
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        try {
            Optional<Category> category = categoryService.findById(id);

            if (category.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("category", category.get());
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Category not found");
                errorResponse.put("message", "No category found with ID: " + id);
                errorResponse.put("success", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/categories - Create category with @Valid validation
     *
     * @param category the category to create
     * @return ResponseEntity with created category or error response
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody Category category) {
        try {
            Category savedCategory = categoryService.saveCategory(category);

            Map<String, Object> response = new HashMap<>();
            response.put("category", savedCategory);
            response.put("message", "Category created successfully");
            response.put("success", true);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("field", e.getField());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create category");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * PUT /api/categories/{id} - Update category with validation
     *
     * @param id the category ID to update
     * @param category the updated category data
     * @return ResponseEntity with updated category or error response
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category category) {
        try {
            // Verify category exists
            Optional<Category> existingCategory = categoryService.findById(id);
            if (!existingCategory.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Category not found");
                errorResponse.put("message", "No category found with ID: " + id);
                errorResponse.put("success", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Set the ID to ensure we're updating the correct entity
            category.setId(id);
            Category updatedCategory = categoryService.saveCategory(category);

            Map<String, Object> response = new HashMap<>();
            response.put("category", updatedCategory);
            response.put("message", "Category updated successfully");
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("field", e.getField());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update category");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * DELETE /api/categories/{id} - Delete with proper error handling
     *
     * @param id the category ID to delete
     * @return ResponseEntity with success message or error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
            // Check if category exists
            Optional<Category> category = categoryService.findById(id);
            if (!category.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Category not found");
                errorResponse.put("message", "No category found with ID: " + id);
                errorResponse.put("success", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            categoryService.deleteCategory(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            response.put("deletedId", id);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Cannot delete category");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Category not found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete category");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/categories/search?name= - Search functionality
     *
     * @param name the search term for category name
     * @return ResponseEntity with matching categories or error response
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCategories(@RequestParam String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid search parameter");
                errorResponse.put("message", "Search name cannot be empty");
                errorResponse.put("success", false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            List<Category> categories = categoryService.searchCategories(name.trim());

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);
            response.put("searchTerm", name.trim());
            response.put("total", categories.size());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Search failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/categories/statistics - Get detailed category statistics
     *
     * @return ResponseEntity with category statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        try {
            List<Object[]> statistics = categoryService.getCategoryStatistics();
            long totalCategories = categoryService.getCategoryCount();

            Map<String, Object> response = new HashMap<>();
            response.put("statistics", statistics);
            response.put("totalCategories", totalCategories);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve statistics");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/categories/{id}/reassign/{replacementId} - Reassign expenses and delete category
     *
     * @param id the category ID to delete
     * @param replacementId the category ID to reassign expenses to
     * @return ResponseEntity with success message or error response
     */
    @PostMapping("/{id}/reassign/{replacementId}")
    public ResponseEntity<Map<String, Object>> deleteCategoryWithReassignment(
            @PathVariable Long id,
            @PathVariable Long replacementId) {
        try {
            categoryService.deleteCategoryWithReassignment(id, replacementId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Category deleted and expenses reassigned successfully");
            response.put("deletedCategoryId", id);
            response.put("replacementCategoryId", replacementId);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Category not found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to reassign and delete category");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
