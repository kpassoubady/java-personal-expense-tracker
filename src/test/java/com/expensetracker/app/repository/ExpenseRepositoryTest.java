package com.expensetracker.app.repository;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category foodCategory;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        // Create test data programmatically to avoid ID conflicts
        foodCategory = new Category("Food", "Food and dining", "#28a745", "fas fa-utensils");
        foodCategory = categoryRepository.save(foodCategory);
        
        testExpense = new Expense("Lunch", new BigDecimal("10.0"), LocalDate.now(), foodCategory);
        testExpense = expenseRepository.save(testExpense);
    }

    @Test
    void testFindAllByOrderByExpenseDateDesc() {
        List<Expense> expenses = expenseRepository.findAllByOrderByExpenseDateDesc();
        assertFalse(expenses.isEmpty());
        assertEquals("Lunch", expenses.get(0).getDescription());
        assertEquals(0, new BigDecimal("10.0").compareTo(expenses.get(0).getAmount()));
    }

    @Test
    void testCustomQuery_TotalExpenses() {
        java.math.BigDecimal total = expenseRepository.getTotalExpenses();
        assertEquals(0, new java.math.BigDecimal("10.0").compareTo(total));
    }

    @Test
    void testAnalyticsQuery_ExpensesByCategory() {
        java.util.List<Object[]> breakdown = expenseRepository.getExpensesByCategory();
        assertFalse(breakdown.isEmpty());
        assertEquals("Food", breakdown.get(0)[0]);
        assertEquals(0, new java.math.BigDecimal("10.0").compareTo((BigDecimal) breakdown.get(0)[1]));
    }

    @Test
    void testPerformance_BulkInsert() {
        // First get the count of existing expenses
        long existingCount = expenseRepository.count();
        
        // Use smaller batch size and existing category
        List<Expense> bulkExpenses = java.util.stream.IntStream.range(0, 100)
            .mapToObj(i -> {
                Expense expense = new Expense("BulkExpense" + i, new BigDecimal("5.00"), LocalDate.now(), foodCategory);
                // Don't set ID, let it be auto-generated
                return expense;
            })
            .toList();
        long start = System.currentTimeMillis();
        List<Expense> saved = expenseRepository.saveAll(bulkExpenses);
        long duration = System.currentTimeMillis() - start;
        
        // Verify the operation was performed
        assertTrue(duration < 2000, "Bulk insert should be performant");
        assertEquals(100, saved.size());
        assertEquals(existingCount + 100, expenseRepository.count());
    }

    @Test
    void testEdgeCases_NullHandling() {
        Expense nullExpense = new Expense(null, null, null, null);
        assertThrows(Exception.class, () -> expenseRepository.save(nullExpense));
    }
}
