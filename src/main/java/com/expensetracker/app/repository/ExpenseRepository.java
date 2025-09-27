package com.expensetracker.app.repository;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Expense entity operations.
 * 
 * Provides CRUD operations and custom query methods for managing personal expenses.
 * Includes methods for filtering, searching, and aggregating expense data.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Find all expenses for a specific category.
     * 
     * @param category the category to filter by
     * @return List of expenses in the category
     */
    List<Expense> findByCategory(Category category);

    /**
     * Find all expenses within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of expenses within the date range
     */
    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses by category and date range, ordered by expense date descending.
     * 
     * @param category the category to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of expenses matching criteria
     */
    List<Expense> findByCategoryAndExpenseDateBetweenOrderByExpenseDateDesc(
            Category category, LocalDate startDate, LocalDate endDate);

    /**
     * Find all expenses ordered by expense date descending.
     * 
     * @return List of expenses ordered by date (newest first)
     */
    List<Expense> findAllByOrderByExpenseDateDesc();

    /**
     * Find expenses by category, ordered by expense date descending.
     * 
     * @param category the category to filter by
     * @return List of expenses in the category ordered by date (newest first)
     */
    List<Expense> findByCategoryOrderByExpenseDateDesc(Category category);

    /**
     * Find expenses containing specific text in the description.
     * 
     * @param description the text to search for in descriptions
     * @return List of matching expenses
     */
    List<Expense> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Calculate total expenses for a specific category.
     * 
     * @param category the category to calculate total for
     * @return the sum of all expenses in the category
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.category = :category")
    BigDecimal getTotalAmountByCategory(@Param("category") Category category);

    /**
     * Calculate total expenses within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the sum of all expenses in the date range
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Get expense summary by category (category name and total amount).
     * 
     * @return List of arrays containing [category_name, total_amount]
     */
    @Query("SELECT c.name, COALESCE(SUM(e.amount), 0) FROM Category c LEFT JOIN c.expenses e GROUP BY c.name ORDER BY SUM(e.amount) DESC")
    List<Object[]> getExpenseSummaryByCategory();

    /**
     * Find top N most recent expenses.
     * 
     * @param limit the maximum number of expenses to return
     * @return List of recent expenses
     */
    @Query(value = "SELECT * FROM expenses ORDER BY expense_date DESC, created_at DESC LIMIT :limit", nativeQuery = true)
    List<Expense> findTopRecentExpenses(@Param("limit") int limit);

    /**
     * Find expenses by category ID.
     * 
     * @param categoryId the category ID to filter by
     * @return List of expenses in the category
     */
    @Query("SELECT e FROM Expense e WHERE e.category.id = :categoryId")
    List<Expense> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find expenses by category ID ordered by expense date descending.
     * 
     * @param categoryId the category ID to filter by
     * @return List of expenses in the category ordered by date (newest first)
     */
    @Query("SELECT e FROM Expense e WHERE e.category.id = :categoryId ORDER BY e.expenseDate DESC")
    List<Expense> findByCategoryIdOrderByExpenseDateDesc(@Param("categoryId") Long categoryId);

    /**
     * Find expenses with amount greater than specified value.
     * 
     * @param amount the minimum amount
     * @return List of expenses with amount greater than specified value
     */
    List<Expense> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Find expenses with amount between specified range.
     * 
     * @param minAmount the minimum amount (inclusive)
     * @param maxAmount the maximum amount (inclusive)
     * @return List of expenses within the amount range
     */
    List<Expense> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Count expenses for a specific category.
     * 
     * @param category the category to count expenses for
     * @return the count of expenses in the category
     */
    long countByCategory(Category category);

    /**
     * Count expenses within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the count of expenses within the date range
     */
    long countByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Calculate total expenses across all categories.
     *
     * @return the sum of all expenses
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal getTotalExpenses();

    /**
     * Get total expenses grouped by category for reporting.
     *
     * @return List of arrays containing [category_name, total_amount] for each category
     */
    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e GROUP BY e.category.name")
    List<Object[]> getExpensesByCategory();
}