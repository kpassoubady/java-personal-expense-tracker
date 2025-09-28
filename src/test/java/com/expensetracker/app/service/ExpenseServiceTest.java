package com.expensetracker.app.service;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.exception.EntityNotFoundException;
import com.expensetracker.app.exception.ValidationException;
import com.expensetracker.app.repository.ExpenseRepository;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService Advanced Tests")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense testExpense;
    private Category testCategory;
    private List<Expense> testExpenses;

    @BeforeEach
    void setUp() {
        testCategory = createTestCategory(1L, "Food", "Food and dining", "#28a745", "fas fa-utensils");
        testExpense = createTestExpense(1L, "Lunch at restaurant", new BigDecimal("25.50"), 
                                      LocalDate.now().minusDays(1), testCategory);
        testExpenses = createTestExpenseList();
    }

    // Custom Matchers for Domain Objects
    public static Matcher<Expense> hasValidExpenseProperties() {
        return new TypeSafeMatcher<Expense>() {
            @Override
            protected boolean matchesSafely(Expense expense) {
                return expense.getDescription() != null && !expense.getDescription().trim().isEmpty() &&
                       expense.getAmount() != null && expense.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
                       expense.getExpenseDate() != null &&
                       expense.getCategory() != null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expense with valid properties");
            }
        };
    }

    public static Matcher<Expense> hasExpenseAmount(BigDecimal expectedAmount) {
        return new TypeSafeMatcher<Expense>() {
            @Override
            protected boolean matchesSafely(Expense expense) {
                return expense.getAmount() != null && 
                       expense.getAmount().compareTo(expectedAmount) == 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expense with amount ").appendValue(expectedAmount);
            }
        };
    }

    public static Matcher<Expense> belongsToCategory(Category expectedCategory) {
        return new TypeSafeMatcher<Expense>() {
            @Override
            protected boolean matchesSafely(Expense expense) {
                return expense.getCategory() != null && 
                       Objects.equals(expense.getCategory().getId(), expectedCategory.getId());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expense belonging to category ").appendValue(expectedCategory.getName());
            }
        };
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should return all expenses ordered by date descending")
        void getAllExpenses_ShouldReturnAllExpensesOrderedByDate() {
            // Given
            when(expenseRepository.findAllByOrderByExpenseDateDesc()).thenReturn(testExpenses);

            // When
            List<Expense> result = expenseService.getAllExpenses();

            // Then
            assertThat(result, hasSize(3));
            assertThat(result, everyItem(hasValidExpenseProperties()));
            verify(expenseRepository).findAllByOrderByExpenseDateDesc();
        }

        @Test
        @DisplayName("Should find expense by ID when exists")
        void findById_ShouldReturnExpense_WhenExists() {
            // Given
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

            // When
            Optional<Expense> result = expenseService.findById(1L);

            // Then
            assertTrue(result.isPresent());
            assertThat(result.get(), hasExpenseAmount(new BigDecimal("25.50")));
            assertThat(result.get(), belongsToCategory(testCategory));
            verify(expenseRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty optional when expense not found")
        void findById_ShouldReturnEmpty_WhenNotExists() {
            // Given
            when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Expense> result = expenseService.findById(999L);

            // Then
            assertFalse(result.isPresent());
            verify(expenseRepository).findById(999L);
        }

        @Test
        @DisplayName("Should throw ValidationException when ID is null")
        void findById_ShouldThrowException_WhenIdIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> expenseService.findById(null));
            assertThat(exception.getMessage(), containsString("Expense ID cannot be null"));
            verifyNoInteractions(expenseRepository);
        }

        @Test
        @DisplayName("Should get expense by ID or throw EntityNotFoundException")
        void getExpenseById_ShouldReturnExpense_WhenExists() {
            // Given
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

            // When
            Expense result = expenseService.getExpenseById(1L);

            // Then
            assertThat(result, is(testExpense));
            verify(expenseRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when expense not found")
        void getExpenseById_ShouldThrowException_WhenNotFound() {
            // Given
            when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> expenseService.getExpenseById(999L));
            assertThat(exception.getMessage(), containsString("Expense"));
            assertThat(exception.getMessage(), containsString("999"));
        }

        @Test
        @DisplayName("Should save new expense successfully")
        void saveExpense_ShouldReturnSavedExpense() {
            // Given
            when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
            when(expenseRepository.save(testExpense)).thenReturn(testExpense);

            // When
            Expense result = expenseService.saveExpense(testExpense);

            // Then
            assertThat(result, is(testExpense));
            assertThat(result, hasValidExpenseProperties());
            verify(categoryService).findById(1L);
            verify(expenseRepository).save(testExpense);
        }

        @Test
        @DisplayName("Should delete expense successfully when exists")
        void deleteExpense_ShouldRemoveExpense_WhenExists() {
            // Given
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
            doNothing().when(expenseRepository).deleteById(1L);

            // When
            expenseService.deleteExpense(1L);

            // Then
            verify(expenseRepository).findById(1L);
            verify(expenseRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when deleting non-existent expense")
        void deleteExpense_ShouldThrowException_WhenNotExists() {
            // Given
            when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> expenseService.deleteExpense(999L));
            assertThat(exception.getMessage(), containsString("Expense"));
            verify(expenseRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should delete multiple expenses and return count")
        void deleteExpenses_ShouldReturnDeletedCount() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L, 3L);
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpenses.get(0)));
            when(expenseRepository.findById(2L)).thenReturn(Optional.of(testExpenses.get(1)));
            when(expenseRepository.findById(3L)).thenReturn(Optional.of(testExpenses.get(2)));
            doNothing().when(expenseRepository).deleteById(any());

            // When
            int deletedCount = expenseService.deleteExpenses(ids);

            // Then
            assertThat(deletedCount, is(3));
            verify(expenseRepository, times(3)).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw ValidationException for null expense")
        void saveExpense_ShouldThrowException_WhenExpenseIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> expenseService.saveExpense(null));
            assertThat(exception.getMessage(), containsString("Expense cannot be null"));
            verifyNoInteractions(expenseRepository);
        }

        @ParameterizedTest
        @DisplayName("Should throw ValidationException for invalid descriptions")
        @CsvSource({
            ", Description is required",
            "'', Description is required", 
            "'   ', Description is required"
        })
        void saveExpense_ShouldThrowException_WhenDescriptionInvalid(String description, String expectedMessage) {
            // Given
            testExpense.setDescription(description);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> expenseService.saveExpense(testExpense));
            assertThat(exception.getMessage(), containsString("Description is required"));
            verifyNoInteractions(expenseRepository);
        }

        @Test
        @DisplayName("Should throw ValidationException for description exceeding 255 characters")
        void saveExpense_ShouldThrowException_WhenDescriptionTooLong() {
            // Given
            testExpense.setDescription("A".repeat(256));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> expenseService.saveExpense(testExpense));
            assertThat(exception.getMessage(), containsString("must not exceed 255 characters"));
            verifyNoInteractions(expenseRepository);
        }

        @ParameterizedTest
        @DisplayName("Should throw ValidationException for invalid amounts")
        @MethodSource("provideInvalidAmounts")
        void saveExpense_ShouldThrowException_WhenAmountInvalid(BigDecimal amount, String expectedMessagePart) {
            // Given
            testExpense.setAmount(amount);

            // When & Then
            Exception exception = assertThrows(Exception.class, 
                () -> expenseService.saveExpense(testExpense));
            assertThat(exception.getMessage(), containsString(expectedMessagePart));
        }

        static Stream<Arguments> provideInvalidAmounts() {
            return Stream.of(
                Arguments.of(null, "Amount is required"),
                Arguments.of(BigDecimal.ZERO, "greater than zero"),
                Arguments.of(new BigDecimal("-10.00"), "negative"),
                Arguments.of(new BigDecimal("10.123"), "more than 2 decimal places")
            );
        }

        @Test
        @DisplayName("Should throw ValidationException for null expense date")
        void saveExpense_ShouldThrowException_WhenExpenseDateIsNull() {
            // Given
            testExpense.setExpenseDate(null);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> expenseService.saveExpense(testExpense));
            assertThat(exception.getMessage(), containsString("Expense date is required"));
        }

        @Test
        @DisplayName("Should throw ValidationException for future expense date")
        void saveExpense_ShouldThrowException_WhenExpenseDateInFuture() {
            // Given
            testExpense.setExpenseDate(LocalDate.now().plusDays(1));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> expenseService.saveExpense(testExpense));
            assertThat(exception.getMessage(), containsString("cannot be in the future"));
        }

        @Test
        @DisplayName("Should throw ValidationException for null category")
        void saveExpense_ShouldThrowException_WhenCategoryIsNull() {
            // Given
            testExpense.setCategory(null);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> expenseService.saveExpense(testExpense));
            assertThat(exception.getMessage(), containsString("Category is required"));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException for non-existent category")
        void saveExpense_ShouldThrowException_WhenCategoryNotExists() {
            // Given
            when(categoryService.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
                () -> expenseService.saveExpense(testExpense));
            assertThat(exception.getMessage(), containsString("Category"));
        }
    }

    @Nested
    @DisplayName("Business Rule Violations")
    class BusinessRuleViolations {

        @Test
        @DisplayName("Should handle database constraint violations gracefully")
        void saveExpense_ShouldThrowValidationException_WhenDatabaseConstraintViolated() {
            // Given
            when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
            when(expenseRepository.save(testExpense))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> expenseService.saveExpense(testExpense));
            assertThat(exception.getMessage(), containsString("Failed to save expense"));
            assertThat(exception.getCause(), instanceOf(DataIntegrityViolationException.class));
        }

        @Test
        @DisplayName("Should handle deletion failures gracefully")
        void deleteExpense_ShouldThrowValidationException_WhenDeletionFails() {
            // Given
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
            doThrow(new DataIntegrityViolationException("Foreign key constraint"))
                .when(expenseRepository).deleteById(1L);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> expenseService.deleteExpense(1L));
            assertThat(exception.getMessage(), containsString("Failed to delete expense"));
        }

        @Test
        @DisplayName("Should validate threshold for high value expenses")
        void getHighValueExpenses_ShouldThrowException_WhenThresholdInvalid() {
            // When & Then - null threshold
            ValidationException exception1 = assertThrows(ValidationException.class,
                () -> expenseService.getHighValueExpenses(null));
            assertThat(exception1.getMessage(), containsString("greater than zero"));

            // When & Then - negative threshold
            ValidationException exception2 = assertThrows(ValidationException.class,
                () -> expenseService.getHighValueExpenses(new BigDecimal("-10")));
            assertThat(exception2.getMessage(), containsString("greater than zero"));

            // When & Then - zero threshold
            ValidationException exception3 = assertThrows(ValidationException.class,
                () -> expenseService.getHighValueExpenses(BigDecimal.ZERO));
            assertThat(exception3.getMessage(), containsString("greater than zero"));
        }

        @Test
        @DisplayName("Should validate limit for top spending categories")
        void getTopSpendingCategories_ShouldThrowException_WhenLimitInvalid() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> expenseService.getTopSpendingCategories(0));
            assertThat(exception.getMessage(), containsString("greater than zero"));
        }
    }

    @Nested
    @DisplayName("Concurrent Access Scenarios")
    class ConcurrentAccessScenarios {

        @Test
        @DisplayName("Should handle concurrent expense creation")
        void concurrentExpenseCreation_ShouldHandleMultipleThreads() throws InterruptedException {
            // Given
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            
            when(categoryService.findById(anyLong())).thenReturn(Optional.of(testCategory));
            when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
                Expense expense = invocation.getArgument(0);
                expense.setId(System.nanoTime()); // Simulate unique ID generation
                return expense;
            });

            // When
            List<CompletableFuture<Expense>> futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    Expense expense = createTestExpense(null, "Concurrent expense " + i, 
                        new BigDecimal("10.00"), LocalDate.now(), testCategory);
                    return expenseService.saveExpense(expense);
                }, executor))
                .collect(Collectors.toList());

            // Then
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            List<Expense> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            assertThat(results, hasSize(threadCount));
            assertThat(results, everyItem(hasValidExpenseProperties()));
            verify(expenseRepository, times(threadCount)).save(any(Expense.class));
        }

        @Test
        @DisplayName("Should handle concurrent deletion attempts")
        void concurrentDeletion_ShouldHandleRaceConditions() {
            // Given
            when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(testExpense))
                .thenReturn(Optional.empty()); // Second call returns empty (already deleted)
            doNothing().when(expenseRepository).deleteById(1L);

            // When & Then - First deletion should succeed
            assertDoesNotThrow(() -> expenseService.deleteExpense(1L));

            // When & Then - Second deletion should throw exception
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> expenseService.deleteExpense(1L));
            assertThat(exception.getMessage(), containsString("Expense"));
        }

        @Test
        @DisplayName("Should handle concurrent read operations efficiently")
        void concurrentReads_ShouldNotInterfere() throws InterruptedException {
            // Given
            when(expenseRepository.findAllByOrderByExpenseDateDesc()).thenReturn(testExpenses);
            
            int threadCount = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // When
            List<CompletableFuture<List<Expense>>> futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> expenseService.getAllExpenses(), executor))
                .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // Then
            List<List<Expense>> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            assertThat(results, hasSize(threadCount));
            results.forEach(expenses -> assertThat(expenses, hasSize(3)));
            verify(expenseRepository, times(threadCount)).findAllByOrderByExpenseDateDesc();
        }
    }

    @Nested
    @DisplayName("Large Dataset Operations and Performance")
    class LargeDatasetOperations {

        @Test
        @DisplayName("Should handle bulk expense creation efficiently")
        void saveExpenses_ShouldHandleLargeBatchEfficiently() {
            // Given
            int batchSize = 5000;
            List<Expense> largeExpenseList = createLargeExpenseList(batchSize);
            when(expenseRepository.saveAll(anyList())).thenReturn(largeExpenseList);

            // When
            long startTime = System.currentTimeMillis();
            List<Expense> result = expenseService.saveExpenses(largeExpenseList);
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertThat(result, hasSize(batchSize));
            assertThat("Bulk insert should complete within reasonable time", 
                duration, lessThan(3000L)); // Less than 3 seconds
            verify(expenseRepository).saveAll(largeExpenseList);
        }

        @Test
        @DisplayName("Should efficiently search through large expense datasets")
        void searchExpenses_ShouldHandleLargeDatasetPerformantly() {
            // Given
            String searchText = "restaurant";
            List<Expense> largeResultSet = createLargeExpenseList(2000);
            when(expenseRepository.findByDescriptionContainingIgnoreCase(searchText))
                .thenReturn(largeResultSet);

            // When
            long startTime = System.currentTimeMillis();
            List<Expense> result = expenseService.searchExpenses(searchText);
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertThat(result, hasSize(2000));
            assertThat("Search should complete within reasonable time", 
                duration, lessThan(1000L)); // Less than 1 second
            verify(expenseRepository).findByDescriptionContainingIgnoreCase(searchText);
        }

        @Test
        @DisplayName("Should handle large expense analytics calculations")
        void getExpenseAnalytics_ShouldHandleLargeDataset() {
            // Given
            BigDecimal largeTotal = new BigDecimal("1000000.00");
            long largeCount = 50000L;
            
            when(expenseRepository.getTotalExpenses()).thenReturn(largeTotal);
            when(expenseRepository.count()).thenReturn(largeCount);
            when(expenseRepository.getExpenseSummaryByCategory()).thenReturn(Collections.emptyList());

            // When
            long startTime = System.currentTimeMillis();
            Map<String, Object> analytics = expenseService.getExpenseAnalytics(null, null);
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertThat(analytics.get("totalAmount"), is(largeTotal));
            assertThat(analytics.get("totalCount"), is(largeCount));
            assertThat(analytics.get("averageExpense"), is(new BigDecimal("20.00")));
            assertThat("Analytics should complete within reasonable time", 
                duration, lessThan(500L)); // Less than 500ms
        }

        @ParameterizedTest
        @DisplayName("Should handle various batch sizes efficiently")
        @ValueSource(ints = {100, 500, 1000, 2000})
        void batchOperations_ShouldScaleWithDataSize(int batchSize) {
            // Given
            List<Long> ids = IntStream.range(1, batchSize + 1)
                .mapToObj(Long::valueOf)
                .collect(Collectors.toList());
            
            // Mock repository calls for each ID
            ids.forEach(id -> when(expenseRepository.findById(id))
                .thenReturn(Optional.of(createTestExpense(id, "Expense " + id, 
                    new BigDecimal("10.00"), LocalDate.now(), testCategory))));

            // When
            long startTime = System.currentTimeMillis();
            int deletedCount = expenseService.deleteExpenses(ids);
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertThat(deletedCount, is(batchSize));
            assertThat("Batch operation should scale reasonably", 
                duration, lessThan(batchSize * 2L)); // Linear scaling assumption
        }
    }

    @Nested
    @DisplayName("Exception Propagation and Error Messages")
    class ExceptionPropagationAndErrorMessages {

        @Test
        @DisplayName("Should propagate repository exceptions with meaningful messages")
        void saveExpense_ShouldPropagateRepositoryExceptions() {
            // Given
            RuntimeException repositoryException = new RuntimeException("Database connection timeout");
            when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
            when(expenseRepository.save(testExpense)).thenThrow(repositoryException);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> expenseService.saveExpense(testExpense));
            
            assertThat(exception.getMessage(), containsString("Failed to save expense"));
            assertThat(exception.getMessage(), containsString("Database connection timeout"));
            assertThat(exception.getCause(), is(repositoryException));
        }

        @Test
        @DisplayName("Should provide detailed validation error messages")
        void validateExpense_ShouldProvideDetailedErrorMessages() {
            // Given - Invalid amount with specific scale
            testExpense.setAmount(new BigDecimal("10.999"));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class,
                () -> expenseService.saveExpense(testExpense));
            
            assertThat(exception.getMessage(), containsString("more than 2 decimal places"));
            assertThat(exception.getField(), is("amount"));
            assertThat(exception.getValue(), is("10.999"));
        }

        @Test
        @DisplayName("Should handle cascading exception scenarios")
        void complexOperation_ShouldHandleCascadingExceptions() {
            // Given - Multiple potential failure points
            when(categoryService.findById(1L))
                .thenThrow(new RuntimeException("Category service unavailable"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> expenseService.saveExpense(testExpense));
            
            assertThat(exception.getMessage(), containsString("Category service unavailable"));
            verify(expenseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should maintain exception context through method calls")
        void getExpenseById_ShouldMaintainExceptionContext() {
            // Given
            Long nonExistentId = 12345L;
            when(expenseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> expenseService.getExpenseById(nonExistentId));
            
            assertThat(exception.getMessage(), containsString("Expense"));
            assertThat(exception.getMessage(), containsString(nonExistentId.toString()));
            assertThat(exception.getEntityType(), is("Expense"));
            assertThat(exception.getIdentifier(), is(nonExistentId.toString()));
        }
    }

    @Nested
    @DisplayName("Search and Filter Operations")
    class SearchAndFilterOperations {

        @Test
        @DisplayName("Should search expenses by description")
        void searchExpenses_ShouldReturnMatchingExpenses() {
            // Given
            String searchText = "restaurant";
            List<Expense> matchingExpenses = Arrays.asList(testExpense);
            when(expenseRepository.findByDescriptionContainingIgnoreCase("restaurant"))
                .thenReturn(matchingExpenses);

            // When
            List<Expense> result = expenseService.searchExpenses(searchText);

            // Then
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(testExpense));
            verify(expenseRepository).findByDescriptionContainingIgnoreCase("restaurant");
        }

        @Test
        @DisplayName("Should return all expenses when search text is null or empty")
        void searchExpenses_ShouldReturnAllExpenses_WhenSearchTextEmpty() {
            // Given
            when(expenseRepository.findAllByOrderByExpenseDateDesc()).thenReturn(testExpenses);

            // When & Then - null search text
            List<Expense> result1 = expenseService.searchExpenses(null);
            assertThat(result1, hasSize(3));

            // When & Then - empty search text
            List<Expense> result2 = expenseService.searchExpenses("");
            assertThat(result2, hasSize(3));

            // When & Then - whitespace search text
            List<Expense> result3 = expenseService.searchExpenses("   ");
            assertThat(result3, hasSize(3));

            verify(expenseRepository, times(3)).findAllByOrderByExpenseDateDesc();
        }

        @Test
        @DisplayName("Should filter expenses by category")
        void getExpensesByCategory_ShouldReturnCategoryExpenses() {
            // Given
            when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
            when(expenseRepository.findByCategory(testCategory)).thenReturn(Arrays.asList(testExpense));

            // When
            List<Expense> result = expenseService.getExpensesByCategory(1L);

            // Then
            assertThat(result, hasSize(1));
            assertThat(result.get(0), belongsToCategory(testCategory));
            verify(categoryService).findById(1L);
            verify(expenseRepository).findByCategory(testCategory);
        }

        @Test
        @DisplayName("Should filter expenses by date range")
        void getExpensesByDateRange_ShouldReturnDateRangeExpenses() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            when(expenseRepository.findByExpenseDateBetween(startDate, endDate))
                .thenReturn(testExpenses);

            // When
            List<Expense> result = expenseService.getExpensesByDateRange(startDate, endDate);

            // Then
            assertThat(result, hasSize(3));
            verify(expenseRepository).findByExpenseDateBetween(startDate, endDate);
        }

        @Test
        @DisplayName("Should get high value expenses above threshold")
        void getHighValueExpenses_ShouldReturnExpensesAboveThreshold() {
            // Given
            BigDecimal threshold = new BigDecimal("100.00");
            List<Expense> highValueExpenses = Arrays.asList(
                createTestExpense(2L, "Expensive dinner", new BigDecimal("150.00"), 
                    LocalDate.now(), testCategory)
            );
            when(expenseRepository.findByAmountGreaterThan(threshold))
                .thenReturn(highValueExpenses);

            // When
            List<Expense> result = expenseService.getHighValueExpenses(threshold);

            // Then
            assertThat(result, hasSize(1));
            assertThat(result.get(0), hasExpenseAmount(new BigDecimal("150.00")));
            verify(expenseRepository).findByAmountGreaterThan(threshold);
        }
    }

    @Nested
    @DisplayName("Calculation and Analytics Operations")
    class CalculationAndAnalyticsOperations {

        @Test
        @DisplayName("Should calculate total expenses correctly")
        void getTotalExpenses_ShouldReturnTotalAmount() {
            // Given
            BigDecimal expectedTotal = new BigDecimal("500.00");
            when(expenseRepository.getTotalExpenses()).thenReturn(expectedTotal);

            // When
            BigDecimal result = expenseService.getTotalExpenses();

            // Then
            assertThat(result, is(expectedTotal));
            verify(expenseRepository).getTotalExpenses();
        }

        @Test
        @DisplayName("Should return zero when no expenses exist")
        void getTotalExpenses_ShouldReturnZero_WhenNoExpenses() {
            // Given
            when(expenseRepository.getTotalExpenses()).thenReturn(null);

            // When
            BigDecimal result = expenseService.getTotalExpenses();

            // Then
            assertThat(result, is(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should calculate total by category")
        void getTotalByCategory_ShouldReturnCategoryTotal() {
            // Given
            BigDecimal categoryTotal = new BigDecimal("150.00");
            when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
            when(expenseRepository.getTotalAmountByCategory(testCategory)).thenReturn(categoryTotal);

            // When
            BigDecimal result = expenseService.getTotalByCategory(1L);

            // Then
            assertThat(result, is(categoryTotal));
            verify(expenseRepository).getTotalAmountByCategory(testCategory);
        }

        @Test
        @DisplayName("Should get expense summary by category")
        void getExpenseSummaryByCategory_ShouldReturnCategorySummary() {
            // Given
            List<Object[]> mockResults = Arrays.asList(
                new Object[]{"Food", new BigDecimal("200.00")},
                new Object[]{"Transport", new BigDecimal("100.00")}
            );
            when(expenseRepository.getExpenseSummaryByCategory()).thenReturn(mockResults);

            // When
            Map<String, BigDecimal> result = expenseService.getExpenseSummaryByCategory();

            // Then
            assertThat(result.size(), is(2));
            assertThat(result.get("Food"), is(new BigDecimal("200.00")));
            assertThat(result.get("Transport"), is(new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("Should get comprehensive expense analytics")
        void getExpenseAnalytics_ShouldReturnComprehensiveAnalytics() {
            // Given
            BigDecimal totalAmount = new BigDecimal("1000.00");
            long totalCount = 20L;
            
            when(expenseRepository.getTotalExpenses()).thenReturn(totalAmount);
            when(expenseRepository.count()).thenReturn(totalCount);
            when(expenseRepository.getExpenseSummaryByCategory()).thenReturn(Collections.emptyList());

            // When
            Map<String, Object> analytics = expenseService.getExpenseAnalytics(null, null);

            // Then
            assertThat(analytics.get("totalAmount"), is(totalAmount));
            assertThat(analytics.get("totalCount"), is(totalCount));
            assertThat(analytics.get("averageExpense"), is(new BigDecimal("50.00")));
            assertThat(analytics, hasKey("categorySummary"));
            assertThat(analytics, hasKey("monthlySummary"));
        }
    }

    // Helper methods for test data creation
    private Category createTestCategory(Long id, String name, String description, String color, String icon) {
        Category category = new Category(name, description, color, icon);
        category.setId(id);
        category.setCreatedAt(LocalDateTime.now().minusDays(1));
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    private Expense createTestExpense(Long id, String description, BigDecimal amount, 
                                    LocalDate date, Category category) {
        Expense expense = new Expense(description, amount, date, category);
        expense.setId(id);
        expense.setCreatedAt(LocalDateTime.now().minusDays(1));
        expense.setUpdatedAt(LocalDateTime.now());
        return expense;
    }

    private List<Expense> createTestExpenseList() {
        return Arrays.asList(
            createTestExpense(1L, "Lunch at restaurant", new BigDecimal("25.50"), 
                LocalDate.now().minusDays(1), testCategory),
            createTestExpense(2L, "Gas station", new BigDecimal("45.00"), 
                LocalDate.now().minusDays(2), testCategory),
            createTestExpense(3L, "Grocery shopping", new BigDecimal("120.75"), 
                LocalDate.now().minusDays(3), testCategory)
        );
    }

    private List<Expense> createLargeExpenseList(int size) {
        return IntStream.range(1, size + 1)
            .mapToObj(i -> createTestExpense((long) i, "Expense " + i, 
                new BigDecimal("10.00"), LocalDate.now().minusDays(i % 30), testCategory))
            .collect(Collectors.toList());
    }
}
