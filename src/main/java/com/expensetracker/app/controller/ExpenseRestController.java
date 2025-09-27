package com.expensetracker.app.controller;

import com.expensetracker.app.dto.ExpenseDTO;
import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.exception.EntityNotFoundException;
import com.expensetracker.app.exception.ValidationException;
import com.expensetracker.app.service.CategoryService;
import com.expensetracker.app.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Expense management operations.
 * 
 * Provides comprehensive API endpoints for expense CRUD operations,
 * advanced search, analytics, and reporting functionalities.
 */
@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class ExpenseRestController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Get paginated expenses with category information.
     * 
     * GET /api/expenses?page=0&size=10&sort=expenseDate,desc
     * 
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by
     * @param sortDir the sort direction (asc/desc)
     * @return ResponseEntity with paginated expense data
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            // Validate pagination parameters
            if (page < 0) {
                throw new ValidationException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new ValidationException("Page size must be between 1 and 100");
            }

            // Create sort direction
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            // For now, we'll simulate pagination by getting all expenses and paginating them
            List<Expense> allExpenses = expenseService.getAllExpenses();
            
            // Apply sorting
            allExpenses.sort((e1, e2) -> {
                int comparison = 0;
                switch (sortBy.toLowerCase()) {
                    case "expensedate":
                        comparison = e1.getExpenseDate().compareTo(e2.getExpenseDate());
                        break;
                    case "amount":
                        comparison = e1.getAmount().compareTo(e2.getAmount());
                        break;
                    case "description":
                        comparison = e1.getDescription().compareToIgnoreCase(e2.getDescription());
                        break;
                    case "category":
                        comparison = e1.getCategory().getName().compareToIgnoreCase(e2.getCategory().getName());
                        break;
                    default:
                        comparison = e1.getExpenseDate().compareTo(e2.getExpenseDate());
                }
                return direction == Sort.Direction.DESC ? -comparison : comparison;
            });

            // Calculate pagination
            int totalElements = allExpenses.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            List<ExpenseDTO> expenseDTOs;
            if (startIndex < totalElements) {
                expenseDTOs = allExpenses.subList(startIndex, endIndex)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            } else {
                expenseDTOs = List.of();
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("content", expenseDTOs);
            response.put("currentPage", page);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("size", size);
            response.put("first", page == 0);
            response.put("last", page >= totalPages - 1);
            response.put("hasNext", page < totalPages - 1);
            response.put("hasPrevious", page > 0);

            return ResponseEntity.ok(response);
            
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to retrieve expenses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get a single expense by ID with validation.
     * 
     * GET /api/expenses/{id}
     * 
     * @param id the expense ID
     * @return ResponseEntity with expense data or error
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getExpenseById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid expense ID: " + id);
            }

            Expense expense = expenseService.getExpenseById(id);
            ExpenseDTO expenseDTO = convertToDTO(expense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("expense", expenseDTO);
            response.put("message", "Expense retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to retrieve expense: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create a new expense with category validation.
     * 
     * POST /api/expenses
     * 
     * @param expenseDTO the expense data
     * @return ResponseEntity with created expense or validation errors
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        try {
            // Additional business validation
            if (expenseDTO.getExpenseDate() != null && expenseDTO.getExpenseDate().isAfter(LocalDate.now())) {
                throw new ValidationException("Expense date cannot be in the future");
            }

            // Validate category exists
            Category category = categoryService.getCategoryById(expenseDTO.getCategoryId());
            
            // Convert DTO to Entity
            Expense expense = convertToEntity(expenseDTO);
            expense.setCategory(category);
            
            // Save expense
            Expense savedExpense = expenseService.saveExpense(expense);
            ExpenseDTO savedExpenseDTO = convertToDTO(savedExpense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("expense", savedExpenseDTO);
            response.put("message", "Expense created successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to create expense: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an existing expense with business validation.
     * 
     * PUT /api/expenses/{id}
     * 
     * @param id the expense ID
     * @param expenseDTO the updated expense data
     * @return ResponseEntity with updated expense or validation errors
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateExpense(
            @PathVariable Long id, 
            @Valid @RequestBody ExpenseDTO expenseDTO) {
        
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid expense ID: " + id);
            }

            // Verify expense exists
            Expense existingExpense = expenseService.getExpenseById(id);
            
            // Additional business validation
            if (expenseDTO.getExpenseDate() != null && expenseDTO.getExpenseDate().isAfter(LocalDate.now())) {
                throw new ValidationException("Expense date cannot be in the future");
            }

            // Validate category exists
            Category category = categoryService.getCategoryById(expenseDTO.getCategoryId());
            
            // Update expense
            existingExpense.setDescription(expenseDTO.getDescription());
            existingExpense.setAmount(expenseDTO.getAmount());
            existingExpense.setExpenseDate(expenseDTO.getExpenseDate());
            existingExpense.setCategory(category);
            
            // Save updated expense
            Expense updatedExpense = expenseService.saveExpense(existingExpense);
            ExpenseDTO updatedExpenseDTO = convertToDTO(updatedExpense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("expense", updatedExpenseDTO);
            response.put("message", "Expense updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to update expense: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete an expense with confirmation.
     * 
     * DELETE /api/expenses/{id}?confirm=true
     * 
     * @param id the expense ID
     * @param confirm confirmation flag (must be true)
     * @return ResponseEntity with deletion status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteExpense(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean confirm) {
        
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid expense ID: " + id);
            }

            if (!confirm) {
                Map<String, Object> confirmationResponse = new HashMap<>();
                confirmationResponse.put("error", "Confirmation Required");
                confirmationResponse.put("message", "Please set confirm=true to delete expense");
                confirmationResponse.put("expenseId", id);
                return ResponseEntity.badRequest().body(confirmationResponse);
            }

            // Get expense details before deletion for response
            Expense expense = expenseService.getExpenseById(id);
            String expenseDescription = expense.getDescription();
            
            // Delete expense
            expenseService.deleteExpense(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expense deleted successfully");
            response.put("deletedExpenseId", id);
            response.put("deletedExpenseDescription", expenseDescription);
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to delete expense: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get expenses by category with pagination.
     * 
     * GET /api/expenses/category/{categoryId}?page=0&size=10
     * 
     * @param categoryId the category ID
     * @param page the page number
     * @param size the page size
     * @return ResponseEntity with category expenses
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getExpensesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            if (categoryId == null || categoryId <= 0) {
                throw new ValidationException("Invalid category ID: " + categoryId);
            }

            if (page < 0) {
                throw new ValidationException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new ValidationException("Page size must be between 1 and 100");
            }

            // Verify category exists
            Category category = categoryService.getCategoryById(categoryId);
            
            // Get expenses for category
            List<Expense> categoryExpenses = expenseService.getExpensesByCategory(categoryId);
            
            // Apply pagination
            int totalElements = categoryExpenses.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            List<ExpenseDTO> expenseDTOs;
            if (startIndex < totalElements) {
                expenseDTOs = categoryExpenses.subList(startIndex, endIndex)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            } else {
                expenseDTOs = List.of();
            }

            // Calculate category statistics
            BigDecimal categoryTotal = expenseService.getTotalByCategory(categoryId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("categoryName", category.getName());
            response.put("categoryId", categoryId);
            response.put("expenses", expenseDTOs);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("size", size);
            response.put("categoryTotal", categoryTotal);
            response.put("hasNext", page < totalPages - 1);
            response.put("hasPrevious", page > 0);
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to retrieve category expenses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get comprehensive analytics summary.
     * 
     * GET /api/expenses/analytics?startDate=2024-01-01&endDate=2024-12-31
     * 
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param topCategoriesLimit limit for top spending categories
     * @return ResponseEntity with analytics data
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getExpenseAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "5") int topCategoriesLimit) {
        
        try {
            if (topCategoriesLimit <= 0 || topCategoriesLimit > 20) {
                throw new ValidationException("Top categories limit must be between 1 and 20");
            }

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new ValidationException("Start date cannot be after end date");
            }

            // Get comprehensive analytics
            Map<String, Object> analytics = expenseService.getExpenseAnalytics(startDate, endDate);
            
            // Add top spending categories
            List<Map<String, Object>> topCategories = expenseService.getTopSpendingCategories(topCategoriesLimit);
            analytics.put("topSpendingCategories", topCategories);
            
            // Add date range information
            Map<String, Object> dateRange = new HashMap<>();
            dateRange.put("startDate", startDate);
            dateRange.put("endDate", endDate);
            dateRange.put("isFiltered", startDate != null || endDate != null);
            analytics.put("dateRange", dateRange);
            
            // Add recent expenses
            List<ExpenseDTO> recentExpenses = expenseService.getRecentExpenses(5)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            analytics.put("recentExpenses", recentExpenses);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analytics", analytics);
            response.put("message", "Analytics retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to retrieve analytics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Advanced search with filters.
     * 
     * GET /api/expenses/search?q=grocery&categoryId=1&minAmount=10&maxAmount=100&startDate=2024-01-01&endDate=2024-12-31&page=0&size=10
     * 
     * @param query search text for description
     * @param categoryId filter by category
     * @param minAmount minimum amount filter
     * @param maxAmount maximum amount filter
     * @param startDate start date filter
     * @param endDate end date filter
     * @param page page number
     * @param size page size
     * @return ResponseEntity with filtered expenses
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchExpenses(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            if (page < 0) {
                throw new ValidationException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new ValidationException("Page size must be between 1 and 100");
            }

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new ValidationException("Start date cannot be after end date");
            }

            if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
                throw new ValidationException("Minimum amount cannot be greater than maximum amount");
            }

            if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Minimum amount cannot be negative");
            }

            if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Maximum amount must be greater than zero");
            }

            // Start with all expenses or search results
            List<Expense> expenses;
            if (query != null && !query.trim().isEmpty()) {
                expenses = expenseService.searchExpenses(query.trim());
            } else {
                expenses = expenseService.getAllExpenses();
            }

            // Apply filters
            if (categoryId != null) {
                // Verify category exists
                categoryService.getCategoryById(categoryId);
                expenses = expenses.stream()
                    .filter(expense -> expense.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
            }

            if (minAmount != null) {
                expenses = expenses.stream()
                    .filter(expense -> expense.getAmount().compareTo(minAmount) >= 0)
                    .collect(Collectors.toList());
            }

            if (maxAmount != null) {
                expenses = expenses.stream()
                    .filter(expense -> expense.getAmount().compareTo(maxAmount) <= 0)
                    .collect(Collectors.toList());
            }

            if (startDate != null) {
                expenses = expenses.stream()
                    .filter(expense -> !expense.getExpenseDate().isBefore(startDate))
                    .collect(Collectors.toList());
            }

            if (endDate != null) {
                expenses = expenses.stream()
                    .filter(expense -> !expense.getExpenseDate().isAfter(endDate))
                    .collect(Collectors.toList());
            }

            // Apply pagination
            int totalElements = expenses.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            List<ExpenseDTO> expenseDTOs;
            if (startIndex < totalElements) {
                expenseDTOs = expenses.subList(startIndex, endIndex)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            } else {
                expenseDTOs = List.of();
            }

            // Calculate search statistics
            BigDecimal searchTotal = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Build search criteria summary
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("query", query);
            searchCriteria.put("categoryId", categoryId);
            searchCriteria.put("minAmount", minAmount);
            searchCriteria.put("maxAmount", maxAmount);
            searchCriteria.put("startDate", startDate);
            searchCriteria.put("endDate", endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("expenses", expenseDTOs);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("size", size);
            response.put("searchTotal", searchTotal);
            response.put("searchCriteria", searchCriteria);
            response.put("hasNext", page < totalPages - 1);
            response.put("hasPrevious", page > 0);
            response.put("message", "Search completed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to search expenses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Utility Methods

    /**
     * Convert Expense entity to ExpenseDTO.
     * 
     * @param expense the expense entity
     * @return ExpenseDTO
     */
    private ExpenseDTO convertToDTO(Expense expense) {
        if (expense == null) {
            return null;
        }
        
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        
        if (expense.getCategory() != null) {
            dto.setCategoryId(expense.getCategory().getId());
            dto.setCategoryName(expense.getCategory().getName());
        }
        
        return dto;
    }

    /**
     * Convert ExpenseDTO to Expense entity.
     * 
     * @param expenseDTO the expense DTO
     * @return Expense entity
     */
    private Expense convertToEntity(ExpenseDTO expenseDTO) {
        if (expenseDTO == null) {
            return null;
        }
        
        Expense expense = new Expense();
        expense.setId(expenseDTO.getId());
        expense.setDescription(expenseDTO.getDescription());
        expense.setAmount(expenseDTO.getAmount());
        expense.setExpenseDate(expenseDTO.getExpenseDate());
        
        // Category will be set by the caller
        
        return expense;
    }
}