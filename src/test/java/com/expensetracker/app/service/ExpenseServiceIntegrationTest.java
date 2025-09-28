package com.expensetracker.app.service;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@Rollback
@DisplayName("ExpenseService Integration Tests")
class ExpenseServiceIntegrationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;

    private Expense testExpense;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clear existing data for clean test state
        expenseRepository.deleteAll();
        
        // Create and save a test category
        testCategory = new Category("Food", "Food and dining", "#28a745", "fas fa-utensils");
        testCategory = categoryService.saveCategory(testCategory);

        // Create test expense
        testExpense = new Expense("Lunch at restaurant", new BigDecimal("25.50"), LocalDate.now().minusDays(1), testCategory);
    }

    @Test
    @DisplayName("Should return all expenses in integration context")
    void getAllExpenses_ShouldReturnAllExpenses() {
        // Given
        expenseService.saveExpense(testExpense);
        Expense anotherExpense = new Expense("Gas station", new BigDecimal("45.00"), LocalDate.now().minusDays(2), testCategory);
        expenseService.saveExpense(anotherExpense);

        // When
        List<Expense> result = expenseService.getAllExpenses();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(exp -> "Lunch at restaurant".equals(exp.getDescription())));
        assertTrue(result.stream().anyMatch(exp -> "Gas station".equals(exp.getDescription())));
    }

    @Test
    @DisplayName("Should find expense by ID in integration context")
    void findById_ShouldReturnExpense_WhenExists() {
        // Given
        Expense savedExpense = expenseService.saveExpense(testExpense);

        // When
        Optional<Expense> result = expenseService.findById(savedExpense.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Lunch at restaurant", result.get().getDescription());
        assertEquals(new BigDecimal("25.50"), result.get().getAmount());
    }

    @Test
    @DisplayName("Should save new expense in integration context")
    void saveExpense_ShouldReturnSavedExpense() {
        // When
        Expense result = expenseService.saveExpense(testExpense);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Lunch at restaurant", result.getDescription());
        assertEquals(new BigDecimal("25.50"), result.getAmount());
        assertEquals(testCategory.getId(), result.getCategory().getId());
        
        // Verify it was actually saved
        Optional<Expense> found = expenseRepository.findById(result.getId());
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Should update existing expense in integration context")
    void updateExpense_ShouldUpdateExpense() {
        // Given
        Expense savedExpense = expenseService.saveExpense(testExpense);
        savedExpense.setDescription("Updated lunch expense");
        savedExpense.setAmount(new BigDecimal("30.00"));

        // When
        Expense updated = expenseService.saveExpense(savedExpense);

        // Then
        assertEquals("Updated lunch expense", updated.getDescription());
        assertEquals(new BigDecimal("30.00"), updated.getAmount());
        
        // Verify the update was persisted
        Optional<Expense> found = expenseRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated lunch expense", found.get().getDescription());
        assertEquals(new BigDecimal("30.00"), found.get().getAmount());
    }

    @Test
    @DisplayName("Should delete expense in integration context")
    void deleteExpense_ShouldRemoveExpense() {
        // Given
        Expense savedExpense = expenseService.saveExpense(testExpense);
        Long expenseId = savedExpense.getId();

        // When
        expenseService.deleteExpense(expenseId);

        // Then
        Optional<Expense> found = expenseRepository.findById(expenseId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should handle validation failures in integration context")
    void saveExpense_ShouldThrowException_WhenValidationFails() {
        // Given - invalid expense with null description
        Expense invalidExpense = new Expense(null, new BigDecimal("10.00"), LocalDate.now(), testCategory);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            expenseService.saveExpense(invalidExpense);
        });

        assertTrue(exception.getMessage().contains("Description is required"));
    }

    @Test
    @DisplayName("Should calculate total expenses correctly in integration context")
    void getTotalExpenses_ShouldReturnCorrectTotal() {
        // Given
        expenseService.saveExpense(testExpense);
        Expense expense2 = new Expense("Dinner", new BigDecimal("40.00"), LocalDate.now(), testCategory);
        expenseService.saveExpense(expense2);

        // When
        BigDecimal total = expenseService.getTotalExpenses();

        // Then
        assertEquals(new BigDecimal("65.50"), total);
    }

    @Test
    @DisplayName("Should find expenses by category in integration context")
    void getExpensesByCategory_ShouldReturnCategoryExpenses() {
        // Given
        expenseService.saveExpense(testExpense);
        
        // Create another category and expense
        Category transportCategory = new Category("Transport", "Transportation expenses", "#007bff", "fas fa-car");
        transportCategory = categoryService.saveCategory(transportCategory);
        Expense transportExpense = new Expense("Gas", new BigDecimal("50.00"), LocalDate.now(), transportCategory);
        expenseService.saveExpense(transportExpense);

        // When
        List<Expense> foodExpenses = expenseService.getExpensesByCategory(testCategory.getId());
        List<Expense> transportExpenses = expenseService.getExpensesByCategory(transportCategory.getId());

        // Then
        assertEquals(1, foodExpenses.size());
        assertEquals("Lunch at restaurant", foodExpenses.get(0).getDescription());
        
        assertEquals(1, transportExpenses.size());
        assertEquals("Gas", transportExpenses.get(0).getDescription());
    }

    @Test
    @DisplayName("Should search expenses by description in integration context")
    void searchExpenses_ShouldReturnMatchingExpenses() {
        // Given
        expenseService.saveExpense(testExpense);
        Expense restaurantExpense = new Expense("Restaurant dinner", new BigDecimal("60.00"), LocalDate.now(), testCategory);
        expenseService.saveExpense(restaurantExpense);
        Expense gasExpense = new Expense("Gas station", new BigDecimal("40.00"), LocalDate.now(), testCategory);
        expenseService.saveExpense(gasExpense);

        // When - search for "restaurant"
        List<Expense> restaurantResults = expenseService.searchExpenses("restaurant");
        
        // Then
        assertEquals(2, restaurantResults.size());
        assertTrue(restaurantResults.stream().allMatch(exp -> 
            exp.getDescription().toLowerCase().contains("restaurant")));

        // When - search for "gas"
        List<Expense> gasResults = expenseService.searchExpenses("gas");
        
        // Then
        assertEquals(1, gasResults.size());
        assertEquals("Gas station", gasResults.get(0).getDescription());
    }

    @Test
    @DisplayName("Should get expense summary by category in integration context")
    void getExpenseSummaryByCategory_ShouldReturnCorrectSummary() {
        // Given
        expenseService.saveExpense(testExpense);
        
        // Create transport category and expense
        Category transportCategory = new Category("Transport", "Transportation", "#007bff", "fas fa-car");
        transportCategory = categoryService.saveCategory(transportCategory);
        Expense transportExpense = new Expense("Gas", new BigDecimal("50.00"), LocalDate.now(), transportCategory);
        expenseService.saveExpense(transportExpense);

        // When
        Map<String, BigDecimal> summary = expenseService.getExpenseSummaryByCategory();

        // Then
        assertNotNull(summary);
        assertTrue(summary.containsKey("Food"));
        assertTrue(summary.containsKey("Transport"));
        assertEquals(new BigDecimal("25.50"), summary.get("Food"));
        assertEquals(new BigDecimal("50.00"), summary.get("Transport"));
    }

    @Test
    @DisplayName("Should get recent expenses in integration context")
    void getRecentExpenses_ShouldReturnMostRecentExpenses() {
        // Given - create expenses with different dates
        Expense expense1 = new Expense("Oldest", new BigDecimal("10.00"), LocalDate.now().minusDays(3), testCategory);
        Expense expense2 = new Expense("Middle", new BigDecimal("20.00"), LocalDate.now().minusDays(2), testCategory);
        Expense expense3 = new Expense("Newest", new BigDecimal("30.00"), LocalDate.now().minusDays(1), testCategory);
        
        expenseService.saveExpense(expense1);
        expenseService.saveExpense(expense2);
        expenseService.saveExpense(expense3);

        // When
        List<Expense> recentExpenses = expenseService.getRecentExpenses(2);

        // Then
        assertEquals(2, recentExpenses.size());
        // Should be ordered by date descending (newest first)
        assertEquals("Newest", recentExpenses.get(0).getDescription());
        assertEquals("Middle", recentExpenses.get(1).getDescription());
    }

    @Test
    @DisplayName("Should get expense analytics in integration context")
    void getExpenseAnalytics_ShouldReturnCorrectAnalytics() {
        // Given
        expenseService.saveExpense(testExpense);
        Expense expense2 = new Expense("Dinner", new BigDecimal("40.00"), LocalDate.now(), testCategory);
        expenseService.saveExpense(expense2);

        // When
        Map<String, Object> analytics = expenseService.getExpenseAnalytics(null, null);

        // Then
        assertNotNull(analytics);
        assertEquals(new BigDecimal("65.50"), analytics.get("totalAmount"));
        assertEquals(2L, analytics.get("totalCount"));
        assertEquals(new BigDecimal("32.75"), analytics.get("averageExpense"));
        assertNotNull(analytics.get("categorySummary"));
        assertNotNull(analytics.get("monthlySummary"));
    }
}