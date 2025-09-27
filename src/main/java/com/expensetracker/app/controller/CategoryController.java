package com.expensetracker.app.controller;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private CategoryService categoryService;

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
        return "categories/list";
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
        return "categories/form";
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
            return "categories/form";
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
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Category");
            model.addAttribute("formAction", "/categories");
            return "categories/form";
        }

        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category created successfully!");
            return "redirect:/categories";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Add New Category");
            model.addAttribute("formAction", "/categories");
            return "categories/form";
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
            return "categories/form";
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
            return "categories/form";
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
}