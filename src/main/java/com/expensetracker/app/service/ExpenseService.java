package com.expensetracker.app.service;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.exception.EntityNotFoundException;
import com.expensetracker.app.exception.ValidationException;
import com.expensetracker.app.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing expenses.
 * 
 * Provides business logic for CRUD operations on expenses,
 * including validation, calculations, and reporting features.
 */
@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryService categoryService;

    /**
     * Get all expenses ordered by date (newest first).
     * 
     * @return List of all expenses
     */
    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllByOrderByExpenseDateDesc();
    }

    /**
     * Find expense by ID.
     * 
     * @param id the expense ID
     * @return Optional containing the expense if found
     */
    @Transactional(readOnly = true)
    public Optional<Expense> findById(Long id) {
        if (id == null) {
            throw new ValidationException("Expense ID cannot be null");
        }
        return expenseRepository.findById(id);
    }

    /**
     * Get expense by ID or throw exception if not found.
     * 
     * @param id the expense ID
     * @return the expense
     * @throws EntityNotFoundException if expense not found
     */
    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense", id));
    }

    /**
     * Save a new expense or update an existing one.
     * 
     * @param expense the expense to save
     * @return the saved expense
     * @throws ValidationException if validation fails
     */
    public Expense saveExpense(Expense expense) {
        validateExpense(expense);
        
        // Additional business logic validation
        if (expense.getExpenseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("expenseDate", expense.getExpenseDate().toString(), 
                "Expense date cannot be in the future");
        }
        
        // Ensure category exists
        if (expense.getCategory().getId() != null) {
            categoryService.findById(expense.getCategory().getId())
                .orElseThrow(() -> new EntityNotFoundException("Category", expense.getCategory().getId()));
        }
        
        try {
            return expenseRepository.save(expense);
        } catch (Exception e) {
            throw new ValidationException("Failed to save expense: " + e.getMessage(), e);
        }
    }

    /**
     * Comprehensive expense validation.
     * 
     * @param expense the expense to validate
     * @throws ValidationException if validation fails
     */
    private void validateExpense(Expense expense) {
        if (expense == null) {
            throw new ValidationException("Expense cannot be null");
        }
        
        if (expense.getDescription() == null || expense.getDescription().trim().isEmpty()) {
            if (expense.getDescription() == null) {
                throw new RuntimeException("Description is required and cannot be null");
            }
            throw new ValidationException("description", expense.getDescription(), 
                "Description is required and cannot be empty");
        }
        
        if (expense.getDescription().length() > 255) {
            throw new ValidationException("description", expense.getDescription(), 
                "Description must not exceed 255 characters");
        }
        
        if (expense.getAmount() == null) {
            throw new ValidationException("amount", "null", "Amount is required");
        }
        
        if (expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            if (expense.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Amount cannot be negative");
            }
            throw new ValidationException("amount", expense.getAmount().toString(), 
                "Amount must be greater than zero");
        }
        
        if (expense.getAmount().scale() > 2) {
            throw new ValidationException("amount", expense.getAmount().toString(), 
                "Amount cannot have more than 2 decimal places");
        }
        
        if (expense.getExpenseDate() == null) {
            throw new ValidationException("expenseDate", "null", "Expense date is required");
        }
        
        if (expense.getCategory() == null) {
            throw new ValidationException("category", "null", "Category is required");
        }
    }

    /**
     * Delete an expense by ID.
     * 
     * @param id the expense ID to delete
     * @throws EntityNotFoundException if expense not found
     * @throws ValidationException if id is null
     */
    public void deleteExpense(Long id) {
        if (id == null) {
            throw new ValidationException("Expense ID cannot be null for deletion");
        }
        
        // Verify expense exists before deletion
        Expense expense = getExpenseById(id);
        
        try {
            expenseRepository.deleteById(id);
        } catch (Exception e) {
            throw new ValidationException("Failed to delete expense with ID " + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Delete multiple expenses by their IDs.
     * 
     * @param ids the list of expense IDs to delete
     * @return the number of expenses successfully deleted
     * @throws ValidationException if ids list is null or empty
     */
    public int deleteExpenses(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ValidationException("Expense IDs list cannot be null or empty");
        }
        
        int deletedCount = 0;
        for (Long id : ids) {
            try {
                deleteExpense(id);
                deletedCount++;
            } catch (EntityNotFoundException e) {
                // Continue with other deletions, but log the missing entity
                // In a real application, you might want to log this
            }
        }
        
        return deletedCount;
    }

    /**
     * Find expenses by category.
     * 
     * @param categoryId the category ID
     * @return List of expenses in the category
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByCategory(Long categoryId) {
        Optional<Category> category = categoryService.findById(categoryId);
        if (category.isPresent()) {
            return expenseRepository.findByCategory(category.get());
        }
        return List.of();
    }

    /**
     * Find expenses within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of expenses within the date range
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByExpenseDateBetween(startDate, endDate);
    }

    /**
     * Search expenses by description.
     * 
     * @param searchText the text to search for in descriptions
     * @return List of matching expenses
     */
    @Transactional(readOnly = true)
    public List<Expense> searchExpenses(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllExpenses();
        }
        return expenseRepository.findByDescriptionContainingIgnoreCase(searchText.trim());
    }

    /**
     * Get recent expenses (last N entries).
     * 
     * @param limit the maximum number of expenses to return
     * @return List of recent expenses
     */
    @Transactional(readOnly = true)
    public List<Expense> getRecentExpenses(int limit) {
        return expenseRepository.findTopRecentExpenses(limit);
    }

    /**
     * Calculate total amount for all expenses.
     * 
     * @return the total amount
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenses() {
        BigDecimal total = expenseRepository.getTotalExpenses();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total amount by category.
     * 
     * @param categoryId the category ID
     * @return the total amount for the category
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalByCategory(Long categoryId) {
        Optional<Category> category = categoryService.findById(categoryId);
        if (category.isPresent()) {
            return expenseRepository.getTotalAmountByCategory(category.get());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculate total amount within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the total amount for the date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.getTotalAmountByDateRange(startDate, endDate);
    }

    /**
     * Get expense summary by category.
     * 
     * @return Map of category names to total amounts
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getExpenseSummaryByCategory() {
        List<Object[]> results = expenseRepository.getExpenseSummaryByCategory();
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],
                    result -> (BigDecimal) result[1]
                ));
    }

    /**
     * Get monthly expense summary for the current year.
     * 
     * @return Map of month names to total amounts
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlyExpenseSummary() {
        int currentYear = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);
        
        List<Expense> yearExpenses = expenseRepository.findByExpenseDateBetween(startOfYear, endOfYear);
        
        return yearExpenses.stream()
                .collect(Collectors.groupingBy(
                    expense -> expense.getExpenseDate().getMonth().toString(),
                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
    }

    /**
     * Get total number of expenses.
     * 
     * @return the count of expenses
     */
    @Transactional(readOnly = true)
    public long getExpenseCount() {
        return expenseRepository.count();
    }

    /**
     * Get category summary for dashboard charts.
     * 
     * @return Map of category names to total amounts
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategorySummary() {
        return getExpenseSummaryByCategory();
    }

    /**
     * Calculate total amount within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the total amount for the date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return getTotalByDateRange(startDate, endDate);
    }

    /**
     * Count expenses within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the count of expenses within the date range
     */
    @Transactional(readOnly = true)
    public long getExpenseCountByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.countByExpenseDateBetween(startDate, endDate);
    }

    /**
     * Create sample expenses for demo purposes.
     * This method creates sample data if no expenses exist.
     */
    public void createSampleExpenses() {
        if (expenseRepository.count() == 0) {
            // Ensure default categories exist
            categoryService.createDefaultCategories();
            
            // Get categories for sample expenses
            List<Category> categories = categoryService.getAllCategories();
            
            if (!categories.isEmpty()) {
                // Create sample expenses
                Expense[] sampleExpenses = {
                    createSampleExpense("Grocery shopping at Whole Foods", new BigDecimal("85.50"), 
                                      LocalDate.now().minusDays(1), findCategoryByName(categories, "Food & Dining")),
                    createSampleExpense("Gas station fill-up", new BigDecimal("45.00"), 
                                      LocalDate.now().minusDays(2), findCategoryByName(categories, "Transportation")),
                    createSampleExpense("Movie tickets", new BigDecimal("24.00"), 
                                      LocalDate.now().minusDays(3), findCategoryByName(categories, "Entertainment")),
                    createSampleExpense("Monthly gym membership", new BigDecimal("59.99"), 
                                      LocalDate.now().minusDays(5), findCategoryByName(categories, "Personal Care")),
                    createSampleExpense("Electric bill", new BigDecimal("120.75"), 
                                      LocalDate.now().minusDays(7), findCategoryByName(categories, "Bills & Utilities"))
                };

                for (Expense expense : sampleExpenses) {
                    if (expense != null) {
                        expenseRepository.save(expense);
                    }
                }
            }
        }
    }

    /**
     * Get detailed expense summaries with enhanced analytics.
     * 
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return Map containing various expense summaries and statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getExpenseAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new java.util.HashMap<>();
        
        // Basic totals
        BigDecimal totalAmount = startDate != null && endDate != null 
            ? getTotalByDateRange(startDate, endDate)
            : getTotalExpenses();
        analytics.put("totalAmount", totalAmount);
        
        // Count statistics
        long totalCount = startDate != null && endDate != null 
            ? expenseRepository.countByExpenseDateBetween(startDate, endDate)
            : getExpenseCount();
        analytics.put("totalCount", totalCount);
        
        // Average expense
        BigDecimal averageExpense = totalCount > 0 
            ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        analytics.put("averageExpense", averageExpense);
        
        // Category summaries
        analytics.put("categorySummary", getExpenseSummaryByCategory());
        
        // Monthly summaries (if no date range specified, use current year)
        if (startDate == null && endDate == null) {
            analytics.put("monthlySummary", getMonthlyExpenseSummary());
        }
        
        return analytics;
    }

    /**
     * Get top spending categories with detailed information.
     * 
     * @param limit the maximum number of categories to return
     * @return List of maps containing category information and spending totals
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopSpendingCategories(int limit) {
        if (limit <= 0) {
            throw new ValidationException("Limit must be greater than zero");
        }
        
        List<Object[]> categoryStats = categoryService.getCategoryStatistics();
        
        return categoryStats.stream()
                .limit(limit)
                .map(stat -> {
                    Map<String, Object> categoryInfo = new java.util.HashMap<>();
                    categoryInfo.put("categoryName", stat[0]);
                    categoryInfo.put("expenseCount", stat[1]);
                    categoryInfo.put("totalAmount", stat[2]);
                    return categoryInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get expenses that exceed a certain amount threshold.
     * 
     * @param threshold the minimum amount threshold
     * @return List of expenses above the threshold
     */
    @Transactional(readOnly = true)
    public List<Expense> getHighValueExpenses(BigDecimal threshold) {
        if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Threshold must be greater than zero");
        }
        return expenseRepository.findByAmountGreaterThan(threshold);
    }

    /**
     * Get expense statistics for a specific category.
     * 
     * @param categoryId the category ID
     * @return Map containing category expense statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCategoryExpenseStats(Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        
        List<Expense> categoryExpenses = expenseRepository.findByCategory(category);
        BigDecimal totalAmount = getTotalByCategory(categoryId);
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("categoryName", category.getName());
        stats.put("expenseCount", categoryExpenses.size());
        stats.put("totalAmount", totalAmount);
        
        if (!categoryExpenses.isEmpty()) {
            // Calculate average
            BigDecimal average = totalAmount.divide(BigDecimal.valueOf(categoryExpenses.size()), 2, java.math.RoundingMode.HALF_UP);
            stats.put("averageAmount", average);
            
            // Find min and max
            BigDecimal minAmount = categoryExpenses.stream()
                    .map(Expense::getAmount)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxAmount = categoryExpenses.stream()
                    .map(Expense::getAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            stats.put("minAmount", minAmount);
            stats.put("maxAmount", maxAmount);
        }
        
        return stats;
    }

    public java.util.List<com.expensetracker.app.entity.Expense> saveExpenses(java.util.List<com.expensetracker.app.entity.Expense> expenses) {
        return expenseRepository.saveAll(expenses);
    }

    private Expense createSampleExpense(String description, BigDecimal amount, LocalDate date, Category category) {
        if (category == null) return null;
        
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setExpenseDate(date);
        expense.setCategory(category);
        // createdAt and updatedAt will be set automatically by JPA @PrePersist
        return expense;
    }

    private Category findCategoryByName(List<Category> categories, String name) {
        return categories.stream()
                .filter(category -> category.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}

