package com.expensetracker.app.controller;

import com.expensetracker.app.config.TemplateConfig;
import com.expensetracker.app.entity.Category;
import com.expensetracker.app.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing categories.
 * 
 * Handles both web interface and REST API endpoints for category operations.
 * Provides CRUD functionality with proper error handling and validation.
 */
@Controller
@RequestMapping("/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TemplateConfig templateConfig;

    /**
     * Display all categories in a web page.
     * 
     * @param model Spring MVC model
     * @return the categories view template
     */
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("categoryCount", categoryService.getCategoryCount());
        
        // Additional data for new layout
        if (templateConfig.isUseNewLayout()) {
            // Calculate total expense count from categories
            int totalExpenseCount = categories.stream().mapToInt(c -> c.getExpenses().size()).sum();
            double totalAmount = categories.stream()
                .flatMap(c -> c.getExpenses().stream())
                .mapToDouble(e -> e.getAmount().doubleValue())
                .sum();
            
            model.addAttribute("totalExpenseCount", totalExpenseCount);
            model.addAttribute("totalAmount", totalAmount);
        }
        
        return "categories/list" + templateConfig.getTemplateSuffix();
    }

    /**
     * Show form to create a new category.
     * 
     * @param model Spring MVC model
     * @return the category form template
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Add New Category");
        model.addAttribute("formAction", "/categories");
        
        // Additional data for new layout
        if (templateConfig.isUseNewLayout()) {
            model.addAttribute("iconOptions", getAvailableIcons());
            model.addAttribute("colorOptions", getAvailableColors());
            // Get recent categories (just first 5 categories for now)
            List<Category> allCategories = categoryService.getAllCategories();
            List<Category> recentCategories = allCategories.size() > 5 ? 
                allCategories.subList(0, 5) : allCategories;
            model.addAttribute("recentCategories", recentCategories);
        }
        
        return "categories/form" + templateConfig.getTemplateSuffix();
    }

    /**
     * Show form to edit an existing category.
     * 
     * @param id the category ID to edit
     * @param model Spring MVC model
     * @param redirectAttributes redirect attributes for error messages
     * @return the category form template or redirect to list
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            model.addAttribute("pageTitle", "Edit Category");
            model.addAttribute("formAction", "/categories/" + id);
            
            // Additional data for new layout
            if (templateConfig.isUseNewLayout()) {
                model.addAttribute("iconOptions", getAvailableIcons());
                model.addAttribute("colorOptions", getAvailableColors());
                // Get recent categories (just first 5 categories for now)
                List<Category> allCategories = categoryService.getAllCategories();
                List<Category> recentCategories = allCategories.size() > 5 ? 
                    allCategories.subList(0, 5) : allCategories;
                model.addAttribute("recentCategories", recentCategories);
            }
            
            return "categories/form" + templateConfig.getTemplateSuffix();
        } else {
            redirectAttributes.addFlashAttribute("error", "Category not found");
            return "redirect:/categories";
        }
    }

    /**
     * Handle category form submission (create new category).
     * 
     * @param category the category data from form
     * @param bindingResult validation results
     * @param model Spring MVC model
     * @param redirectAttributes redirect attributes for success/error messages
     * @return redirect to categories list or back to form with errors
     */
    @PostMapping
    public String createCategory(@Valid @ModelAttribute Category category, 
                               BindingResult bindingResult, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        
        // Custom business rule validations
        validateCategoryBusinessRules(category, bindingResult);
        
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors in category creation: {}", bindingResult.getErrorCount());
            
            model.addAttribute("pageTitle", "Add New Category");
            model.addAttribute("formAction", "/categories");
            model.addAttribute("error", "Please correct the errors below and try again.");
            
            // Additional data for new layout
            if (templateConfig.isUseNewLayout()) {
                model.addAttribute("iconOptions", getAvailableIcons());
                model.addAttribute("colorOptions", getAvailableColors());
                // Get recent categories (just first 5 categories for now)
                List<Category> allCategories = categoryService.getAllCategories();
                List<Category> recentCategories = allCategories.size() > 5 ? 
                    allCategories.subList(0, 5) : allCategories;
                model.addAttribute("recentCategories", recentCategories);
            }
            
            return "categories/form" + templateConfig.getTemplateSuffix();
        }

        try {
            // Set default values if not provided
            if (category.getColor() == null || category.getColor().trim().isEmpty()) {
                category.setColor("#007bff");
            }
            if (category.getIcon() == null || category.getIcon().trim().isEmpty()) {
                category.setIcon("fas fa-tag");
            }
            
            categoryService.saveCategory(category);
            
            // Success message with category details
            String successMessage = String.format(
                "Category '%s' created successfully!", 
                category.getName());
            redirectAttributes.addFlashAttribute("success", successMessage);
            
            // PRG Pattern - Always redirect after successful POST
            return "redirect:/categories";
            
        } catch (IllegalArgumentException e) {
            logger.warn("Business rule violation in category creation: {}", e.getMessage());
            
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Add New Category");
            model.addAttribute("formAction", "/categories");
            
            // Additional data for new layout
            if (templateConfig.isUseNewLayout()) {
                model.addAttribute("iconOptions", getAvailableIcons());
                model.addAttribute("colorOptions", getAvailableColors());
                // Get recent categories (just first 5 categories for now)
                List<Category> allCategories = categoryService.getAllCategories();
                List<Category> recentCategories = allCategories.size() > 5 ? 
                    allCategories.subList(0, 5) : allCategories;
                model.addAttribute("recentCategories", recentCategories);
            }
            
            return "categories/form" + templateConfig.getTemplateSuffix();
            
        } catch (Exception e) {
            logger.error("Unexpected error creating category", e);
            
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("pageTitle", "Add New Category");
            model.addAttribute("formAction", "/categories");
            
            if (templateConfig.isUseNewLayout()) {
                model.addAttribute("iconOptions", getAvailableIcons());
                model.addAttribute("colorOptions", getAvailableColors());
                List<Category> allCategories = categoryService.getAllCategories();
                List<Category> recentCategories = allCategories.size() > 5 ? 
                    allCategories.subList(0, 5) : allCategories;
                model.addAttribute("recentCategories", recentCategories);
            }
            
            return "categories/form" + templateConfig.getTemplateSuffix();
        }
    }

    /**
     * Handle category form submission (update existing category).
     * 
     * @param id the category ID to update
     * @param category the updated category data from form
     * @param bindingResult validation results
     * @param model Spring MVC model
     * @param redirectAttributes redirect attributes for success/error messages
     * @return redirect to categories list or back to form with errors
     */
    @PostMapping("/{id}")
    public String updateCategory(@PathVariable Long id, 
                               @Valid @ModelAttribute Category category, 
                               BindingResult bindingResult, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Category");
            model.addAttribute("formAction", "/categories/" + id);
            
            // Additional data for new layout
            if (templateConfig.isUseNewLayout()) {
                model.addAttribute("iconOptions", getAvailableIcons());
                model.addAttribute("colorOptions", getAvailableColors());
                // Get recent categories (just first 5 categories for now)
                List<Category> allCategories = categoryService.getAllCategories();
                List<Category> recentCategories = allCategories.size() > 5 ? 
                    allCategories.subList(0, 5) : allCategories;
                model.addAttribute("recentCategories", recentCategories);
            }
            
            return "categories/form" + templateConfig.getTemplateSuffix();
        }

        try {
            category.setId(id);
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
            return "redirect:/categories";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Edit Category");
            model.addAttribute("formAction", "/categories/" + id);
            
            // Additional data for new layout
            if (templateConfig.isUseNewLayout()) {
                model.addAttribute("iconOptions", getAvailableIcons());
                model.addAttribute("colorOptions", getAvailableColors());
                // Get recent categories (just first 5 categories for now)
                List<Category> allCategories = categoryService.getAllCategories();
                List<Category> recentCategories = allCategories.size() > 5 ? 
                    allCategories.subList(0, 5) : allCategories;
                model.addAttribute("recentCategories", recentCategories);
            }
            
            return "categories/form" + templateConfig.getTemplateSuffix();
        }
    }

    /**
     * Delete a category.
     * 
     * @param id the category ID to delete
     * @param redirectAttributes redirect attributes for success/error messages
     * @return redirect to categories list
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Category> category = categoryService.findById(id);
            if (category.isPresent()) {
                categoryService.deleteCategory(id);
                redirectAttributes.addFlashAttribute("success", 
                    "Category '" + category.get().getName() + "' deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Category not found");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }

    // REST API Endpoints

    /**
     * REST API: Get all categories.
     * 
     * @return ResponseEntity with list of categories
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Category>> getAllCategoriesApi() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * REST API: Get category by ID.
     * 
     * @param id the category ID
     * @return ResponseEntity with category data or 404
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Category> getCategoryByIdApi(@PathVariable Long id) {
        Optional<Category> category = categoryService.findById(id);
        return category.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * REST API: Create new category.
     * 
     * @param category the category data
     * @return ResponseEntity with created category or error
     */
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createCategoryApi(@Valid @RequestBody Category category, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation errors: " + bindingResult.getAllErrors());
        }

        try {
            Category savedCategory = categoryService.saveCategory(category);
            return ResponseEntity.ok(savedCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * REST API: Update existing category.
     * 
     * @param id the category ID
     * @param category the updated category data
     * @return ResponseEntity with updated category or error
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCategoryApi(@PathVariable Long id, 
                                             @Valid @RequestBody Category category, 
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation errors: " + bindingResult.getAllErrors());
        }

        try {
            category.setId(id);
            Category updatedCategory = categoryService.saveCategory(category);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * REST API: Delete category.
     * 
     * @param id the category ID
     * @return ResponseEntity with success message or error
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategoryApi(@PathVariable Long id) {
        try {
            Optional<Category> category = categoryService.findById(id);
            if (category.isPresent()) {
                categoryService.deleteCategory(id);
                return ResponseEntity.ok("Category deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Helper methods for new template system

    /**
     * Get available icons for category selection.
     * 
     * @return List of available FontAwesome icon classes
     */
    private List<String> getAvailableIcons() {
        return Arrays.asList(
            "fa-utensils", "fa-car", "fa-home", "fa-shopping-cart", "fa-gamepad",
            "fa-film", "fa-dumbbell", "fa-heart", "fa-graduation-cap", "fa-briefcase",
            "fa-plane", "fa-coffee", "fa-music", "fa-book", "fa-mobile-alt",
            "fa-laptop", "fa-tshirt", "fa-gift", "fa-paw", "fa-tools",
            "fa-medkit", "fa-bus", "fa-gas-pump", "fa-shopping-bag", "fa-camera",
            "fa-wine-glass", "fa-pizza-slice", "fa-hamburger", "fa-ice-cream", "fa-birthday-cake"
        );
    }

    /**
     * Get available colors for category selection.
     * 
     * @return List of available hex color codes
     */
    private List<String> getAvailableColors() {
        return Arrays.asList(
            "#007bff", "#28a745", "#ffc107", "#dc3545", "#6c757d",
            "#17a2b8", "#6f42c1", "#e83e8c", "#fd7e14", "#20c997",
            "#495057", "#f8f9fa", "#343a40", "#ffffff", "#000000",
            "#ff6b6b", "#4ecdc4", "#45b7d1", "#f9ca24", "#f0932b",
            "#eb4d4b", "#6c5ce7", "#a29bfe", "#fd79a8", "#e17055"
        );
    }

    // Private Helper Methods

    /**
     * Validate business rules for category entities.
     * 
     * @param category the category to validate
     * @param bindingResult the binding result to add errors to
     */
    private void validateCategoryBusinessRules(Category category, BindingResult bindingResult) {
        // Business rule: Category name cannot be "Default" (reserved)
        if (category.getName() != null && "default".equalsIgnoreCase(category.getName().trim())) {
            bindingResult.rejectValue("name", "category.name.reserved", 
                "Category name 'Default' is reserved and cannot be used");
        }
        
        // Business rule: Category name cannot contain special characters except spaces and dashes
        if (category.getName() != null && !category.getName().matches("^[a-zA-Z0-9\\s\\-]+$")) {
            bindingResult.rejectValue("name", "category.name.invalidChars", 
                "Category name can only contain letters, numbers, spaces, and dashes");
        }
        
        // Business rule: Color must be a valid hex color (additional validation beyond @ValidColor)
        if (category.getColor() != null && !category.getColor().isEmpty()) {
            String color = category.getColor().trim();
            if (!color.startsWith("#")) {
                bindingResult.rejectValue("color", "category.color.invalidFormat", 
                    "Color must start with # (e.g., #FF0000)");
            }
        }
        
        // Business rule: Icon must be from FontAwesome (basic validation)
        if (category.getIcon() != null && !category.getIcon().isEmpty()) {
            String icon = category.getIcon().trim();
            if (!icon.startsWith("fa")) {
                bindingResult.rejectValue("icon", "category.icon.invalidFormat", 
                    "Icon must be a valid FontAwesome class (e.g., fas fa-tag)");
            }
        }
        
        // Business rule: Description cannot be the same as name
        if (category.getName() != null && category.getDescription() != null &&
            category.getName().equalsIgnoreCase(category.getDescription().trim())) {
            bindingResult.rejectValue("description", "category.description.sameAsName", 
                "Description should be different from the category name");
        }
    }
}