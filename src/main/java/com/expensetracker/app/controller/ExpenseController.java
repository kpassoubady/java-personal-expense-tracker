package com.expensetracker.app.controller;

import com.expensetracker.app.config.TemplateConfig;
import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.service.CategoryService;
import com.expensetracker.app.service.ExpenseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for managing expenses.
 * 
 * Handles both web interface and REST API endpoints for expense operations.
 * Provides CRUD functionality, search, filtering, and reporting features.
 */
@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private TemplateConfig templateConfig;

    /**
     * Display all expenses with optional filtering.
     * 
     * @param categoryId optional category filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param search optional search text
     * @param model Spring MVC model
     * @return the expenses list view template
     */
    @GetMapping
    public String listExpenses(@RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             @RequestParam(required = false) String search,
                             Model model) {
        
        List<Expense> expenses;
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            expenses = expenseService.searchExpenses(search);
        } else if (categoryId != null && startDate != null && endDate != null) {
            // Filter by category and date range
            expenses = expenseService.getExpensesByDateRange(startDate, endDate)
                    .stream()
                    .filter(expense -> expense.getCategory().getId().equals(categoryId))
                    .toList();
        } else if (categoryId != null) {
            expenses = expenseService.getExpensesByCategory(categoryId);
        } else if (startDate != null && endDate != null) {
            expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        } else {
            expenses = expenseService.getAllExpenses();
        }

        // Calculate total for filtered expenses
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add data to model
        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("expenseCount", expenses.size());
        
        // Add filter parameters to model for form retention
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("search", search);

        return "expenses/list" + templateConfig.getTemplateSuffix();
    }

    /**
     * Show form to create a new expense.
     * 
     * @param model Spring MVC model
     * @return the expense form template
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Expense expense = new Expense();
        expense.setExpenseDate(LocalDate.now()); // Default to today
        
        model.addAttribute("expense", expense);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Add New Expense");
        model.addAttribute("formAction", "/expenses");
        return "expenses/form" + templateConfig.getTemplateSuffix();
    }

    /**
     * Show form to edit an existing expense.
     * 
     * @param id the expense ID to edit
     * @param model Spring MVC model
     * @param redirectAttributes redirect attributes for error messages
     * @return the expense form template or redirect to list
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Expense> expense = expenseService.findById(id);
        if (expense.isPresent()) {
            model.addAttribute("expense", expense.get());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Edit Expense");
            model.addAttribute("formAction", "/expenses/" + id);
            return "expenses/form" + templateConfig.getTemplateSuffix();
        } else {
            redirectAttributes.addFlashAttribute("error", "Expense not found");
            return "redirect:/expenses";
        }
    }

    /**
     * Handle expense form submission (create new expense).
     * 
     * @param expense the expense data from form
     * @param bindingResult validation results
     * @param model Spring MVC model
     * @param redirectAttributes redirect attributes for success/error messages
     * @return redirect to expenses list or back to form with errors
     */
    @PostMapping
    public String createExpense(@Valid @ModelAttribute Expense expense, 
                              BindingResult bindingResult, 
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        
        // Custom business rule validations
        validateExpenseBusinessRules(expense, bindingResult);
        
        if (bindingResult.hasErrors()) {
            // Log validation errors for debugging
            logger.warn("Validation errors in expense creation: {}", bindingResult.getErrorCount());
            
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Add New Expense");
            model.addAttribute("formAction", "/expenses");
            model.addAttribute("error", "Please correct the errors below and try again.");
            
            return "expenses/form" + templateConfig.getTemplateSuffix();
        }

        try {
            // Set category from categoryId if provided
            if (expense.getCategory() == null && expense.getCategoryId() != null) {
                Category category = categoryService.getCategoryById(expense.getCategoryId());
                expense.setCategory(category);
            }
            
            expenseService.saveExpense(expense);
            
            // Success message with expense details
            String successMessage = String.format(
                "Expense '%s' of $%.2f created successfully!", 
                expense.getDescription(), expense.getAmount());
            redirectAttributes.addFlashAttribute("success", successMessage);
            
            // PRG Pattern - Always redirect after successful POST
            return "redirect:/expenses";
            
        } catch (IllegalArgumentException e) {
            logger.warn("Business rule violation in expense creation: {}", e.getMessage());
            
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Add New Expense");
            model.addAttribute("formAction", "/expenses");
            
            return "expenses/form" + templateConfig.getTemplateSuffix();
            
        } catch (Exception e) {
            logger.error("Unexpected error creating expense", e);
            
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Add New Expense");
            model.addAttribute("formAction", "/expenses");
            
            return "expenses/form" + templateConfig.getTemplateSuffix();
        }
    }

    /**
     * Handle expense form submission (update existing expense).
     * 
     * @param id the expense ID to update
     * @param expense the updated expense data from form
     * @param bindingResult validation results
     * @param model Spring MVC model
     * @param redirectAttributes redirect attributes for success/error messages
     * @return redirect to expenses list or back to form with errors
     */
    @PostMapping("/{id}")
    public String updateExpense(@PathVariable Long id, 
                              @Valid @ModelAttribute Expense expense, 
                              BindingResult bindingResult, 
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        
        // Validate path variable
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid expense ID");
            return "redirect:/expenses";
        }
        
        // Ensure expense exists
        Optional<Expense> existingExpense = expenseService.findById(id);
        if (!existingExpense.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Expense not found");
            return "redirect:/expenses";
        }
        
        // Custom business rule validations
        validateExpenseBusinessRules(expense, bindingResult);
        
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors in expense update: {}", bindingResult.getErrorCount());
            
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Edit Expense");
            model.addAttribute("formAction", "/expenses/" + id);
            model.addAttribute("error", "Please correct the errors below and try again.");
            
            return "expenses/form" + templateConfig.getTemplateSuffix();
        }

        try {
            expense.setId(id);
            
            // Set category from categoryId if provided
            if (expense.getCategory() == null && expense.getCategoryId() != null) {
                Category category = categoryService.getCategoryById(expense.getCategoryId());
                expense.setCategory(category);
            }
            
            expenseService.saveExpense(expense);
            
            // Success message with expense details
            String successMessage = String.format(
                "Expense '%s' updated successfully!", 
                expense.getDescription());
            redirectAttributes.addFlashAttribute("success", successMessage);
            
            // PRG Pattern - Always redirect after successful POST
            return "redirect:/expenses";
            
        } catch (IllegalArgumentException e) {
            logger.warn("Business rule violation in expense update: {}", e.getMessage());
            
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Edit Expense");
            model.addAttribute("formAction", "/expenses/" + id);
            
            return "expenses/form" + templateConfig.getTemplateSuffix();
            
        } catch (Exception e) {
            logger.error("Unexpected error updating expense with ID: {}", id, e);
            
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Edit Expense");
            model.addAttribute("formAction", "/expenses/" + id);
            
            return "expenses/form" + templateConfig.getTemplateSuffix();
        }
    }

    /**
     * Delete an expense.
     * 
     * @param id the expense ID to delete
     * @param redirectAttributes redirect attributes for success/error messages
     * @return redirect to expenses list
     */
    @PostMapping("/{id}/delete")
    public String deleteExpense(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Expense> expense = expenseService.findById(id);
        if (expense.isPresent()) {
            expenseService.deleteExpense(id);
            redirectAttributes.addFlashAttribute("success", "Expense deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Expense not found");
        }
        return "redirect:/expenses";
    }

    /**
     * Display expense reports and analytics.
     * 
     * @param model Spring MVC model
     * @return the reports view template
     */
    @GetMapping("/reports")
    public String showReports(Model model) {
        // Summary statistics
        model.addAttribute("totalExpenses", expenseService.getTotalExpenses());
        model.addAttribute("expenseCount", expenseService.getExpenseCount());
        
        // Category summary
        Map<String, BigDecimal> categorySummary = expenseService.getExpenseSummaryByCategory();
        model.addAttribute("categorySummary", categorySummary);
        
        // Monthly summary
        Map<String, BigDecimal> monthlySummary = expenseService.getMonthlyExpenseSummary();
        model.addAttribute("monthlySummary", monthlySummary);
        
        // Recent expenses
        List<Expense> recentExpenses = expenseService.getRecentExpenses(10);
        model.addAttribute("recentExpenses", recentExpenses);
        
        return "expenses/reports";
    }

    // REST API Endpoints

    /**
     * REST API: Get all expenses with optional filters.
     * 
     * @param categoryId optional category filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param search optional search text
     * @return ResponseEntity with list of expenses
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Expense>> getAllExpensesApi(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {
        
        List<Expense> expenses;
        
        if (search != null && !search.trim().isEmpty()) {
            expenses = expenseService.searchExpenses(search);
        } else if (categoryId != null) {
            expenses = expenseService.getExpensesByCategory(categoryId);
        } else if (startDate != null && endDate != null) {
            expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        } else {
            expenses = expenseService.getAllExpenses();
        }
        
        return ResponseEntity.ok(expenses);
    }

    /**
     * REST API: Get expense by ID.
     * 
     * @param id the expense ID
     * @return ResponseEntity with expense data or 404
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Expense> getExpenseByIdApi(@PathVariable Long id) {
        Optional<Expense> expense = expenseService.findById(id);
        return expense.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * REST API: Create new expense.
     * 
     * @param expense the expense data
     * @return ResponseEntity with created expense or error
     */
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createExpenseApi(@Valid @RequestBody Expense expense, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation errors: " + bindingResult.getAllErrors());
        }

        try {
            Expense savedExpense = expenseService.saveExpense(expense);
            return ResponseEntity.ok(savedExpense);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * REST API: Update existing expense.
     * 
     * @param id the expense ID
     * @param expense the updated expense data
     * @return ResponseEntity with updated expense or error
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateExpenseApi(@PathVariable Long id, 
                                            @Valid @RequestBody Expense expense, 
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation errors: " + bindingResult.getAllErrors());
        }

        try {
            expense.setId(id);
            Expense updatedExpense = expenseService.saveExpense(expense);
            return ResponseEntity.ok(updatedExpense);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * REST API: Delete expense.
     * 
     * @param id the expense ID
     * @return ResponseEntity with success message or error
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteExpenseApi(@PathVariable Long id) {
        Optional<Expense> expense = expenseService.findById(id);
        if (expense.isPresent()) {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok("Expense deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * REST API: Get expense summary by category.
     * 
     * @return ResponseEntity with category summary
     */
    @GetMapping("/api/summary/category")
    @ResponseBody
    public ResponseEntity<Map<String, BigDecimal>> getCategorySummaryApi() {
        Map<String, BigDecimal> summary = expenseService.getExpenseSummaryByCategory();
        return ResponseEntity.ok(summary);
    }

    /**
     * REST API: Get monthly expense summary.
     * 
     * @return ResponseEntity with monthly summary
     */
    @GetMapping("/api/summary/monthly")
    @ResponseBody
    public ResponseEntity<Map<String, BigDecimal>> getMonthlySummaryApi() {
        Map<String, BigDecimal> summary = expenseService.getMonthlyExpenseSummary();
        return ResponseEntity.ok(summary);
    }

    // Private Helper Methods

    /**
     * Validate business rules for expense entities.
     * 
     * @param expense the expense to validate
     * @param bindingResult the binding result to add errors to
     */
    private void validateExpenseBusinessRules(Expense expense, BindingResult bindingResult) {
        // Business rule: Expense amount cannot exceed $10,000
        if (expense.getAmount() != null && expense.getAmount().compareTo(new BigDecimal("10000.00")) > 0) {
            bindingResult.rejectValue("amount", "expense.amount.tooHigh", 
                "Expense amount cannot exceed $10,000.00");
        }
        
        // Business rule: Expense date cannot be more than 1 year in the past
        if (expense.getExpenseDate() != null) {
            LocalDate oneYearAgo = LocalDate.now().minusYears(1);
            if (expense.getExpenseDate().isBefore(oneYearAgo)) {
                bindingResult.rejectValue("expenseDate", "expense.date.tooOld", 
                    "Expense date cannot be more than one year in the past");
            }
        }
        
        // Business rule: Description cannot contain only numbers
        if (expense.getDescription() != null && expense.getDescription().matches("^\\d+$")) {
            bindingResult.rejectValue("description", "expense.description.onlyNumbers", 
                "Description cannot contain only numbers");
        }
        
        // Business rule: Weekend expenses over $500 require additional validation
        if (expense.getExpenseDate() != null && expense.getAmount() != null) {
            int dayOfWeek = expense.getExpenseDate().getDayOfWeek().getValue();
            boolean isWeekend = dayOfWeek >= 6; // Saturday = 6, Sunday = 7
            if (isWeekend && expense.getAmount().compareTo(new BigDecimal("500.00")) > 0) {
                logger.info("High weekend expense flagged for review: {} - ${}", 
                    expense.getDescription(), expense.getAmount());
                // Note: In a real application, this might trigger a review workflow
            }
        }
    }
}