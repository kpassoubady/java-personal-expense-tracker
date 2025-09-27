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
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find a category by its name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Find all categories ordered by name alphabetically.
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Check if a category exists by name (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find categories that contain the given text in their name or description.
     */
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Category> findByNameOrDescriptionContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Get category statistics (name, expense count, total amount).
     */
    @Query("SELECT c.name, COUNT(e), COALESCE(SUM(e.amount), 0) " +
           "FROM Category c LEFT JOIN c.expenses e " +
           "GROUP BY c.id, c.name " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategoryStatistics();
}
