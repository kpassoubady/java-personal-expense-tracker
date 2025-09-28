package com.expensetracker.app.service;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.exception.EntityNotFoundException;
import com.expensetracker.app.exception.ValidationException;
import com.expensetracker.app.repository.CategoryRepository;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private Category savedCategory;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        testCategory = createTestCategory("Food", "Food and dining", "#28a745", "fas fa-utensils");
        savedCategory = createSavedCategory(1L, "Food", "Food and dining", "#28a745", "fas fa-utensils");
        testExpense = createTestExpense(savedCategory);
    }

    // Custom Matchers
    public static Matcher<Category> hasValidAuditFields() {
        return new TypeSafeMatcher<Category>() {
            @Override
            protected boolean matchesSafely(Category category) {
                return category.getCreatedAt() != null &&
                       category.getUpdatedAt() != null &&
                       !category.getUpdatedAt().isBefore(category.getCreatedAt());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("category with valid audit fields");
            }
        };
    }

    public static Matcher<Category> hasSameCorePropertiesAs(Category expected) {
        return new TypeSafeMatcher<Category>() {
            @Override
            protected boolean matchesSafely(Category actual) {
                return Objects.equals(actual.getName(), expected.getName()) &&
                       Objects.equals(actual.getDescription(), expected.getDescription()) &&
                       Objects.equals(actual.getColor(), expected.getColor()) &&
                       Objects.equals(actual.getIcon(), expected.getIcon());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("category with same core properties as ").appendValue(expected);
            }
        };
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should return all categories ordered by name")
        void getAllCategories_ShouldReturnAllCategories() {
            // Given
            List<Category> categories = Arrays.asList(
                createSavedCategory(1L, "Food", "Food expenses", "#28a745", "fas fa-utensils"),
                createSavedCategory(2L, "Transport", "Transportation", "#007bff", "fas fa-car")
            );
            when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(categories);

            // When
            List<Category> result = categoryService.getAllCategories();

            // Then
            assertThat(result, hasSize(2));
            assertThat(result.get(0).getName(), is("Food"));
            assertThat(result.get(1).getName(), is("Transport"));
            verify(categoryRepository).findAllByOrderByNameAsc();
        }

        @Test
        @DisplayName("Should find category by ID when exists")
        void findById_ShouldReturnCategory_WhenExists() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(savedCategory));

            // When
            Optional<Category> result = categoryService.findById(1L);

            // Then
            assertTrue(result.isPresent());
            assertThat(result.get().getName(), is("Food"));
            verify(categoryRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty optional when category not found")
        void findById_ShouldReturnEmpty_WhenNotExists() {
            // Given
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Category> result = categoryService.findById(999L);

            // Then
            assertFalse(result.isPresent());
            verify(categoryRepository).findById(999L);
        }

        @Test
        @DisplayName("Should throw ValidationException when ID is null")
        void findById_ShouldThrowException_WhenIdIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.findById(null));
            assertThat(exception.getMessage(), containsString("Category ID cannot be null"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should get category by ID or throw EntityNotFoundException")
        void getCategoryById_ShouldReturnCategory_WhenExists() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(savedCategory));

            // When
            Category result = categoryService.getCategoryById(1L);

            // Then
            assertThat(result, is(savedCategory));
            verify(categoryRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when category not found")
        void getCategoryById_ShouldThrowException_WhenNotFound() {
            // Given
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.getCategoryById(999L));
            assertThat(exception.getMessage(), containsString("Category"));
            assertThat(exception.getMessage(), containsString("999"));
        }

        @Test
        @DisplayName("Should save new category successfully")
        void saveCategory_ShouldReturnSavedCategory() {
            // Given
            when(categoryRepository.findByNameIgnoreCase("Food")).thenReturn(Optional.empty());
            when(categoryRepository.save(testCategory)).thenReturn(savedCategory);

            // When
            Category result = categoryService.saveCategory(testCategory);

            // Then
            assertThat(result, is(savedCategory));
            assertThat(result, hasValidAuditFields());
            verify(categoryRepository).findByNameIgnoreCase("Food");
            verify(categoryRepository).save(testCategory);
        }

        @Test
        @DisplayName("Should update existing category successfully")
        void updateCategory_ShouldUpdateCategory() {
            // Given
            Category updatedCategory = createSavedCategory(1L, "Updated Food", "Updated description", "#ff0000", "fas fa-pizza");
            when(categoryRepository.findByNameIgnoreCase("Updated Food")).thenReturn(Optional.empty());
            when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);

            // When
            Category result = categoryService.saveCategory(updatedCategory);

            // Then
            assertThat(result.getName(), is("Updated Food"));
            assertThat(result.getDescription(), is("Updated description"));
            assertThat(result, hasValidAuditFields());
            verify(categoryRepository).save(updatedCategory);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw ValidationException for null category")
        void saveCategory_ShouldThrowException_WhenCategoryIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(null));
            assertThat(exception.getMessage(), containsString("Category cannot be null"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw ValidationException for empty name")
        void saveCategory_ShouldThrowException_WhenNameIsEmpty() {
            // Given
            testCategory.setName("");

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("Category name is required"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw ValidationException for null name")
        void saveCategory_ShouldThrowException_WhenNameIsNull() {
            // Given
            testCategory.setName(null);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("Category name is required"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw ValidationException for name exceeding 100 characters")
        void saveCategory_ShouldThrowException_WhenNameTooLong() {
            // Given
            testCategory.setName("A".repeat(101));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("must not exceed 100 characters"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw ValidationException for invalid color format")
        void saveCategory_ShouldThrowException_WhenColorInvalid() {
            // Given
            testCategory.setColor("invalid-color");

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("valid hex color code"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw ValidationException for icon exceeding 50 characters")
        void saveCategory_ShouldThrowException_WhenIconTooLong() {
            // Given
            testCategory.setIcon("A".repeat(51));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("Icon must not exceed 50 characters"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw ValidationException for description exceeding 255 characters")
        void saveCategory_ShouldThrowException_WhenDescriptionTooLong() {
            // Given
            testCategory.setDescription("A".repeat(256));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("Description must not exceed 255 characters"));
            verifyNoInteractions(categoryRepository);
        }
    }

    @Nested
    @DisplayName("Duplicate Name Handling")
    class DuplicateNameHandling {

        @Test
        @DisplayName("Should throw ValidationException for duplicate name on new category")
        void saveCategory_ShouldThrowException_WhenDuplicateNameForNewCategory() {
            // Given
            when(categoryRepository.findByNameIgnoreCase("Food")).thenReturn(Optional.of(savedCategory));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("Category with this name already exists"));
            verify(categoryRepository).findByNameIgnoreCase("Food");
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException for duplicate name on update")
        void saveCategory_ShouldThrowException_WhenDuplicateNameForUpdate() {
            // Given
            Category existingCategory = createSavedCategory(2L, "Transport", "Transport expenses", "#007bff", "fas fa-car");
            Category updatingCategory = createSavedCategory(1L, "Transport", "Food expenses", "#28a745", "fas fa-utensils");
            
            when(categoryRepository.findByNameIgnoreCase("Transport")).thenReturn(Optional.of(existingCategory));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(updatingCategory));
            assertThat(exception.getMessage(), containsString("Category with this name already exists"));
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow updating category with same name")
        void saveCategory_ShouldAllowUpdate_WhenSameCategoryName() {
            // Given
            Category updatingCategory = createSavedCategory(1L, "Food", "Updated description", "#28a745", "fas fa-utensils");
            when(categoryRepository.findByNameIgnoreCase("Food")).thenReturn(Optional.of(savedCategory));
            when(categoryRepository.save(updatingCategory)).thenReturn(updatingCategory);

            // When
            Category result = categoryService.saveCategory(updatingCategory);

            // Then
            assertThat(result.getDescription(), is("Updated description"));
            verify(categoryRepository).save(updatingCategory);
        }

        @Test
        @DisplayName("Should check case-insensitive duplicate names")
        void saveCategory_ShouldThrowException_WhenDuplicateNameCaseInsensitive() {
            // Given
            testCategory.setName("FOOD");
            when(categoryRepository.findByNameIgnoreCase("FOOD")).thenReturn(Optional.of(savedCategory));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("Category with this name already exists"));
        }
    }

    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {

        @Test
        @DisplayName("Should delete category successfully when no expenses")
        void deleteCategory_ShouldRemoveCategory_WhenNoExpenses() {
            // Given
            Category categoryWithoutExpenses = createSavedCategory(1L, "Food", "Food expenses", "#28a745", "fas fa-utensils");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryWithoutExpenses));
            doNothing().when(categoryRepository).deleteById(1L);

            // When
            categoryService.deleteCategory(1L);

            // Then
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ValidationException when deleting category with expenses")
        void deleteCategory_ShouldThrowException_WhenCategoryHasExpenses() {
            // Given
            Category categoryWithExpenses = createSavedCategory(1L, "Food", "Food expenses", "#28a745", "fas fa-utensils");
            categoryWithExpenses.getExpenses().add(testExpense);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryWithExpenses));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> categoryService.deleteCategory(1L));
            assertThat(exception.getMessage(), containsString("Cannot delete category because it has associated expenses"));
            verify(categoryRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when deletion ID is null")
        void deleteCategory_ShouldThrowException_WhenIdIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> categoryService.deleteCategory(null));
            assertThat(exception.getMessage(), containsString("Category ID cannot be null"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should handle deletion errors gracefully")
        void deleteCategory_ShouldThrowException_WhenDeletionFails() {
            // Given
            Category categoryWithoutExpenses = createSavedCategory(1L, "Food", "Food expenses", "#28a745", "fas fa-utensils");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryWithoutExpenses));
            doThrow(new DataIntegrityViolationException("Database error")).when(categoryRepository).deleteById(1L);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> categoryService.deleteCategory(1L));
            assertThat(exception.getMessage(), containsString("Failed to delete category"));
        }
    }

    @Nested
    @DisplayName("Category Statistics Tests")
    class CategoryStatisticsTests {

        @Test
        @DisplayName("Should return category statistics correctly")
        void getCategoryStatistics_ShouldReturnStats() {
            // Given
            List<Object[]> mockStats = Arrays.asList(
                new Object[]{"Food", 10L, new BigDecimal("250.50")},
                new Object[]{"Transport", 5L, new BigDecimal("150.00")},
                new Object[]{"Entertainment", 0L, new BigDecimal("0.00")}
            );
            when(categoryRepository.getCategoryStatistics()).thenReturn(mockStats);

            // When
            List<Object[]> result = categoryService.getCategoryStatistics();

            // Then
            assertThat(result, hasSize(3));
            assertThat(result.get(0)[0], is("Food"));
            assertThat(result.get(0)[1], is(10L));
            assertThat(result.get(0)[2], is(new BigDecimal("250.50")));
            verify(categoryRepository).getCategoryStatistics();
        }

        @Test
        @DisplayName("Should return empty list when no statistics available")
        void getCategoryStatistics_ShouldReturnEmptyList_WhenNoData() {
            // Given
            when(categoryRepository.getCategoryStatistics()).thenReturn(Collections.emptyList());

            // When
            List<Object[]> result = categoryService.getCategoryStatistics();

            // Then
            assertThat(result, hasSize(0));
            verify(categoryRepository).getCategoryStatistics();
        }

        @Test
        @DisplayName("Should get category count")
        void getCategoryCount_ShouldReturnCount() {
            // Given
            when(categoryRepository.count()).thenReturn(5L);

            // When
            long result = categoryService.getCategoryCount();

            // Then
            assertThat(result, is(5L));
            verify(categoryRepository).count();
        }
    }

    @Nested
    @DisplayName("Search Functionality Tests")
    class SearchFunctionalityTests {

        @Test
        @DisplayName("Should search categories by name")
        void searchCategories_ShouldReturnMatchingCategories_WhenSearchingByName() {
            // Given
            String searchText = "food";
            List<Category> matchingCategories = Arrays.asList(savedCategory);
            when(categoryRepository.findByNameOrDescriptionContainingIgnoreCase("food"))
                .thenReturn(matchingCategories);

            // When
            List<Category> result = categoryService.searchCategories(searchText);

            // Then
            assertThat(result, hasSize(1));
            assertThat(result.get(0).getName(), is("Food"));
            verify(categoryRepository).findByNameOrDescriptionContainingIgnoreCase("food");
        }

        @Test
        @DisplayName("Should search categories by description")
        void searchCategories_ShouldReturnMatchingCategories_WhenSearchingByDescription() {
            // Given
            String searchText = "dining";
            List<Category> matchingCategories = Arrays.asList(savedCategory);
            when(categoryRepository.findByNameOrDescriptionContainingIgnoreCase("dining"))
                .thenReturn(matchingCategories);

            // When
            List<Category> result = categoryService.searchCategories(searchText);

            // Then
            assertThat(result, hasSize(1));
            verify(categoryRepository).findByNameOrDescriptionContainingIgnoreCase("dining");
        }

        @Test
        @DisplayName("Should trim search text before searching")
        void searchCategories_ShouldTrimSearchText() {
            // Given
            String searchText = "  food  ";
            when(categoryRepository.findByNameOrDescriptionContainingIgnoreCase("food"))
                .thenReturn(Collections.emptyList());

            // When
            categoryService.searchCategories(searchText);

            // Then
            verify(categoryRepository).findByNameOrDescriptionContainingIgnoreCase("food");
        }

        @Test
        @DisplayName("Should throw ValidationException for null search text")
        void searchCategories_ShouldThrowException_WhenSearchTextIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> categoryService.searchCategories(null));
            assertThat(exception.getMessage(), containsString("Search text cannot be null or empty"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw ValidationException for empty search text")
        void searchCategories_ShouldThrowException_WhenSearchTextIsEmpty() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> categoryService.searchCategories(""));
            assertThat(exception.getMessage(), containsString("Search text cannot be null or empty"));
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void searchCategories_ShouldReturnEmptyList_WhenNoMatches() {
            // Given
            String searchText = "nonexistent";
            when(categoryRepository.findByNameOrDescriptionContainingIgnoreCase("nonexistent"))
                .thenReturn(Collections.emptyList());

            // When
            List<Category> result = categoryService.searchCategories(searchText);

            // Then
            assertThat(result, hasSize(0));
            verify(categoryRepository).findByNameOrDescriptionContainingIgnoreCase("nonexistent");
        }
    }

    @Nested
    @DisplayName("Batch Operations Tests")
    class BatchOperationsTests {

        @Test
        @DisplayName("Should create default categories when none exist")
        void createDefaultCategories_ShouldCreateCategories_WhenNoneExist() {
            // Given
            when(categoryRepository.count()).thenReturn(0L);
            when(categoryRepository.saveAll(any())).thenReturn(Collections.emptyList());

            // When
            categoryService.createDefaultCategories();

            // Then
            verify(categoryRepository).count();
            verify(categoryRepository).saveAll(argThat(categories -> {
                List<Category> categoryList = (List<Category>) categories;
                return categoryList.size() == 5 && 
                       categoryList.stream().anyMatch(cat -> "Food & Dining".equals(cat.getName()));
            }));
        }

        @Test
        @DisplayName("Should not create default categories when some exist")
        void createDefaultCategories_ShouldNotCreateCategories_WhenSomeExist() {
            // Given
            when(categoryRepository.count()).thenReturn(3L);

            // When
            categoryService.createDefaultCategories();

            // Then
            verify(categoryRepository).count();
            verify(categoryRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Audit Field Tests")
    class AuditFieldTests {

        @Test
        @DisplayName("Should update audit fields on save")
        void saveCategory_ShouldUpdateAuditFields() {
            // Given
            LocalDateTime beforeSave = LocalDateTime.now();
            when(categoryRepository.findByNameIgnoreCase("Food")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category category = invocation.getArgument(0);
                category.setCreatedAt(LocalDateTime.now());
                category.setUpdatedAt(LocalDateTime.now());
                return category;
            });

            // When
            Category result = categoryService.saveCategory(testCategory);

            // Then
            assertThat(result.getCreatedAt(), is(notNullValue()));
            assertThat(result.getUpdatedAt(), is(notNullValue()));
            assertThat(result.getCreatedAt(), is(greaterThanOrEqualTo(beforeSave)));
            assertThat(result.getUpdatedAt(), is(greaterThanOrEqualTo(result.getCreatedAt())));
        }
    }

    @Nested
    @DisplayName("Transaction Rollback Tests")
    class TransactionRollbackTests {

        @Test
        @DisplayName("Should rollback transaction on save failure")
        void saveCategory_ShouldRollbackTransaction_WhenSaveFails() {
            // Given
            when(categoryRepository.findByNameIgnoreCase("Food")).thenReturn(Optional.empty());
            when(categoryRepository.save(testCategory))
                .thenThrow(new DataIntegrityViolationException("Database constraint violation"));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> categoryService.saveCategory(testCategory));
            assertThat(exception.getMessage(), containsString("Failed to save category"));
            assertThat(exception.getCause(), instanceOf(DataIntegrityViolationException.class));
        }

        @Test
        @DisplayName("Should handle concurrent modification gracefully")
        void deleteCategory_ShouldHandleConcurrentModification() {
            // Given
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(savedCategory));
            doThrow(new DataIntegrityViolationException("Concurrent modification"))
                .when(categoryRepository).deleteById(1L);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> categoryService.deleteCategory(1L));
            assertThat(exception.getMessage(), containsString("Failed to delete category"));
        }
    }

    @Nested
    @DisplayName("Category Existence Tests")
    class CategoryExistenceTests {

        @Test
        @DisplayName("Should return true when category exists by name")
        void categoryExists_ShouldReturnTrue_WhenCategoryExists() {
            // Given
            when(categoryRepository.findByNameIgnoreCase("Food")).thenReturn(Optional.of(savedCategory));

            // When
            boolean result = categoryService.categoryExists("Food");

            // Then
            assertTrue(result);
            verify(categoryRepository).findByNameIgnoreCase("Food");
        }

        @Test
        @DisplayName("Should return false when category does not exist")
        void categoryExists_ShouldReturnFalse_WhenCategoryDoesNotExist() {
            // Given
            when(categoryRepository.findByNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

            // When
            boolean result = categoryService.categoryExists("NonExistent");

            // Then
            assertFalse(result);
            verify(categoryRepository).findByNameIgnoreCase("NonExistent");
        }

        @Test
        @DisplayName("Should check existence by name case-insensitively")
        void existsByName_ShouldReturnTrue_WhenCategoryExistsCaseInsensitive() {
            // Given
            when(categoryRepository.findByNameIgnoreCase("food")).thenReturn(Optional.of(savedCategory));

            // When
            boolean result = categoryService.existsByName("food");

            // Then
            assertTrue(result);
            verify(categoryRepository).findByNameIgnoreCase("food");
        }

        @Test
        @DisplayName("Should return false for null or empty name")
        void existsByName_ShouldReturnFalse_WhenNameIsNullOrEmpty() {
            // When & Then
            assertFalse(categoryService.existsByName(null));
            assertFalse(categoryService.existsByName(""));
            assertFalse(categoryService.existsByName("   "));
            verifyNoInteractions(categoryRepository);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large category lists efficiently")
        void getAllCategories_ShouldHandleLargeLists() {
            // Given
            List<Category> largeList = createLargeCategoryList(1000);
            when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(largeList);

            // When
            long startTime = System.currentTimeMillis();
            List<Category> result = categoryService.getAllCategories();
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(result, hasSize(1000));
            assertThat("Operation should complete within reasonable time", 
                endTime - startTime, lessThan(1000L)); // Less than 1 second
            verify(categoryRepository).findAllByOrderByNameAsc();
        }

        @Test
        @DisplayName("Should handle multiple concurrent searches efficiently")
        void searchCategories_ShouldHandleConcurrentSearches() {
            // Given
            when(categoryRepository.findByNameOrDescriptionContainingIgnoreCase(anyString()))
                .thenReturn(Arrays.asList(savedCategory));

            // When & Then
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    categoryService.searchCategories("test" + i);
                }
            });

            verify(categoryRepository, times(100)).findByNameOrDescriptionContainingIgnoreCase(anyString());
        }
    }

    // Helper methods
    private Category createTestCategory(String name, String description, String color, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setColor(color);
        category.setIcon(icon);
        return category;
    }

    private Category createSavedCategory(Long id, String name, String description, String color, String icon) {
        Category category = createTestCategory(name, description, color, icon);
        category.setId(id);
        category.setCreatedAt(LocalDateTime.now().minusDays(1));
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    private Expense createTestExpense(Category category) {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Test expense");
        expense.setAmount(new BigDecimal("10.00"));
        expense.setExpenseDate(LocalDate.now());
        expense.setCategory(category);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        return expense;
    }

    private List<Category> createLargeCategoryList(int size) {
        List<Category> categories = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            categories.add(createSavedCategory((long) i, "Category" + i, "Description" + i, "#" + String.format("%06X", i % 0xFFFFFF), "fas fa-tag"));
        }
        return categories;
    }
}
