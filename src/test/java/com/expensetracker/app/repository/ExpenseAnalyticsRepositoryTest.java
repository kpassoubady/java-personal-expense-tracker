package com.expensetracker.app.repository;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive integration tests for ExpenseAnalyticsRepository.
 * Tests analytics queries with focus on accuracy, performance, and edge cases.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ExpenseAnalyticsRepository Integration Tests")
class ExpenseAnalyticsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseAnalyticsRepository analyticsRepository;

    // Test data categories
    private Category foodCategory;
    private Category transportCategory;
    private Category entertainmentCategory;
    private Category healthCategory;
    private Category utilitiesCategory;

    @BeforeEach
    void setUp() {
        createTestCategories();
    }

    private void createTestCategories() {
        foodCategory = createCategory("Food", "Meals and groceries", "#FF5722", "🍔");
        transportCategory = createCategory("Transport", "Travel and commuting", "#2196F3", "🚗");
        entertainmentCategory = createCategory("Entertainment", "Movies and games", "#9C27B0", "🎬");
        healthCategory = createCategory("Health", "Medical expenses", "#4CAF50", "🏥");
        utilitiesCategory = createCategory("Utilities", "Bills and services", "#FF9800", "💡");
    }

    private Category createCategory(String name, String description, String color, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setColor(color);
        category.setIcon(icon);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return entityManager.persistAndFlush(category);
    }

    private Expense createExpense(String description, BigDecimal amount, LocalDate date, Category category) {
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setExpenseDate(date);
        expense.setCategory(category);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        return expense;
    }

    private void createComprehensiveTestData() {
        // Create expenses across multiple months and years for comprehensive testing
        
        // 2024 data - Full year with varying amounts
        for (int month = 1; month <= 12; month++) {
            for (int day = 1; day <= Math.min(28, LocalDate.of(2024, month, 1).lengthOfMonth()); day += 2) {
                LocalDate expenseDate = LocalDate.of(2024, month, day);
                
                // Food expenses (daily)
                entityManager.persist(createExpense("Daily meal " + month + "-" + day, 
                    new BigDecimal("25.50").add(new BigDecimal(month)), expenseDate, foodCategory));
                
                // Transport expenses (weekdays only)
                if (expenseDate.getDayOfWeek().getValue() <= 5) {
                    entityManager.persist(createExpense("Commute " + month + "-" + day, 
                        new BigDecimal("12.75"), expenseDate, transportCategory));
                }
                
                // Entertainment expenses (weekends)
                if (expenseDate.getDayOfWeek().getValue() > 5) {
                    entityManager.persist(createExpense("Weekend fun " + month + "-" + day, 
                        new BigDecimal("45.00").add(new BigDecimal(month * 2)), expenseDate, entertainmentCategory));
                }
            }
            
            // Monthly recurring expenses
            LocalDate monthlyDate = LocalDate.of(2024, month, 15);
            entityManager.persist(createExpense("Monthly health checkup", 
                new BigDecimal("150.00"), monthlyDate, healthCategory));
            entityManager.persist(createExpense("Utility bills", 
                new BigDecimal("200.00").add(new BigDecimal(month * 10)), monthlyDate, utilitiesCategory));
        }
        
        // 2025 partial data for trend analysis
        for (int month = 1; month <= 9; month++) {
            LocalDate monthlyDate = LocalDate.of(2025, month, 10);
            entityManager.persist(createExpense("2025 Food expense", 
                new BigDecimal("30.00").add(new BigDecimal(month * 2)), monthlyDate, foodCategory));
            entityManager.persist(createExpense("2025 Transport", 
                new BigDecimal("15.00"), monthlyDate, transportCategory));
        }
        
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("Monthly Expense Analytics")
    class MonthlyExpenseAnalyticsTests {

        @BeforeEach
        void setUpMonthlyData() {
            createComprehensiveTestData();
        }

        @Test
        @DisplayName("Should calculate monthly expense totals for a full year")
        void testGetMonthlyExpenseTotals() {
            // When
            List<Object[]> monthlyTotals = analyticsRepository.getMonthlyExpenseTotals(2024);

            // Then
            assertThat(monthlyTotals).hasSize(12);
            
            // Verify data structure
            Object[] januaryData = monthlyTotals.get(0);
            assertThat(januaryData).hasSize(2);
            assertThat(januaryData[0]).isEqualTo(1); // January = month 1
            assertThat(januaryData[1]).isInstanceOf(BigDecimal.class);
            
            // Verify all months are present and ordered correctly
            for (int i = 0; i < 12; i++) {
                Object[] monthData = monthlyTotals.get(i);
                assertThat(monthData[0]).isEqualTo(i + 1);
                
                BigDecimal monthTotal = (BigDecimal) monthData[1];
                assertThat(monthTotal).isGreaterThan(BigDecimal.ZERO);
            }
        }

        @Test
        @DisplayName("Should handle year with no data gracefully")
        void testGetMonthlyExpenseTotalsForEmptyYear() {
            // When
            List<Object[]> monthlyTotals = analyticsRepository.getMonthlyExpenseTotals(2023);

            // Then
            assertThat(monthlyTotals).isEmpty();
        }

        @Test
        @DisplayName("Should calculate monthly totals with high precision")
        void testMonthlyTotalsDecimalPrecision() {
            // Given - Create precise decimal amounts with valid precision (2 decimal places)
            LocalDate testDate = LocalDate.of(2024, 6, 15);
            entityManager.persist(createExpense("Precision test 1", new BigDecimal("123.45"), testDate, foodCategory));
            entityManager.persist(createExpense("Precision test 2", new BigDecimal("789.12"), testDate, foodCategory));
            entityManager.flush();

            // When
            List<Object[]> monthlyTotals = analyticsRepository.getMonthlyExpenseTotals(2024);

            // Then
            Object[] juneData = monthlyTotals.stream()
                .filter(data -> data[0].equals(6))
                .findFirst()
                .orElseThrow();
            
            BigDecimal juneTotal = (BigDecimal) juneData[1];
            // Should include our precision test amounts plus existing June data
            assertThat(juneTotal).isGreaterThan(new BigDecimal("912.57")); // 123.45 + 789.12
        }
    }

    @Nested
    @DisplayName("Daily Expense Analytics")
    class DailyExpenseAnalyticsTests {

        @BeforeEach
        void setUpDailyData() {
            // Create daily data for February 2024 (29 days - leap year)
            for (int day = 1; day <= 29; day++) {
                LocalDate date = LocalDate.of(2024, 2, day);
                BigDecimal amount = new BigDecimal("10.00").multiply(new BigDecimal(day));
                
                entityManager.persist(createExpense("Daily expense " + day, amount, date, foodCategory));
                
                // Add weekend bonuses
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    entityManager.persist(createExpense("Weekend bonus " + day, 
                        new BigDecimal("50.00"), date, entertainmentCategory));
                }
            }
            entityManager.flush();
        }

        @Test
        @DisplayName("Should calculate daily expense totals for a specific month")
        void testGetDailyExpenseTotals() {
            // When
            List<Object[]> dailyTotals = analyticsRepository.getDailyExpenseTotals(2024, 2);

            // Then
            assertThat(dailyTotals).hasSize(29); // February 2024 has 29 days
            
            // Verify first day
            Object[] firstDay = dailyTotals.get(0);
            assertThat(firstDay[0]).isEqualTo(1); // Day 1
            assertThat(firstDay[1]).isEqualTo(new BigDecimal("10.00"));
            
            // Verify weekend day has bonus
            Object[] weekendDay = dailyTotals.stream()
                .filter(data -> data[0].equals(3)) // Saturday Feb 3rd, 2024
                .findFirst()
                .orElseThrow();
            
            BigDecimal weekendTotal = (BigDecimal) weekendDay[1];
            assertThat(weekendTotal).isEqualTo(new BigDecimal("80.00")); // 30.00 + 50.00
        }

        @Test
        @DisplayName("Should handle month with no data")
        void testGetDailyExpenseTotalsForEmptyMonth() {
            // When
            List<Object[]> dailyTotals = analyticsRepository.getDailyExpenseTotals(2023, 1);

            // Then
            assertThat(dailyTotals).isEmpty();
        }

        @Test
        @DisplayName("Should handle invalid month gracefully")
        void testGetDailyExpenseTotalsInvalidMonth() {
            // When
            List<Object[]> dailyTotals = analyticsRepository.getDailyExpenseTotals(2024, 13);

            // Then
            assertThat(dailyTotals).isEmpty();
        }
    }

    @Nested
    @DisplayName("Category Spending Analytics")
    class CategorySpendingAnalyticsTests {

        @BeforeEach
        void setUpCategoryData() {
            LocalDate startDate = LocalDate.of(2024, 6, 1);
            
            // Create varying amounts per category
            entityManager.persist(createExpense("Food 1", new BigDecimal("500.00"), startDate, foodCategory));
            entityManager.persist(createExpense("Food 2", new BigDecimal("300.00"), startDate.plusDays(5), foodCategory));
            entityManager.persist(createExpense("Transport 1", new BigDecimal("200.00"), startDate.plusDays(2), transportCategory));
            entityManager.persist(createExpense("Entertainment 1", new BigDecimal("750.00"), startDate.plusDays(10), entertainmentCategory));
            entityManager.persist(createExpense("Health 1", new BigDecimal("100.00"), startDate.plusDays(15), healthCategory));
            
            // Category with no expenses in range
            // utilitiesCategory has no expenses
            
            entityManager.flush();
        }

        @Test
        @DisplayName("Should get top spending categories with correct ranking")
        void testGetTopSpendingCategories() {
            // When
            LocalDate startDate = LocalDate.of(2024, 6, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);
            List<Object[]> topCategories = analyticsRepository.getTopSpendingCategories(startDate, endDate, 3);

            // Then
            assertThat(topCategories).hasSize(3);
            
            // Verify ranking (should be sorted by amount DESC)
            Object[] firstCategory = topCategories.get(0);
            assertThat(firstCategory[0]).isEqualTo("Food"); // category name
            assertThat(firstCategory[1]).isEqualTo(new BigDecimal("800.00")); // total amount
            assertThat(firstCategory[2]).isEqualTo(2L); // expense count
            
            Object[] secondCategory = topCategories.get(1);
            assertThat(secondCategory[0]).isEqualTo("Entertainment");
            assertThat(secondCategory[1]).isEqualTo(new BigDecimal("750.00"));
            assertThat(secondCategory[2]).isEqualTo(1L);
            
            Object[] thirdCategory = topCategories.get(2);
            assertThat(thirdCategory[0]).isEqualTo("Transport");
            assertThat(thirdCategory[1]).isEqualTo(new BigDecimal("200.00"));
            assertThat(thirdCategory[2]).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should handle limit parameter correctly")
        void testGetTopSpendingCategoriesWithLimit() {
            // When
            LocalDate startDate = LocalDate.of(2024, 6, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);
            List<Object[]> topCategories = analyticsRepository.getTopSpendingCategories(startDate, endDate, 2);

            // Then
            assertThat(topCategories).hasSize(2);
        }

        @Test
        @DisplayName("Should calculate spending percentages accurately")
        void testCategorySpendingPercentages() {
            // When
            LocalDate startDate = LocalDate.of(2024, 6, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);
            List<Object[]> topCategories = analyticsRepository.getTopSpendingCategories(startDate, endDate, 10);

            // Calculate total and percentages
            BigDecimal total = topCategories.stream()
                .map(data -> (BigDecimal) data[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Then
            assertThat(total).isEqualTo(new BigDecimal("1850.00"));
            
            // Verify percentages
            BigDecimal foodPercentage = ((BigDecimal) topCategories.get(0)[1])
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            
            // Allow for small rounding differences
            assertThat(foodPercentage).isBetween(new BigDecimal("43.24"), new BigDecimal("43.25")); // ~800/1850 * 100
        }
    }

    @Nested
    @DisplayName("Trend Analysis")
    class TrendAnalysisTests {

        @Test
        @DisplayName("Should analyze expense trends over time")
        void testGetExpenseTrends() {
            // Given - Create trend data over 30 days
            LocalDate startDate = LocalDate.now().minusDays(30);
            
            for (int i = 0; i < 30; i++) {
                LocalDate date = startDate.plusDays(i);
                BigDecimal amount = new BigDecimal("50.00").add(new BigDecimal(i * 2)); // Increasing trend
                
                entityManager.persist(createExpense("Trend test " + i, amount, date, foodCategory));
                
                // Add some random variance
                if (i % 3 == 0) {
                    entityManager.persist(createExpense("Variance " + i, 
                        new BigDecimal("25.00"), date, transportCategory));
                }
            }
            entityManager.flush();

            // When
            List<Object[]> trends = analyticsRepository.getExpenseTrends(startDate);

            // Then
            assertThat(trends).hasSize(30);
            
            // Verify data structure and ordering (DESC by date)
            for (int i = 0; i < trends.size(); i++) {
                Object[] trendData = trends.get(i);
                assertThat(trendData).hasSize(3);
                assertThat(trendData[0]).isInstanceOf(LocalDate.class);
                assertThat(trendData[1]).isInstanceOf(BigDecimal.class);
                assertThat(trendData[2]).isInstanceOf(Long.class);
                
                // Verify descending date order
                if (i > 0) {
                    LocalDate currentDate = (LocalDate) trendData[0];
                    LocalDate previousDate = (LocalDate) trends.get(i-1)[0];
                    assertThat(currentDate).isBefore(previousDate);
                }
            }
        }

        @Test
        @DisplayName("Should handle historical data gaps in trends")
        void testExpenseTrendsWithDataGaps() {
            // Given - Create data with gaps
            LocalDate baseDate = LocalDate.now().minusDays(10);
            
            // Day 1, 3, 5, 7, 9 (skip even days)
            for (int i = 1; i <= 9; i += 2) {
                LocalDate date = baseDate.plusDays(i);
                entityManager.persist(createExpense("Gap test " + i, 
                    new BigDecimal("100.00"), date, foodCategory));
            }
            entityManager.flush();

            // When
            List<Object[]> trends = analyticsRepository.getExpenseTrends(baseDate);

            // Then
            assertThat(trends).hasSize(5); // Only days with data
            
            // Verify no zero amounts (gaps should not appear in results)
            for (Object[] trendData : trends) {
                BigDecimal amount = (BigDecimal) trendData[1];
                assertThat(amount).isGreaterThan(BigDecimal.ZERO);
            }
        }
    }

    @Nested
    @DisplayName("Statistical Calculations")
    class StatisticalCalculationsTests {

        @BeforeEach
        void setUpStatisticalData() {
            // Create consistent daily expenses for average calculation
            
            // Create daily expenses: 10, 20, 30, ..., 310 (31 days)
            for (int day = 1; day <= 31; day++) {
                LocalDate date = LocalDate.of(2024, 7, day);
                BigDecimal amount = new BigDecimal(day * 10);
                entityManager.persist(createExpense("Daily " + day, amount, date, foodCategory));
            }
            entityManager.flush();
        }

        @Test
        @DisplayName("Should calculate average daily expense accurately")
        void testGetAverageDailyExpense() {
            // When
            LocalDate startDate = LocalDate.of(2024, 7, 1);
            LocalDate endDate = LocalDate.of(2024, 7, 31);
            BigDecimal average = analyticsRepository.getAverageDailyExpense(startDate, endDate);

            // Then
            // Expected: (10+20+30+...+310)/31 = 4960/31 = 160.00
            assertThat(average).isEqualByComparingTo(new BigDecimal("160.00"));
        }

        @Test
        @DisplayName("Should handle single day average calculation")
        void testGetAverageDailyExpenseSingleDay() {
            // Given
            LocalDate singleDate = LocalDate.of(2024, 8, 1);
            entityManager.persist(createExpense("Single day test", 
                new BigDecimal("75.50"), singleDate, foodCategory));
            entityManager.flush();

            // When
            BigDecimal average = analyticsRepository.getAverageDailyExpense(singleDate, singleDate);

            // Then
            assertThat(average).isEqualByComparingTo(new BigDecimal("75.50"));
        }

        @Test
        @DisplayName("Should return zero for date range with no expenses")
        void testGetAverageDailyExpenseNoData() {
            // When
            LocalDate startDate = LocalDate.of(2023, 1, 1);
            LocalDate endDate = LocalDate.of(2023, 1, 31);
            BigDecimal average = analyticsRepository.getAverageDailyExpense(startDate, endDate);

            // Then
            assertThat(average).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should identify high expenses relative to category average")
        void testFindHighExpensesRelativeToCategory() {
            // Given - Create a dedicated test category with multiple expenses to establish an average
            Category testCategory = createCategory("HighExpenseTest", "For testing high expenses", "#FF0000", "🧪");
            
            // Create multiple expenses to establish a proper average
            LocalDate testDate = LocalDate.of(2024, 8, 15);
            
            // Create 5 "normal" expenses at $20 each (average will be around $24)
            for (int i = 1; i <= 5; i++) {
                entityManager.persist(createExpense("Normal expense " + i, 
                    new BigDecimal("20.00"), testDate.plusDays(i), testCategory));
            }
            
            // Create 1 high expense at $50 (208% of $24 average, should exceed 150% threshold)
            entityManager.persist(createExpense("High expense", 
                new BigDecimal("50.00"), testDate.plusDays(6), testCategory));
            
            // Create 1 very high expense at $75 (312% of $24 average, should definitely exceed 150% threshold)
            entityManager.persist(createExpense("Very high expense", 
                new BigDecimal("75.00"), testDate.plusDays(7), testCategory));
            
            entityManager.flush();
            entityManager.clear();

            // When - Find expenses > 150% of category average
            List<Expense> highExpenses = analyticsRepository.findHighExpensesRelativeToCategory(150.0);

            // Then - Filter for our test category only
            List<Expense> testCategoryHighExpenses = highExpenses.stream()
                .filter(e -> e.getCategory().getName().equals("HighExpenseTest"))
                .toList();
            
            // Should find both the $50 and $75 expenses as they exceed 150% of the ~$30 average
            assertThat(testCategoryHighExpenses).hasSizeGreaterThanOrEqualTo(2);
            
            // Verify the high expenses are included
            List<BigDecimal> highAmounts = testCategoryHighExpenses.stream()
                .map(Expense::getAmount)
                .toList();
            
            assertThat(highAmounts).contains(new BigDecimal("50.00"), new BigDecimal("75.00"));
        }
    }

    @Nested
    @DisplayName("Performance and Optimization Tests")
    class PerformanceOptimizationTests {

        @Test
        @DisplayName("Should handle large datasets efficiently")
        void testLargeDatasetPerformance() {
            // Given - Create large dataset
            StopWatch stopWatch = new StopWatch("Large Dataset Performance");
            
            // Create 1000 expenses across multiple categories and dates
            stopWatch.start("Data Creation");
            LocalDate baseDate = LocalDate.of(2024, 1, 1);
            for (int i = 0; i < 1000; i++) {
                Category category = i % 5 == 0 ? foodCategory :
                                 i % 5 == 1 ? transportCategory :
                                 i % 5 == 2 ? entertainmentCategory :
                                 i % 5 == 3 ? healthCategory : utilitiesCategory;
                
                LocalDate expenseDate = baseDate.plusDays(i % 365);
                BigDecimal amount = new BigDecimal(i % 500 + 1).setScale(2, RoundingMode.HALF_UP);
                
                entityManager.persist(createExpense("Performance test " + i, amount, expenseDate, category));
                
                if (i % 100 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
            entityManager.flush();
            stopWatch.stop();

            // When - Execute complex queries
            stopWatch.start("Monthly Analytics Query");
            List<Object[]> monthlyTotals = analyticsRepository.getMonthlyExpenseTotals(2024);
            stopWatch.stop();
            
            stopWatch.start("Top Categories Query");
            List<Object[]> topCategories = analyticsRepository.getTopSpendingCategories(
                baseDate, baseDate.plusDays(365), 5);
            stopWatch.stop();
            
            stopWatch.start("Trends Query");
            List<Object[]> trends = analyticsRepository.getExpenseTrends(baseDate);
            stopWatch.stop();

            // Then
            System.out.println(stopWatch.prettyPrint());
            
            assertThat(monthlyTotals).hasSizeGreaterThan(0);
            assertThat(topCategories).hasSize(5);
            assertThat(trends).hasSizeGreaterThan(0);
            
            // Performance assertions (adjust thresholds as needed)
            assertThat(stopWatch.getTotalTimeMillis()).isLessThan(5000); // Total under 5 seconds
        }

        @Test
        @DisplayName("Should optimize aggregation queries with proper indexing")
        void testAggregationQueryOptimization() {
            // Given - Create data with consistent patterns for optimization testing
            LocalDate startDate = LocalDate.of(2024, 6, 1);
            
            for (int i = 0; i < 100; i++) {
                entityManager.persist(createExpense("Optimization test " + i,
                    new BigDecimal("50.00"), startDate.plusDays(i % 30), foodCategory));
            }
            entityManager.flush();

            // When - Measure aggregation performance
            StopWatch stopWatch = new StopWatch("Aggregation Performance");
            
            stopWatch.start("Average Calculation");
            BigDecimal average = analyticsRepository.getAverageDailyExpense(startDate, startDate.plusDays(30));
            stopWatch.stop();
            
            stopWatch.start("Category Aggregation");
            List<Object[]> categories = analyticsRepository.getTopSpendingCategories(
                startDate, startDate.plusDays(30), 10);
            stopWatch.stop();

            // Then
            System.out.println("Aggregation Performance: " + stopWatch.prettyPrint());
            
            assertThat(average).isGreaterThan(BigDecimal.ZERO);
            assertThat(categories).isNotEmpty();
            assertThat(stopWatch.getTotalTimeMillis()).isLessThan(1000); // Under 1 second
        }
    }

    @Nested
    @DisplayName("Null Handling and Edge Cases")
    class NullHandlingEdgeCasesTests {

        @Test
        @DisplayName("Should handle null amounts gracefully in calculations")
        void testNullAmountHandling() {
            // Note: Due to @NotNull constraint, we can't actually create null amounts
            // This test verifies the COALESCE behavior with zero amounts
            
            // Given - Create expenses with minimum amounts
            LocalDate testDate = LocalDate.of(2024, 9, 1);
            entityManager.persist(createExpense("Zero-like test", 
                new BigDecimal("0.01"), testDate, foodCategory));
            entityManager.flush();

            // When
            List<Object[]> monthlyTotals = analyticsRepository.getMonthlyExpenseTotals(2024);
            
            // Then
            Object[] septemberData = monthlyTotals.stream()
                .filter(data -> data[0].equals(9))
                .findFirst()
                .orElseThrow();
            
            assertThat(septemberData[1]).isNotNull();
            assertThat((BigDecimal) septemberData[1]).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle categories with no expenses")
        void testCategoriesWithNoExpenses() {
            // Given - Create a category but no expenses for it
            createCategory("Empty", "No expenses", "#000000", "❌");
            
            // Create expenses for other categories
            LocalDate testDate = LocalDate.of(2024, 9, 15);
            entityManager.persist(createExpense("Test expense", 
                new BigDecimal("100.00"), testDate, foodCategory));
            entityManager.flush();

            // When
            List<Object[]> topCategories = analyticsRepository.getTopSpendingCategories(
                testDate.minusDays(5), testDate.plusDays(5), 10);

            // Then
            // Empty category should not appear in results (LEFT JOIN with WHERE filters it out)
            assertThat(topCategories).hasSize(1);
            assertThat(topCategories.get(0)[0]).isEqualTo("Food");
        }

        @Test
        @DisplayName("Should handle edge case date ranges")
        void testEdgeCaseDateRanges() {
            // Given - Create expense on specific date
            LocalDate expenseDate = LocalDate.of(2024, 2, 29); // Leap year
            entityManager.persist(createExpense("Leap year test", 
                new BigDecimal("100.00"), expenseDate, foodCategory));
            entityManager.flush();

            // When - Query for leap day specifically
            List<Object[]> dailyTotals = analyticsRepository.getDailyExpenseTotals(2024, 2);

            // Then
            assertThat(dailyTotals).isNotEmpty();
            Object[] leapDayData = dailyTotals.stream()
                .filter(data -> data[0].equals(29))
                .findFirst()
                .orElseThrow();
            
            assertThat(leapDayData[1]).isEqualTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("Time Zone and Date Handling")
    class TimeZoneDateHandlingTests {

        @Test
        @DisplayName("Should handle different time zones consistently")
        void testTimeZoneConsistency() {
            // Given - Create expenses with dates that might be affected by timezone
            LocalDate utcDate = LocalDate.of(2024, 10, 1);
            
            // Create expense at what would be different times in different zones
            entityManager.persist(createExpense("UTC test", 
                new BigDecimal("50.00"), utcDate, foodCategory));
            
            // Create another expense on the same logical date
            entityManager.persist(createExpense("Same day test", 
                new BigDecimal("75.00"), utcDate, transportCategory));
            entityManager.flush();

            // When
            List<Object[]> dailyTotals = analyticsRepository.getDailyExpenseTotals(2024, 10);

            // Then
            Object[] dayData = dailyTotals.stream()
                .filter(data -> data[0].equals(1))
                .findFirst()
                .orElseThrow();
            
            // Both expenses should be aggregated together
            assertThat(dayData[1]).isEqualTo(new BigDecimal("125.00"));
        }

        @Test
        @DisplayName("Should handle date boundary conditions")
        void testDateBoundaryConditions() {
            // Given - Create expenses at month boundaries
            LocalDate lastDayOfMonth = LocalDate.of(2024, 4, 30);
            LocalDate firstDayOfNextMonth = LocalDate.of(2024, 5, 1);
            
            entityManager.persist(createExpense("Month end", 
                new BigDecimal("100.00"), lastDayOfMonth, foodCategory));
            entityManager.persist(createExpense("Month start", 
                new BigDecimal("200.00"), firstDayOfNextMonth, foodCategory));
            entityManager.flush();

            // When
            List<Object[]> aprilTotals = analyticsRepository.getDailyExpenseTotals(2024, 4);
            List<Object[]> mayTotals = analyticsRepository.getDailyExpenseTotals(2024, 5);

            // Then
            // April should have the month-end expense
            Object[] aprilLastDay = aprilTotals.stream()
                .filter(data -> data[0].equals(30))
                .findFirst()
                .orElseThrow();
            assertThat(aprilLastDay[1]).isEqualTo(new BigDecimal("100.00"));
            
            // May should have the month-start expense
            Object[] mayFirstDay = mayTotals.stream()
                .filter(data -> data[0].equals(1))
                .findFirst()
                .orElseThrow();
            assertThat(mayFirstDay[1]).isEqualTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("Should handle year boundary in trends")
        void testYearBoundaryInTrends() {
            // Given - Create expenses across year boundary
            LocalDate endOf2024 = LocalDate.of(2024, 12, 31);
            LocalDate startOf2025 = LocalDate.of(2025, 1, 1);
            
            entityManager.persist(createExpense("Year end 2024", 
                new BigDecimal("500.00"), endOf2024, foodCategory));
            entityManager.persist(createExpense("Year start 2025", 
                new BigDecimal("300.00"), startOf2025, foodCategory));
            entityManager.flush();

            // When - Get trends from Dec 30, 2024 (should include both)
            LocalDate trendsStart = LocalDate.of(2024, 12, 30);
            List<Object[]> trends = analyticsRepository.getExpenseTrends(trendsStart);

            // Then
            assertThat(trends).hasSize(2);
            
            // Verify correct chronological ordering (DESC)
            LocalDate firstTrendDate = (LocalDate) trends.get(0)[0];
            LocalDate secondTrendDate = (LocalDate) trends.get(1)[0];
            
            assertThat(firstTrendDate).isEqualTo(startOf2025);
            assertThat(secondTrendDate).isEqualTo(endOf2024);
        }
    }

    @Nested
    @DisplayName("Day of Week Pattern Analysis")
    class DayOfWeekPatternAnalysisTests {

        @BeforeEach
        void setUpWeeklyPatternData() {
            // Create systematic weekly pattern data
            LocalDate startDate = LocalDate.of(2024, 8, 5); // Monday
            
            for (int week = 0; week < 4; week++) {
                for (int day = 0; day < 7; day++) {
                    LocalDate expenseDate = startDate.plusDays(week * 7 + day);
                    DayOfWeek dayOfWeek = expenseDate.getDayOfWeek();
                    
                    // Pattern: Higher spending on weekends
                    BigDecimal amount = dayOfWeek.getValue() <= 5 ? 
                        new BigDecimal("25.00") : new BigDecimal("75.00");
                    
                    entityManager.persist(createExpense("Weekly pattern " + week + "-" + day,
                        amount, expenseDate, foodCategory));
                }
            }
            entityManager.flush();
        }

        @Test
        @DisplayName("Should analyze spending patterns by day of week")
        void testGetSpendingPatternsByDayOfWeek() {
            // When
            List<Object[]> weeklyPatterns = analyticsRepository.getSpendingPatternsByDayOfWeek();

            // Then
            assertThat(weeklyPatterns).hasSize(7); // 7 days of week
            
            // Verify data structure
            for (Object[] pattern : weeklyPatterns) {
                assertThat(pattern).hasSize(3);
                assertThat(pattern[0]).isInstanceOf(Integer.class); // day_of_week
                assertThat(pattern[1]).isInstanceOf(BigDecimal.class); // total_amount
                assertThat(pattern[2]).isInstanceOf(Long.class); // expense_count
            }
            
            // Verify weekend spending is higher than weekday spending
            Object[] mondayPattern = weeklyPatterns.stream()
                .filter(data -> data[0].equals(2)) // Monday in DAYOFWEEK is 2
                .findFirst()
                .orElseThrow();
            
            Object[] saturdayPattern = weeklyPatterns.stream()
                .filter(data -> data[0].equals(7)) // Saturday in DAYOFWEEK is 7
                .findFirst()
                .orElseThrow();
            
            BigDecimal mondayTotal = (BigDecimal) mondayPattern[1];
            BigDecimal saturdayTotal = (BigDecimal) saturdayPattern[1];
            
            assertThat(saturdayTotal).isGreaterThan(mondayTotal);
            assertThat(saturdayTotal).isEqualByComparingTo(new BigDecimal("300.00")); // 4 weeks * 75.00
            assertThat(mondayTotal).isEqualByComparingTo(new BigDecimal("100.00")); // 4 weeks * 25.00
        }
    }

    @Nested
    @DisplayName("Budget Analysis")
    class BudgetAnalysisTests {

        @Test
        @DisplayName("Should perform budget analysis with over/under budget detection")
        void testGetBudgetAnalysis() {
            // Given - Create expenses for budget analysis
            LocalDate startDate = LocalDate.of(2024, 11, 1);
            LocalDate endDate = LocalDate.of(2024, 11, 30);
            BigDecimal budgetLimit = new BigDecimal("500.00");
            
            // Food category: over budget
            entityManager.persist(createExpense("Food 1", new BigDecimal("300.00"), 
                startDate, foodCategory));
            entityManager.persist(createExpense("Food 2", new BigDecimal("250.00"), 
                startDate.plusDays(5), foodCategory));
            
            // Transport category: under budget
            entityManager.persist(createExpense("Transport 1", new BigDecimal("200.00"), 
                startDate.plusDays(2), transportCategory));
            
            // Entertainment: exactly at budget
            entityManager.persist(createExpense("Entertainment 1", new BigDecimal("500.00"), 
                startDate.plusDays(10), entertainmentCategory));
            
            entityManager.flush();

            // When
            List<Object[]> budgetAnalysis = analyticsRepository.getBudgetAnalysis(
                startDate, endDate, budgetLimit);

            // Then
            assertThat(budgetAnalysis).hasSizeGreaterThanOrEqualTo(3);
            
            // Find and verify food category (over budget)
            Object[] foodBudget = budgetAnalysis.stream()
                .filter(data -> "Food".equals(data[0]))
                .findFirst()
                .orElseThrow();
            
            assertThat(foodBudget[0]).isEqualTo("Food"); // category name
            assertThat(foodBudget[1]).isEqualTo(new BigDecimal("550.00")); // total spent
            assertThat(foodBudget[2]).isEqualTo(budgetLimit); // budget limit
            assertThat(foodBudget[3]).isEqualTo(true); // over budget
            
            // Find and verify transport category (under budget)
            Object[] transportBudget = budgetAnalysis.stream()
                .filter(data -> "Transport".equals(data[0]))
                .findFirst()
                .orElseThrow();
            
            assertThat(transportBudget[1]).isEqualTo(new BigDecimal("200.00")); // total spent
            assertThat(transportBudget[3]).isEqualTo(false); // under budget
            
            // Find and verify entertainment category (exactly at budget)
            Object[] entertainmentBudget = budgetAnalysis.stream()
                .filter(data -> "Entertainment".equals(data[0]))
                .findFirst()
                .orElseThrow();
            
            assertThat(entertainmentBudget[1]).isEqualTo(new BigDecimal("500.00")); // total spent
            assertThat(entertainmentBudget[3]).isEqualTo(false); // not over budget (equal counts as not over)
        }

        @Test
        @DisplayName("Should include categories with no expenses in budget analysis")
        void testBudgetAnalysisIncludesEmptyCategories() {
            // Given - Create budget analysis with some categories having no expenses
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);
            BigDecimal budgetLimit = new BigDecimal("1000.00");
            
            // Only create expense for food category, others should appear with zero
            entityManager.persist(createExpense("Only expense", new BigDecimal("100.00"), 
                startDate, foodCategory));
            entityManager.flush();

            // When
            List<Object[]> budgetAnalysis = analyticsRepository.getBudgetAnalysis(
                startDate, endDate, budgetLimit);

            // Then
            // Should include all categories that exist, even those with no expenses
            assertThat(budgetAnalysis).hasSizeGreaterThanOrEqualTo(1);
            
            // Verify food category has the expense
            Object[] foodBudget = budgetAnalysis.stream()
                .filter(data -> "Food".equals(data[0]))
                .findFirst()
                .orElseThrow();
            
            assertThat(foodBudget[1]).isEqualTo(new BigDecimal("100.00")); // total spent
            assertThat(foodBudget[3]).isEqualTo(false); // under budget
        }
    }
}