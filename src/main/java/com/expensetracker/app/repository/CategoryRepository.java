package com.expensetracker.app.repository;

import com.expensetracker.app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations.
 * 
 * Provides CRUD operations and custom query methods for managing expense categories.
 * Extends JpaRepository to inherit basic database operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find a category by its name (case-insensitive).
     * 
     * @param name the category name to search for
     * @return Optional containing the category if found
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Find all categories ordered by name alphabetically.
     * 
     * @return List of categories sorted by name
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Check if a category exists by name (case-insensitive).
     * 
     * @param name the category name to check
     * @return true if category exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find categories that contain the given text in their name or description.
     * 
     * @param searchText the text to search for
     * @return List of matching categories
     */
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Category> findByNameOrDescriptionContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Find categories with their expense count.
     * 
     * @return List of categories with expense counts
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.expenses")
    List<Category> findAllWithExpenses();

    /**
     * Find categories that have at least one expense.
     * 
     * @return List of categories with expenses
     */
    @Query("SELECT DISTINCT c FROM Category c INNER JOIN c.expenses e")
    List<Category> findCategoriesWithExpenses();

    /**
     * Find categories that have no expenses.
     * 
     * @return List of categories without expenses
     */
    @Query("SELECT c FROM Category c WHERE c.expenses IS EMPTY")
    List<Category> findCategoriesWithoutExpenses();

    /**
     * Find categories ordered by expense count descending.
     * 
     * @return List of categories ordered by number of expenses (most used first)
     */
    @Query("SELECT c FROM Category c LEFT JOIN c.expenses e GROUP BY c ORDER BY COUNT(e) DESC")
    List<Category> findAllOrderByExpenseCountDesc();

    /**
     * Get category statistics (name, expense count, total amount).
     * 
     * @return List of arrays containing [category_name, expense_count, total_amount]
     */
    @Query("SELECT c.name, COUNT(e), COALESCE(SUM(e.amount), 0) " +
           "FROM Category c LEFT JOIN c.expenses e " +
           "GROUP BY c.id, c.name " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategoryStatistics();

    /**
     * Find categories by color.
     * 
     * @param color the color to search for
     * @return List of categories with the specified color
     */
    List<Category> findByColor(String color);

    /**
     * Find categories by icon.
     * 
     * @param icon the icon to search for
     * @return List of categories with the specified icon
     */
    List<Category> findByIcon(String icon);

    /**
     * Count categories that have at least one expense.
     * 
     * @return the count of categories with expenses
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Category c INNER JOIN c.expenses e")
    long countCategoriesWithExpenses();
}