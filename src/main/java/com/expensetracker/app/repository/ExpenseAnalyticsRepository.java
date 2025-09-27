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
 * Repository interface for combined expense tracking analytics.
 * 
 * Provides advanced query methods that combine data from both
 * Expense and Category entities for comprehensive reporting.
 */
@Repository
public interface ExpenseAnalyticsRepository extends JpaRepository<Expense, Long> {

    /**
     * Get monthly expense totals for the current year.
     * 
     * @param year the year to analyze
     * @return List of arrays containing [month, total_amount]
     */
    @Query("SELECT MONTH(e.expenseDate), COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e WHERE YEAR(e.expenseDate) = :year " +
           "GROUP BY MONTH(e.expenseDate) ORDER BY MONTH(e.expenseDate)")
    List<Object[]> getMonthlyExpenseTotals(@Param("year") int year);

    /**
     * Get daily expense totals for a specific month.
     * 
     * @param year the year
     * @param month the month (1-12)
     * @return List of arrays containing [day, total_amount]
     */
    @Query("SELECT DAY(e.expenseDate), COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e WHERE YEAR(e.expenseDate) = :year AND MONTH(e.expenseDate) = :month " +
           "GROUP BY DAY(e.expenseDate) ORDER BY DAY(e.expenseDate)")
    List<Object[]> getDailyExpenseTotals(@Param("year") int year, @Param("month") int month);

    /**
     * Get top spending categories for a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param limit the maximum number of categories to return
     * @return List of arrays containing [category_name, total_amount, expense_count]
     */
    @Query(value = "SELECT c.name, COALESCE(SUM(e.amount), 0), COUNT(e.id) " +
                   "FROM categories c LEFT JOIN expenses e ON c.id = e.category_id " +
                   "WHERE e.expense_date BETWEEN :startDate AND :endDate " +
                   "GROUP BY c.id, c.name " +
                   "ORDER BY SUM(e.amount) DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopSpendingCategories(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate, 
                                          @Param("limit") int limit);

    /**
     * Get expense trends for the last N days.
     * 
     * @param days the number of days to analyze
     * @return List of arrays containing [date, total_amount, expense_count]
     */
    @Query("SELECT e.expenseDate, COALESCE(SUM(e.amount), 0), COUNT(e.id) " +
           "FROM Expense e WHERE e.expenseDate >= :startDate " +
           "GROUP BY e.expenseDate ORDER BY e.expenseDate DESC")
    List<Object[]> getExpenseTrends(@Param("startDate") LocalDate startDate);

    /**
     * Get average daily expense for a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the average daily expense amount
     */
    @Query("SELECT COALESCE(AVG(daily.total), 0) FROM " +
           "(SELECT COALESCE(SUM(e.amount), 0) as total FROM Expense e " +
           "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.expenseDate) as daily")
    BigDecimal getAverageDailyExpense(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find expenses that exceed a certain percentage of the category average.
     * 
     * @param percentage the percentage threshold (e.g., 150.0 for 150%)
     * @return List of expenses that exceed the category average by the specified percentage
     */
    @Query("SELECT e FROM Expense e WHERE e.amount > " +
           "(SELECT AVG(e2.amount) * (:percentage / 100.0) FROM Expense e2 WHERE e2.category = e.category)")
    List<Expense> findHighExpensesRelativeToCategory(@Param("percentage") Double percentage);

    /**
     * Get spending patterns by day of week.
     * 
     * @return List of arrays containing [day_of_week, total_amount, expense_count]
     */
    @Query(value = "SELECT DAYOFWEEK(e.expense_date) as day_of_week, " +
                   "COALESCE(SUM(e.amount), 0) as total_amount, " +
                   "COUNT(e.id) as expense_count " +
                   "FROM expenses e " +
                   "GROUP BY DAYOFWEEK(e.expense_date) " +
                   "ORDER BY day_of_week", nativeQuery = true)
    List<Object[]> getSpendingPatternsByDayOfWeek();

    /**
     * Get budget analysis - categories with their totals compared to a budget limit.
     * 
     * @param startDate the start date for the budget period
     * @param endDate the end date for the budget period
     * @param budgetLimit the budget limit to compare against
     * @return List of arrays containing [category_name, total_spent, budget_limit, over_budget]
     */
    @Query("SELECT c.name, COALESCE(SUM(e.amount), 0), :budgetLimit, " +
           "CASE WHEN COALESCE(SUM(e.amount), 0) > :budgetLimit THEN true ELSE false END " +
           "FROM Category c LEFT JOIN c.expenses e " +
           "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.id, c.name")
    List<Object[]> getBudgetAnalysis(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate, 
                                   @Param("budgetLimit") BigDecimal budgetLimit);
}