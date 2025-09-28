package com.expensetracker.app.repository;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ExpenseRepository focusing on database interactions,
 * performance, concurrency, and complex query scenarios.
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false",
    "logging.level.org.hibernate.SQL=DEBUG"
})
@DisplayName("ExpenseRepository Integration Tests")
class ExpenseRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category foodCategory;
    private Category transportCategory;
    private Category entertainmentCategory;
    private Category healthCategory;

    @BeforeEach
    void setUp() {
        // Clear existing data
        expenseRepository.deleteAll();
        categoryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test categories
        foodCategory = createAndPersistCategory("Food", "Food and dining", "#28a745", "fas fa-utensils");
        transportCategory = createAndPersistCategory("Transport", "Transportation costs", "#007bff", "fas fa-car");
        entertainmentCategory = createAndPersistCategory("Entertainment", "Entertainment expenses", "#dc3545", "fas fa-film");
        healthCategory = createAndPersistCategory("Health", "Healthcare expenses", "#ffc107", "fas fa-heartbeat");
    }

    private Category createAndPersistCategory(String name, String description, String color, String icon) {
        Category category = new Category(name, description, color, icon);
        entityManager.persistAndFlush(category);
        return category;
    }

    private Expense createExpense(String description, BigDecimal amount, LocalDate date, Category category) {
        Expense expense = new Expense(description, amount, date, category);
        return expense;
    }

    @Nested
    @DisplayName("Custom Query Methods Tests")
    class CustomQueryMethodsTests {

        @Test
        @DisplayName("Should find expenses by category with proper data mapping")
        void testFindByCategory() {
            // Given
            Expense expense1 = createExpense("Lunch", new BigDecimal("15.50"), LocalDate.now(), foodCategory);
            Expense expense2 = createExpense("Dinner", new BigDecimal("25.00"), LocalDate.now().minusDays(1), foodCategory);
            Expense expense3 = createExpense("Bus ticket", new BigDecimal("3.50"), LocalDate.now(), transportCategory);
            
            entityManager.persistAndFlush(expense1);
            entityManager.persistAndFlush(expense2);
            entityManager.persistAndFlush(expense3);

            // When
            List<Expense> foodExpenses = expenseRepository.findByCategory(foodCategory);
            List<Expense> transportExpenses = expenseRepository.findByCategory(transportCategory);

            // Then
            assertThat(foodExpenses).hasSize(2);
            assertThat(foodExpenses)
                .extracting(Expense::getDescription)
                .containsExactlyInAnyOrder("Lunch", "Dinner");
            assertThat(foodExpenses)
                .extracting(Expense::getAmount)
                .containsExactlyInAnyOrder(new BigDecimal("15.50"), new BigDecimal("25.00"));

            assertThat(transportExpenses).hasSize(1);
            assertThat(transportExpenses.get(0).getDescription()).isEqualTo("Bus ticket");
        }

        @Test
        @DisplayName("Should find expenses by date range with boundary conditions")
        void testFindByExpenseDateBetween() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(5);
            LocalDate endDate = LocalDate.now().minusDays(1);
            
            Expense withinRange1 = createExpense("Within range 1", new BigDecimal("10.00"), startDate, foodCategory);
            Expense withinRange2 = createExpense("Within range 2", new BigDecimal("20.00"), endDate, foodCategory);
            Expense beforeRange = createExpense("Before range", new BigDecimal("30.00"), startDate.minusDays(1), foodCategory);
            Expense afterRange = createExpense("After range", new BigDecimal("40.00"), startDate.minusDays(2), foodCategory);

            entityManager.persistAndFlush(withinRange1);
            entityManager.persistAndFlush(withinRange2);
            entityManager.persistAndFlush(beforeRange);
            entityManager.persistAndFlush(afterRange);

            // When
            List<Expense> expensesInRange = expenseRepository.findByExpenseDateBetween(startDate, endDate);

            // Then
            assertThat(expensesInRange).hasSize(2);
            assertThat(expensesInRange)
                .extracting(Expense::getDescription)
                .containsExactlyInAnyOrder("Within range 1", "Within range 2");
        }

        @Test
        @DisplayName("Should find expenses by category and date range with proper ordering")
        void testFindByCategoryAndExpenseDateBetweenOrderByExpenseDateDesc() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(3);
            LocalDate endDate = LocalDate.now();
            
            Expense recent = createExpense("Recent food", new BigDecimal("15.00"), LocalDate.now(), foodCategory);
            Expense middle = createExpense("Middle food", new BigDecimal("20.00"), LocalDate.now().minusDays(1), foodCategory);
            Expense older = createExpense("Older food", new BigDecimal("25.00"), LocalDate.now().minusDays(2), foodCategory);
            Expense transport = createExpense("Transport", new BigDecimal("5.00"), LocalDate.now(), transportCategory);

            entityManager.persistAndFlush(recent);
            entityManager.persistAndFlush(middle);
            entityManager.persistAndFlush(older);
            entityManager.persistAndFlush(transport);

            // When
            List<Expense> result = expenseRepository.findByCategoryAndExpenseDateBetweenOrderByExpenseDateDesc(
                foodCategory, startDate, endDate);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result)
                .extracting(Expense::getDescription)
                .containsExactly("Recent food", "Middle food", "Older food");
        }

        @Test
        @DisplayName("Should search expenses by description ignoring case")
        void testFindByDescriptionContainingIgnoreCase() {
            // Given
            Expense lunch1 = createExpense("McDonald's lunch", new BigDecimal("12.50"), LocalDate.now(), foodCategory);
            Expense lunch2 = createExpense("Business LUNCH meeting", new BigDecimal("45.00"), LocalDate.now(), foodCategory);
            Expense dinner = createExpense("Italian dinner", new BigDecimal("30.00"), LocalDate.now(), foodCategory);

            entityManager.persistAndFlush(lunch1);
            entityManager.persistAndFlush(lunch2);
            entityManager.persistAndFlush(dinner);

            // When
            List<Expense> lunchExpenses = expenseRepository.findByDescriptionContainingIgnoreCase("lunch");
            List<Expense> italianExpenses = expenseRepository.findByDescriptionContainingIgnoreCase("ITALIAN");

            // Then
            assertThat(lunchExpenses).hasSize(2);
            assertThat(lunchExpenses)
                .extracting(Expense::getDescription)
                .containsExactlyInAnyOrder("McDonald's lunch", "Business LUNCH meeting");

            assertThat(italianExpenses).hasSize(1);
            assertThat(italianExpenses.get(0).getDescription()).isEqualTo("Italian dinner");
        }

        @Test
        @DisplayName("Should find expenses by amount range")
        void testFindByAmountBetween() {
            // Given
            Expense cheap = createExpense("Cheap meal", new BigDecimal("5.00"), LocalDate.now(), foodCategory);
            Expense moderate = createExpense("Moderate meal", new BigDecimal("15.00"), LocalDate.now(), foodCategory);
            Expense expensive = createExpense("Expensive meal", new BigDecimal("50.00"), LocalDate.now(), foodCategory);

            entityManager.persistAndFlush(cheap);
            entityManager.persistAndFlush(moderate);
            entityManager.persistAndFlush(expensive);

            // When
            List<Expense> moderateExpenses = expenseRepository.findByAmountBetween(
                new BigDecimal("10.00"), new BigDecimal("30.00"));

            // Then
            assertThat(moderateExpenses).hasSize(1);
            assertThat(moderateExpenses.get(0).getDescription()).isEqualTo("Moderate meal");
        }

        @Test
        @DisplayName("Should find expenses by amount greater than threshold")
        void testFindByAmountGreaterThan() {
            // Given
            Expense small = createExpense("Small expense", new BigDecimal("5.00"), LocalDate.now(), foodCategory);
            Expense large1 = createExpense("Large expense 1", new BigDecimal("25.00"), LocalDate.now(), foodCategory);
            Expense large2 = createExpense("Large expense 2", new BigDecimal("35.00"), LocalDate.now(), foodCategory);

            entityManager.persistAndFlush(small);
            entityManager.persistAndFlush(large1);
            entityManager.persistAndFlush(large2);

            // When
            List<Expense> largeExpenses = expenseRepository.findByAmountGreaterThan(new BigDecimal("20.00"));

            // Then
            assertThat(largeExpenses).hasSize(2);
            assertThat(largeExpenses)
                .extracting(Expense::getDescription)
                .containsExactlyInAnyOrder("Large expense 1", "Large expense 2");
        }
    }

    @Nested
    @DisplayName("Custom JPQL and Native Query Tests")
    class CustomQueryTests {

        @Test
        @DisplayName("Should calculate total expenses correctly")
        void testGetTotalExpenses() {
            // Given
            Expense expense1 = createExpense("Expense 1", new BigDecimal("10.50"), LocalDate.now(), foodCategory);
            Expense expense2 = createExpense("Expense 2", new BigDecimal("25.25"), LocalDate.now(), transportCategory);
            Expense expense3 = createExpense("Expense 3", new BigDecimal("15.75"), LocalDate.now(), entertainmentCategory);

            entityManager.persistAndFlush(expense1);
            entityManager.persistAndFlush(expense2);
            entityManager.persistAndFlush(expense3);

            // When
            BigDecimal total = expenseRepository.getTotalExpenses();

            // Then
            assertThat(total).isEqualByComparingTo(new BigDecimal("51.50"));
        }

        @Test
        @DisplayName("Should calculate total amount by category")
        void testGetTotalAmountByCategory() {
            // Given
            Expense food1 = createExpense("Food 1", new BigDecimal("12.50"), LocalDate.now(), foodCategory);
            Expense food2 = createExpense("Food 2", new BigDecimal("18.25"), LocalDate.now(), foodCategory);
            Expense transport1 = createExpense("Transport 1", new BigDecimal("5.00"), LocalDate.now(), transportCategory);

            entityManager.persistAndFlush(food1);
            entityManager.persistAndFlush(food2);
            entityManager.persistAndFlush(transport1);

            // When
            BigDecimal foodTotal = expenseRepository.getTotalAmountByCategory(foodCategory);
            BigDecimal transportTotal = expenseRepository.getTotalAmountByCategory(transportCategory);
            BigDecimal healthTotal = expenseRepository.getTotalAmountByCategory(healthCategory);

            // Then
            assertThat(foodTotal).isEqualByComparingTo(new BigDecimal("30.75"));
            assertThat(transportTotal).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(healthTotal).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate total amount by date range")
        void testGetTotalAmountByDateRange() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(2);
            LocalDate endDate = LocalDate.now();
            
            Expense inRange1 = createExpense("In range 1", new BigDecimal("10.00"), startDate, foodCategory);
            Expense inRange2 = createExpense("In range 2", new BigDecimal("15.50"), endDate, foodCategory);
            Expense outOfRange = createExpense("Out of range", new BigDecimal("20.00"), startDate.minusDays(1), foodCategory);

            entityManager.persistAndFlush(inRange1);
            entityManager.persistAndFlush(inRange2);
            entityManager.persistAndFlush(outOfRange);

            // When
            BigDecimal total = expenseRepository.getTotalAmountByDateRange(startDate, endDate);

            // Then
            assertThat(total).isEqualByComparingTo(new BigDecimal("25.50"));
        }

        @Test
        @DisplayName("Should get expense summary by category with proper aggregation")
        void testGetExpenseSummaryByCategory() {
            // Given
            Expense food1 = createExpense("Food 1", new BigDecimal("20.00"), LocalDate.now(), foodCategory);
            Expense food2 = createExpense("Food 2", new BigDecimal("30.00"), LocalDate.now(), foodCategory);
            Expense transport1 = createExpense("Transport 1", new BigDecimal("10.00"), LocalDate.now(), transportCategory);

            entityManager.persistAndFlush(food1);
            entityManager.persistAndFlush(food2);
            entityManager.persistAndFlush(transport1);

            // When
            List<Object[]> summary = expenseRepository.getExpenseSummaryByCategory();

            // Then
            assertThat(summary).hasSize(4); // All categories should be included (LEFT JOIN)
            
            // Find food category in results
            Object[] foodResult = summary.stream()
                .filter(row -> "Food".equals(row[0]))
                .findFirst()
                .orElseThrow();
            assertThat(foodResult[1]).isEqualTo(new BigDecimal("50.00"));

            // Find transport category in results
            Object[] transportResult = summary.stream()
                .filter(row -> "Transport".equals(row[0]))
                .findFirst()
                .orElseThrow();
            assertThat(transportResult[1]).isEqualTo(new BigDecimal("10.00"));

            // Categories with no expenses should have zero total
            Object[] healthResult = summary.stream()
                .filter(row -> "Health".equals(row[0]))
                .findFirst()
                .orElseThrow();
            assertThat(healthResult[1]).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should get expenses by category for analytics")
        void testGetExpensesByCategory() {
            // Given
            Expense food1 = createExpense("Food 1", new BigDecimal("25.00"), LocalDate.now(), foodCategory);
            Expense transport1 = createExpense("Transport 1", new BigDecimal("15.00"), LocalDate.now(), transportCategory);

            entityManager.persistAndFlush(food1);
            entityManager.persistAndFlush(transport1);

            // When
            List<Object[]> breakdown = expenseRepository.getExpensesByCategory();

            // Then
            assertThat(breakdown).hasSize(2); // Only categories with expenses
            
            Object[] foodResult = breakdown.stream()
                .filter(row -> "Food".equals(row[0]))
                .findFirst()
                .orElseThrow();
            assertThat(foodResult[1]).isEqualTo(new BigDecimal("25.00"));

            Object[] transportResult = breakdown.stream()
                .filter(row -> "Transport".equals(row[0]))
                .findFirst()
                .orElseThrow();
            assertThat(transportResult[1]).isEqualTo(new BigDecimal("15.00"));
        }

        @Test
        @DisplayName("Should find top recent expenses using native query")
        void testFindTopRecentExpenses() {
            // Given
            LocalDate today = LocalDate.now();
            Expense recent1 = createExpense("Recent 1", new BigDecimal("10.00"), today, foodCategory);
            Expense recent2 = createExpense("Recent 2", new BigDecimal("15.00"), today.minusDays(1), transportCategory);
            Expense recent3 = createExpense("Recent 3", new BigDecimal("20.00"), today.minusDays(2), entertainmentCategory);
            Expense older = createExpense("Older", new BigDecimal("25.00"), today.minusDays(10), healthCategory);

            entityManager.persistAndFlush(recent1);
            try { Thread.sleep(10); } catch (InterruptedException e) { /* ignore */ } // Ensure different created_at timestamps
            entityManager.persistAndFlush(recent2);
            try { Thread.sleep(10); } catch (InterruptedException e) { /* ignore */ }
            entityManager.persistAndFlush(recent3);
            try { Thread.sleep(10); } catch (InterruptedException e) { /* ignore */ }
            entityManager.persistAndFlush(older);

            // When
            List<Expense> topRecent = expenseRepository.findTopRecentExpenses(3);

            // Then
            assertThat(topRecent).hasSize(3);
            assertThat(topRecent.get(0).getDescription()).isEqualTo("Recent 1");
            assertThat(topRecent.get(1).getDescription()).isEqualTo("Recent 2");
            assertThat(topRecent.get(2).getDescription()).isEqualTo("Recent 3");
        }

        @Test
        @DisplayName("Should find expenses by category ID")
        void testFindByCategoryId() {
            // Given
            Expense food1 = createExpense("Food 1", new BigDecimal("10.00"), LocalDate.now(), foodCategory);
            Expense food2 = createExpense("Food 2", new BigDecimal("15.00"), LocalDate.now(), foodCategory);
            Expense transport1 = createExpense("Transport 1", new BigDecimal("5.00"), LocalDate.now(), transportCategory);

            entityManager.persistAndFlush(food1);
            entityManager.persistAndFlush(food2);
            entityManager.persistAndFlush(transport1);

            // When
            List<Expense> foodExpenses = expenseRepository.findByCategoryId(foodCategory.getId());
            List<Expense> transportExpenses = expenseRepository.findByCategoryId(transportCategory.getId());

            // Then
            assertThat(foodExpenses).hasSize(2);
            assertThat(transportExpenses).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting Tests")
    class PaginationAndSortingTests {

        @Test
        @DisplayName("Should paginate results correctly")
        void testPagination() {
            // Given - Create 15 expenses
            IntStream.range(1, 16).forEach(i -> {
                Expense expense = createExpense("Expense " + i, new BigDecimal(i * 5), 
                    LocalDate.now().minusDays(i), foodCategory);
                entityManager.persistAndFlush(expense);
            });

            // When
            Pageable firstPage = PageRequest.of(0, 5);
            Pageable secondPage = PageRequest.of(1, 5);
            
            Page<Expense> page1 = expenseRepository.findAll(firstPage);
            Page<Expense> page2 = expenseRepository.findAll(secondPage);

            // Then
            assertThat(page1.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(15);
            assertThat(page1.getTotalPages()).isEqualTo(3);
            assertThat(page1.isFirst()).isTrue();
            assertThat(page1.hasNext()).isTrue();

            assertThat(page2.getContent()).hasSize(5);
            assertThat(page2.isFirst()).isFalse();
            assertThat(page2.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should sort results by different fields")
        void testSorting() {
            // Given
            Expense expensive = createExpense("Expensive", new BigDecimal("100.00"), LocalDate.now(), foodCategory);
            Expense cheap = createExpense("Cheap", new BigDecimal("5.00"), LocalDate.now().minusDays(1), transportCategory);
            Expense moderate = createExpense("Moderate", new BigDecimal("25.00"), LocalDate.now().minusDays(2), entertainmentCategory);

            entityManager.persistAndFlush(expensive);
            entityManager.persistAndFlush(cheap);
            entityManager.persistAndFlush(moderate);

            // When - Sort by amount descending
            Sort sortByAmountDesc = Sort.by(Sort.Direction.DESC, "amount");
            List<Expense> byAmountDesc = expenseRepository.findAll(sortByAmountDesc);

            // Sort by date ascending
            Sort sortByDateAsc = Sort.by(Sort.Direction.ASC, "expenseDate");
            List<Expense> byDateAsc = expenseRepository.findAll(sortByDateAsc);

            // Then
            assertThat(byAmountDesc)
                .extracting(Expense::getDescription)
                .containsExactly("Expensive", "Moderate", "Cheap");

            assertThat(byDateAsc)
                .extracting(Expense::getDescription)
                .containsExactly("Moderate", "Cheap", "Expensive");
        }

        @Test
        @DisplayName("Should combine pagination with sorting")
        void testPaginationWithSorting() {
            // Given
            IntStream.range(1, 11).forEach(i -> {
                Expense expense = createExpense("Expense " + i, new BigDecimal(i * 10), 
                    LocalDate.now().minusDays(i), foodCategory);
                entityManager.persistAndFlush(expense);
            });

            // When
            Pageable pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "amount"));
            Page<Expense> result = expenseRepository.findAll(pageRequest);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                .extracting(Expense::getDescription)
                .containsExactly("Expense 10", "Expense 9", "Expense 8");
        }
    }

    @Nested
    @DisplayName("Database Constraints and Relationships Tests")
    class ConstraintsAndRelationshipsTests {

        @Test
        @DisplayName("Should enforce foreign key constraint")
        void testForeignKeyConstraint() {
            // When & Then - Cannot save expense with null category (validation error first)
            Expense expenseWithoutCategory = new Expense("Test", new BigDecimal("10.00"), LocalDate.now().minusDays(1), null);
            
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(expenseWithoutCategory);
            }).isInstanceOf(jakarta.validation.ConstraintViolationException.class)
              .hasMessageContaining("Category is required");
        }

        @Test
        @DisplayName("Should enforce NOT NULL constraints")
        void testNotNullConstraints() {
            // Test null description - Bean validation catches this first
            assertThatThrownBy(() -> {
                Expense expense = new Expense(null, new BigDecimal("10.00"), LocalDate.now().minusDays(1), foodCategory);
                entityManager.persistAndFlush(expense);
            }).isInstanceOf(jakarta.validation.ConstraintViolationException.class)
              .hasMessageContaining("Description is required");

            // Test null amount - Bean validation catches this first
            assertThatThrownBy(() -> {
                Expense expense = new Expense("Test", null, LocalDate.now().minusDays(1), foodCategory);
                entityManager.persistAndFlush(expense);
            }).isInstanceOf(jakarta.validation.ConstraintViolationException.class)
              .hasMessageContaining("Amount is required");

            // Test null expense date - Bean validation catches this first
            assertThatThrownBy(() -> {
                Expense expense = new Expense("Test", new BigDecimal("10.00"), null, foodCategory);
                entityManager.persistAndFlush(expense);
            }).isInstanceOf(jakarta.validation.ConstraintViolationException.class)
              .hasMessageContaining("Expense date is required");
        }

        @Test
        @DisplayName("Should maintain referential integrity on category deletion")
        void testReferentialIntegrity() {
            // Given
            Expense expense = createExpense("Test expense", new BigDecimal("10.00"), LocalDate.now().minusDays(1), foodCategory);
            entityManager.persistAndFlush(expense);

            // When & Then - Should not be able to delete category with associated expenses
            assertThatThrownBy(() -> {
                entityManager.remove(foodCategory);
                entityManager.flush();
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should handle cascade operations correctly")
        void testCascadeOperations() {
            // Given
            Category tempCategory = createAndPersistCategory("Temp", "Temporary", "#000000", "fas fa-temp");
            Expense expense = createExpense("Temp expense", new BigDecimal("10.00"), LocalDate.now(), tempCategory);
            entityManager.persistAndFlush(expense);

            Long expenseId = expense.getId();
            Long categoryId = tempCategory.getId();

            // When - Delete the expense first
            expenseRepository.deleteById(expenseId);
            entityManager.flush();

            // Then - Category should still exist
            assertThat(categoryRepository.existsById(categoryId)).isTrue();
            assertThat(expenseRepository.existsById(expenseId)).isFalse();
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle bulk insert operations efficiently")
        void testBulkInsertPerformance() {
            // Given
            int recordCount = 1000;
            StopWatch stopWatch = new StopWatch();

            // When
            stopWatch.start();
            
            List<Expense> expenses = IntStream.range(1, recordCount + 1)
                .mapToObj(i -> createExpense("Bulk Expense " + i, 
                    new BigDecimal(i % 100 + 1), 
                    LocalDate.now().minusDays(i % 365), 
                    i % 2 == 0 ? foodCategory : transportCategory))
                .toList();

            expenseRepository.saveAll(expenses);
            entityManager.flush();
            
            stopWatch.stop();

            // Then
            long executionTime = stopWatch.getTotalTimeMillis();
            assertThat(executionTime).isLessThan(5000); // Should complete within 5 seconds
            assertThat(expenseRepository.count()).isEqualTo(recordCount);
        }

        @Test
        @DisplayName("Should perform complex analytics queries efficiently")
        void testComplexQueryPerformance() {
            // Given - Create diverse test data
            IntStream.range(1, 501).forEach(i -> {
                Category category = i % 4 == 0 ? foodCategory : 
                                  i % 4 == 1 ? transportCategory :
                                  i % 4 == 2 ? entertainmentCategory : healthCategory;
                
                Expense expense = createExpense("Performance Test " + i, 
                    new BigDecimal(i % 200 + 1), 
                    LocalDate.now().minusDays(i % 30), 
                    category);
                entityManager.persist(expense);
                
                if (i % 100 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            });
            entityManager.flush();

            // When & Then - Test various queries
            StopWatch stopWatch = new StopWatch();

            stopWatch.start("getTotalExpenses");
            BigDecimal total = expenseRepository.getTotalExpenses();
            stopWatch.stop();
            assertThat(total).isGreaterThan(BigDecimal.ZERO);

            stopWatch.start("getExpensesByCategory");
            List<Object[]> breakdown = expenseRepository.getExpensesByCategory();
            stopWatch.stop();
            assertThat(breakdown).hasSize(4);

            stopWatch.start("findTopRecentExpenses");
            List<Expense> recentExpenses = expenseRepository.findTopRecentExpenses(50);
            stopWatch.stop();
            assertThat(recentExpenses).hasSize(50);

            // All queries should complete quickly
            assertThat(stopWatch.getTotalTimeMillis()).isLessThan(2000);
        }

        @ParameterizedTest
        @ValueSource(ints = {100, 500, 1000})
        @DisplayName("Should handle large dataset queries with different sizes")
        void testLargeDatasetQueries(int datasetSize) {
            // Given
            IntStream.range(1, datasetSize + 1).forEach(i -> {
                Expense expense = createExpense("Dataset Test " + i, 
                    new BigDecimal(i % 100 + 1), 
                    LocalDate.now().minusDays(i % 30), 
                    i % 2 == 0 ? foodCategory : transportCategory);
                entityManager.persist(expense);
                
                if (i % 200 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            });
            entityManager.flush();

            // When
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            
            List<Expense> allExpenses = expenseRepository.findAll();
            BigDecimal total = expenseRepository.getTotalExpenses();
            List<Object[]> categoryBreakdown = expenseRepository.getExpensesByCategory();
            
            stopWatch.stop();

            // Then
            assertThat(allExpenses).hasSize(datasetSize);
            assertThat(total).isGreaterThan(BigDecimal.ZERO);
            assertThat(categoryBreakdown).hasSizeGreaterThan(0);
            
            // Performance should scale reasonably
            long expectedMaxTime = datasetSize < 500 ? 1000 : 3000;
            assertThat(stopWatch.getTotalTimeMillis()).isLessThan(expectedMaxTime);
        }
    }

    @Nested
    @DisplayName("Transaction Isolation and Data Consistency Tests")
    class TransactionIsolationTests {

        @Test
        @DisplayName("Should maintain data consistency across operations")
        void testDataConsistency() {
            // Given - Create test data
            IntStream.range(1, 6).forEach(i -> {
                Expense expense = createExpense("Consistency Test " + i, 
                    new BigDecimal(i * 10), 
                    LocalDate.now().minusDays(i), 
                    foodCategory);
                entityManager.persistAndFlush(expense);
            });

            // When - Perform various operations
            List<Expense> expenses = expenseRepository.findAll();
            BigDecimal total = expenseRepository.getTotalExpenses();
            List<Object[]> breakdown = expenseRepository.getExpensesByCategory();
            long count = expenseRepository.count();

            // Then - Verify data consistency
            assertThat(expenses).hasSize(5);
            assertThat(count).isEqualTo(5);
            assertThat(total).isEqualByComparingTo(new BigDecimal("150.00")); // 10+20+30+40+50
            assertThat(breakdown).hasSize(1); // Only food category has expenses
            
            Object[] foodBreakdown = breakdown.get(0);
            assertThat(foodBreakdown[0]).isEqualTo("Food");
            assertThat(foodBreakdown[1]).isEqualTo(new BigDecimal("150.00"));
        }

        @Test
        @DisplayName("Should handle transaction rollback properly")
        void testTransactionRollback() {
            // Given - Initial count
            long initialCount = expenseRepository.count();
            
            // When & Then - Bean validation prevents invalid data from being persisted
            assertThatThrownBy(() -> {
                // This should fail validation immediately
                Expense invalidExpense = createExpense("Invalid", null, LocalDate.now().minusDays(1), foodCategory);
                entityManager.persistAndFlush(invalidExpense);
            }).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
            
            // Clear the entity manager after exception
            entityManager.clear();
            
            // Then - Count should be unchanged (validation prevented persistence)
            assertThat(expenseRepository.count()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should maintain referential integrity")
        void testReferentialIntegrity() {
            // Given
            Expense expense = createExpense("Referential Test", new BigDecimal("10.00"), LocalDate.now().minusDays(1), foodCategory);
            entityManager.persistAndFlush(expense);
            
            Long categoryId = foodCategory.getId();
            
            // When & Then - Cannot delete category with associated expenses
            assertThatThrownBy(() -> {
                Category categoryToDelete = entityManager.find(Category.class, categoryId);
                entityManager.remove(categoryToDelete);
                entityManager.flush();
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
            
            // Clear entity manager after exception
            entityManager.clear();
            
            // Category should still exist (deletion failed due to constraint)
            Category categoryAfterFailedDelete = entityManager.find(Category.class, categoryId);
            assertThat(categoryAfterFailedDelete).isNotNull();
            assertThat(categoryAfterFailedDelete.getName()).isEqualTo("Food");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty result sets gracefully")
        void testEmptyResultSets() {
            // When
            List<Expense> nonExistentCategory = expenseRepository.findByCategory(
                createAndPersistCategory("NonExistent", "Non existent", "#000000", "fas fa-none"));
            List<Expense> futureExpenses = expenseRepository.findByExpenseDateBetween(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
            BigDecimal emptyTotal = expenseRepository.getTotalAmountByCategory(healthCategory);
            List<Object[]> emptyBreakdown = expenseRepository.getExpensesByCategory();

            // Then
            assertThat(nonExistentCategory).isEmpty();
            assertThat(futureExpenses).isEmpty();
            assertThat(emptyTotal).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(emptyBreakdown).isEmpty();
        }

        @Test
        @DisplayName("Should handle boundary date conditions")
        void testBoundaryDateConditions() {
            // Given
            LocalDate today = LocalDate.now();
            Expense todayExpense = createExpense("Today", new BigDecimal("10.00"), today, foodCategory);
            
            entityManager.persistAndFlush(todayExpense);

            // When & Then - Same date for start and end
            List<Expense> sameDateRange = expenseRepository.findByExpenseDateBetween(today, today);
            assertThat(sameDateRange).hasSize(1);

            // Invalid range (end before start) should return empty
            List<Expense> invalidRange = expenseRepository.findByExpenseDateBetween(
                today.plusDays(1), today);
            assertThat(invalidRange).isEmpty();
        }

        @Test
        @DisplayName("Should handle large amount values")
        void testLargeAmountValues() {
            // Given - Use amounts within validation constraints
            BigDecimal largeAmount = new BigDecimal("999999.99"); // Max allowed
            BigDecimal moderateAmount = new BigDecimal("500000.00");
            
            Expense largeExpense = createExpense("Large", largeAmount, LocalDate.now().minusDays(1), foodCategory);
            Expense moderateExpense = createExpense("Moderate", moderateAmount, LocalDate.now().minusDays(2), transportCategory);

            entityManager.persistAndFlush(largeExpense);
            entityManager.persistAndFlush(moderateExpense);

            // When
            BigDecimal total = expenseRepository.getTotalExpenses();
            List<Expense> largeExpenses = expenseRepository.findByAmountGreaterThan(new BigDecimal("100000"));

            // Then
            assertThat(total).isEqualByComparingTo(new BigDecimal("1499999.99"));
            assertThat(largeExpenses).hasSize(2);
        }

        @Test
        @DisplayName("Should handle special characters in descriptions")
        void testSpecialCharactersInDescriptions() {
            // Given
            Expense specialChars = createExpense("Café & Restaurant's meal! @#$%^&*()", 
                new BigDecimal("25.50"), LocalDate.now(), foodCategory);
            Expense unicodeChars = createExpense("食事 - японская кухня - مطعم", 
                new BigDecimal("30.00"), LocalDate.now(), foodCategory);

            entityManager.persistAndFlush(specialChars);
            entityManager.persistAndFlush(unicodeChars);

            // When
            List<Expense> cafeSearch = expenseRepository.findByDescriptionContainingIgnoreCase("café");
            List<Expense> restaurantSearch = expenseRepository.findByDescriptionContainingIgnoreCase("restaurant");
            List<Expense> unicodeSearch = expenseRepository.findByDescriptionContainingIgnoreCase("食事");

            // Then
            assertThat(cafeSearch).hasSize(1);
            assertThat(restaurantSearch).hasSize(1);
            assertThat(unicodeSearch).hasSize(1);
        }

        @Test
        @DisplayName("Should handle count operations correctly")
        void testCountOperations() {
            // Given
            Expense expense1 = createExpense("Count Test 1", new BigDecimal("10.00"), LocalDate.now(), foodCategory);
            Expense expense2 = createExpense("Count Test 2", new BigDecimal("15.00"), LocalDate.now().minusDays(1), foodCategory);
            Expense expense3 = createExpense("Count Test 3", new BigDecimal("20.00"), LocalDate.now(), transportCategory);

            entityManager.persistAndFlush(expense1);
            entityManager.persistAndFlush(expense2);
            entityManager.persistAndFlush(expense3);

            // When
            long foodCount = expenseRepository.countByCategory(foodCategory);
            long transportCount = expenseRepository.countByCategory(transportCategory);
            long healthCount = expenseRepository.countByCategory(healthCategory);
            long dateRangeCount = expenseRepository.countByExpenseDateBetween(
                LocalDate.now().minusDays(1), LocalDate.now());

            // Then
            assertThat(foodCount).isEqualTo(2);
            assertThat(transportCount).isEqualTo(1);
            assertThat(healthCount).isEqualTo(0);
            assertThat(dateRangeCount).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Query Result Mapping and Data Integrity Tests")
    class ResultMappingAndIntegrityTests {

        @Test
        @DisplayName("Should map query results to correct data types")
        void testQueryResultMapping() {
            // Given
            Expense expense = createExpense("Mapping Test", new BigDecimal("123.45"), LocalDate.now(), foodCategory);
            entityManager.persistAndFlush(expense);

            // When
            List<Object[]> summary = expenseRepository.getExpenseSummaryByCategory();
            List<Object[]> breakdown = expenseRepository.getExpensesByCategory();

            // Then - Verify data types and values
            Object[] foodSummary = summary.stream()
                .filter(row -> "Food".equals(row[0]))
                .findFirst()
                .orElseThrow();
            
            assertThat(foodSummary[0]).isInstanceOf(String.class);
            assertThat(foodSummary[1]).isInstanceOf(BigDecimal.class);
            assertThat((BigDecimal) foodSummary[1]).isEqualByComparingTo(new BigDecimal("123.45"));

            Object[] foodBreakdown = breakdown.stream()
                .filter(row -> "Food".equals(row[0]))
                .findFirst()
                .orElseThrow();
            
            assertThat(foodBreakdown[0]).isInstanceOf(String.class);
            assertThat(foodBreakdown[1]).isInstanceOf(BigDecimal.class);
        }

        @Test
        @DisplayName("Should maintain data integrity across complex operations")
        void testDataIntegrityAcrossOperations() {
            // Given - Create initial data
            List<Expense> initialExpenses = List.of(
                createExpense("Integrity Test 1", new BigDecimal("10.00"), LocalDate.now(), foodCategory),
                createExpense("Integrity Test 2", new BigDecimal("20.00"), LocalDate.now(), transportCategory),
                createExpense("Integrity Test 3", new BigDecimal("30.00"), LocalDate.now(), entertainmentCategory)
            );

            initialExpenses.forEach(entityManager::persistAndFlush);

            // When - Perform various operations
            BigDecimal initialTotal = expenseRepository.getTotalExpenses();
            List<Object[]> initialBreakdown = expenseRepository.getExpensesByCategory();
            
            // Add more expenses
            Expense newExpense = createExpense("New Expense", new BigDecimal("40.00"), LocalDate.now(), foodCategory);
            entityManager.persistAndFlush(newExpense);
            
            BigDecimal updatedTotal = expenseRepository.getTotalExpenses();
            List<Object[]> updatedBreakdown = expenseRepository.getExpensesByCategory();

            // Then - Verify integrity
            assertThat(initialTotal).isEqualByComparingTo(new BigDecimal("60.00"));
            assertThat(updatedTotal).isEqualByComparingTo(new BigDecimal("100.00"));
            
            assertThat(initialBreakdown).hasSize(3);
            assertThat(updatedBreakdown).hasSize(3); // Same categories, updated amounts
            
            // Verify individual category totals
            BigDecimal foodTotal = expenseRepository.getTotalAmountByCategory(foodCategory);
            assertThat(foodTotal).isEqualByComparingTo(new BigDecimal("50.00")); // 10 + 40
        }

        @Test
        @DisplayName("Should preserve precision in decimal calculations")
        void testDecimalPrecision() {
            // Given - Use precise decimal values with 2 decimal places (as per validation)
            List<Expense> precisionExpenses = List.of(
                createExpense("Precision 1", new BigDecimal("10.55"), LocalDate.now().minusDays(1), foodCategory),
                createExpense("Precision 2", new BigDecimal("20.44"), LocalDate.now().minusDays(2), foodCategory),
                createExpense("Precision 3", new BigDecimal("30.01"), LocalDate.now().minusDays(3), foodCategory)
            );

            precisionExpenses.forEach(entityManager::persistAndFlush);

            // When
            BigDecimal total = expenseRepository.getTotalExpenses();
            BigDecimal categoryTotal = expenseRepository.getTotalAmountByCategory(foodCategory);

            // Then - Verify precision is maintained
            BigDecimal expectedTotal = new BigDecimal("61.00"); // 10.55 + 20.44 + 30.01
            assertThat(total).isEqualByComparingTo(expectedTotal);
            assertThat(categoryTotal).isEqualByComparingTo(expectedTotal);
        }
    }
}