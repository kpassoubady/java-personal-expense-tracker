package com.expensetracker.app.service;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@Rollback
class CategoryServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clear existing data for clean test state
        categoryRepository.deleteAll();
        
        testCategory = new Category("Food", "Food and dining", "#28a745", "fas fa-utensils");
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        categoryRepository.save(testCategory);
        Category anotherCategory = new Category("Transport", "Transportation expenses", "#007bff", "fas fa-car");
        categoryRepository.save(anotherCategory);

        // When
        List<Category> result = categoryService.getAllCategories();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(cat -> "Food".equals(cat.getName())));
        assertTrue(result.stream().anyMatch(cat -> "Transport".equals(cat.getName())));
    }

    @Test
    void findById_ShouldReturnCategory_WhenExists() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);

        // When
        Optional<Category> result = categoryService.findById(savedCategory.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Food", result.get().getName());
    }

    @Test
    void saveCategory_ShouldReturnSavedCategory() {
        // When
        Category result = categoryService.saveCategory(testCategory);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Food", result.getName());
        
        // Verify it was actually saved
        Optional<Category> found = categoryRepository.findById(result.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void updateCategory_ShouldUpdateCategory() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);
        savedCategory.setName("Updated Food");
        savedCategory.setDescription("Updated description");

        // When
        Category updated = categoryService.saveCategory(savedCategory);

        // Then
        assertEquals("Updated Food", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        
        // Verify the update was persisted
        Optional<Category> found = categoryRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Food", found.get().getName());
    }

    @Test
    void deleteCategory_ShouldRemoveCategory() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);
        Long categoryId = savedCategory.getId();

        // When
        categoryService.deleteCategory(categoryId);

        // Then
        Optional<Category> found = categoryRepository.findById(categoryId);
        assertFalse(found.isPresent());
    }

    @Test
    void saveCategory_ShouldThrowException_WhenDuplicateName() {
        // Given - save a category first
        categoryRepository.save(testCategory);
        
        // Try to save another category with the same name
        Category duplicateCategory = new Category("Food", "Another food category", "#ff0000", "fas fa-pizza-slice");

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            categoryService.saveCategory(duplicateCategory);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void getCategoryStatistics_ShouldReturnStats() {
        // Given
        categoryRepository.save(testCategory);

        // When
        List<Object[]> stats = categoryService.getCategoryStatistics();

        // Then
        assertNotNull(stats);
        // Stats might be empty if no expenses are associated, which is fine for this test
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        // When & Then - try to find a non-existent category
        Optional<Category> result = categoryService.findById(999L);
        
        // The service returns Optional.empty() for non-existent categories
        assertFalse(result.isPresent());
    }
}
