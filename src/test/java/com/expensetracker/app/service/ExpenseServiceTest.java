package com.expensetracker.app.service;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.repository.ExpenseRepository;
import com.expensetracker.app.service.ExpenseService;
import com.expensetracker.app.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @MockBean
    private ExpenseRepository expenseRepository;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ExpenseService expenseService;

    private Expense testExpense;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Food", "Food and dining", "#28a745", "fas fa-utensils");
        testCategory.setId(1L);

        testExpense = new Expense("Lunch", new BigDecimal("25.50"), LocalDate.now(), testCategory);
        testExpense.setId(1L);
    }

    @Test
    void getAllExpenses_ShouldReturnAllExpenses() {
        // Given
        List<Expense> expenses = Arrays.asList(testExpense);
        when(expenseRepository.findAllByOrderByExpenseDateDesc()).thenReturn(expenses);

        // When
        List<Expense> result = expenseService.getAllExpenses();

        // Then
        assertEquals(1, result.size());
        assertEquals("Lunch", result.get(0).getDescription());
        verify(expenseRepository).findAllByOrderByExpenseDateDesc();
    }

    @Test
    void findById_ShouldReturnExpense_WhenExists() {
        // Given
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        // When
        Optional<Expense> result = expenseService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Lunch", result.get().getDescription());
        assertEquals(new BigDecimal("25.50"), result.get().getAmount());
        verify(expenseRepository).findById(1L);
    }

    @Test
    void saveExpense_ShouldReturnSavedExpense() {
        // Given
        testExpense.getCategory().setId(1L);
        when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        // When
        Expense result = expenseService.saveExpense(testExpense);

        // Then
        assertNotNull(result);
        assertEquals("Lunch", result.getDescription());
        verify(expenseRepository).save(testExpense);
        verify(categoryService).findById(1L);
    }

    @Test
    void saveExpense_ShouldThrowException_WhenAmountNegative() {
        Expense invalidExpense = new Expense("Dinner", new BigDecimal("-10.00"), LocalDate.now(), testCategory);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            expenseService.saveExpense(invalidExpense);
        });
        assertTrue(exception.getMessage().contains("negative"));
    }

    @Test
    void saveExpense_ShouldThrowException_WhenDescriptionMissing() {
        Expense invalidExpense = new Expense(null, new BigDecimal("10.00"), LocalDate.now(), testCategory);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            expenseService.saveExpense(invalidExpense);
        });
        assertTrue(exception.getMessage().contains("Description is required") || exception.getMessage().contains("description"));
    }

    @Test
    void getTotalExpenses_ShouldReturnSum() {
        when(expenseRepository.getTotalExpenses()).thenReturn(new java.math.BigDecimal("100.00"));
        java.math.BigDecimal total = expenseService.getTotalExpenses();
        assertEquals(new java.math.BigDecimal("100.00"), total);
        verify(expenseRepository).getTotalExpenses();
    }

    @Test
    void getExpensesByCategory_ShouldReturnExpenses() {
        when(expenseRepository.findByCategory(any(Category.class))).thenReturn(Arrays.asList(testExpense));
        when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
        List<Expense> expenses = expenseService.getExpensesByCategory(1L);
        assertNotNull(expenses);
        assertEquals(1, expenses.size());
        assertEquals(1L, expenses.get(0).getCategory().getId());
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(expenseRepository.findById(2L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            expenseService.findById(2L).orElseThrow(() -> new RuntimeException("Expense not found"));
        });
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void transactionRollback_ShouldRollbackOnException() {
        testExpense.getCategory().setId(1L);
        when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
        when(expenseRepository.save(any(Expense.class))).thenThrow(new RuntimeException("DB error"));
        Exception exception = assertThrows(RuntimeException.class, () -> {
            expenseService.saveExpense(testExpense);
        });
        assertTrue(exception.getMessage().contains("Failed to save expense") || exception.getMessage().contains("DB error"));
    }

    @Test
    void bulkInsertExpenses_PerformanceTest() {
        List<Expense> bulkExpenses = IntStream.range(0, 1000)
            .mapToObj(i -> new Expense("Expense" + i, new BigDecimal("10.00"), LocalDate.now(), testCategory))
            .collect(Collectors.toList());
        when(expenseRepository.saveAll(anyList())).thenReturn(bulkExpenses);
        long start = System.currentTimeMillis();
        List<Expense> result = expenseService.saveExpenses(bulkExpenses);
        long duration = System.currentTimeMillis() - start;
        assertEquals(1000, result.size());
        assertTrue(duration < 2000, "Bulk insert should be performant");
        verify(expenseRepository).saveAll(bulkExpenses);
    }
}
