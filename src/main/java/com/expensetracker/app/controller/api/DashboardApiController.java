package com.expensetracker.app.controller.api;

import com.expensetracker.app.service.CategoryService;
import com.expensetracker.app.service.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for Dashboard Data
 * Provides JSON endpoints for AJAX requests from dashboard.js
 */
@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class DashboardApiController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardApiController.class);

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Get expenses grouped by category for pie chart
     * GET /api/expenses/by-category
     */
    @GetMapping("/by-category")
    public ResponseEntity<List<Map<String, Object>>> getExpensesByCategory() {
        try {
            logger.info("Fetching expenses by category for dashboard");
            
            // Get category summary from service
            Map<String, BigDecimal> categorySummary = expenseService.getCategorySummary();
            
            if (categorySummary == null || categorySummary.isEmpty()) {
                logger.warn("No expense data found for category chart");
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Convert to the format expected by Chart.js
            List<Map<String, Object>> result = categorySummary.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("category", entry.getKey());
                    item.put("amount", entry.getValue().doubleValue());
                    item.put("formattedAmount", String.format("$%.2f", entry.getValue()));
                    return item;
                })
                .collect(Collectors.toList());

            logger.info("Retrieved {} categories with expenses", result.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error fetching expenses by category", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get monthly expense trends for bar chart
     * GET /api/expenses/monthly-trends?months=6
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrends(
            @RequestParam(defaultValue = "6") int months) {
        try {
            logger.info("Fetching monthly expense trends for {} months", months);
            
            if (months <= 0 || months > 12) {
                months = 6; // Default to 6 months
            }

            List<Map<String, Object>> result = new ArrayList<>();
            LocalDate currentDate = LocalDate.now();
            
            // Get data for the requested number of months
            for (int i = months - 1; i >= 0; i--) {
                LocalDate monthDate = currentDate.minusMonths(i);
                LocalDate startDate = monthDate.withDayOfMonth(1);
                LocalDate endDate = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
                
                // Get expenses for this month
                BigDecimal monthlyTotal = expenseService.getTotalExpensesByDateRange(startDate, endDate);
                long expenseCount = expenseService.getExpenseCountByDateRange(startDate, endDate);
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                monthData.put("fullMonth", monthDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                monthData.put("year", monthDate.getYear());
                monthData.put("amount", monthlyTotal.doubleValue());
                monthData.put("count", expenseCount);
                monthData.put("formattedAmount", String.format("$%.2f", monthlyTotal));
                
                result.add(monthData);
            }

            logger.info("Retrieved monthly trends for {} months", result.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error fetching monthly expense trends", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get dashboard summary statistics
     * GET /api/expenses/dashboard-summary
     */
    @GetMapping("/dashboard-summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        try {
            logger.info("Fetching dashboard summary statistics");
            
            Map<String, Object> summary = new HashMap<>();
            
            // Total expenses
            BigDecimal totalExpenses = expenseService.getTotalExpenses();
            summary.put("totalExpenses", totalExpenses.doubleValue());
            summary.put("formattedTotalExpenses", String.format("$%.2f", totalExpenses));
            
            // Total count
            long totalCount = expenseService.getExpenseCount();
            summary.put("totalCount", totalCount);
            
            // Category count
            long categoryCount = categoryService.getCategoryCount();
            summary.put("categoryCount", categoryCount);
            
            // This month's expenses
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
            
            BigDecimal thisMonth = expenseService.getTotalExpensesByDateRange(startOfMonth, endOfMonth);
            summary.put("thisMonth", thisMonth.doubleValue());
            summary.put("formattedThisMonth", String.format("$%.2f", thisMonth));
            
            // Average expense
            BigDecimal averageExpense = totalCount > 0 ? 
                totalExpenses.divide(BigDecimal.valueOf(totalCount), 2, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            summary.put("averageExpense", averageExpense.doubleValue());
            summary.put("formattedAverageExpense", String.format("$%.2f", averageExpense));
            
            // Timestamp
            summary.put("lastUpdated", System.currentTimeMillis());
            summary.put("lastUpdatedFormatted", new Date().toString());
            
            logger.info("Dashboard summary: Total=${}, Count={}, Categories={}", 
                totalExpenses, totalCount, categoryCount);
            
            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            logger.error("Error fetching dashboard summary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get recent expenses for dashboard
     * GET /api/expenses/recent?limit=5
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentExpenses(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            logger.info("Fetching {} recent expenses for dashboard", limit);
            
            if (limit <= 0 || limit > 50) {
                limit = 5; // Default to 5, max 50
            }

            var recentExpenses = expenseService.getRecentExpenses(limit);
            
            List<Map<String, Object>> result = recentExpenses.stream()
                .map(expense -> {
                    Map<String, Object> expenseMap = new HashMap<>();
                    expenseMap.put("id", expense.getId());
                    expenseMap.put("description", expense.getDescription());
                    expenseMap.put("amount", expense.getAmount().doubleValue());
                    expenseMap.put("formattedAmount", String.format("$%.2f", expense.getAmount()));
                    expenseMap.put("expenseDate", expense.getExpenseDate().toString());
                    expenseMap.put("createdAt", expense.getCreatedAt().toString());
                    
                    if (expense.getCategory() != null) {
                        Map<String, Object> categoryMap = new HashMap<>();
                        categoryMap.put("id", expense.getCategory().getId());
                        categoryMap.put("name", expense.getCategory().getName());
                        categoryMap.put("color", expense.getCategory().getColor());
                        categoryMap.put("icon", expense.getCategory().getIcon());
                        expenseMap.put("category", categoryMap);
                    }
                    
                    return expenseMap;
                })
                .collect(Collectors.toList());

            logger.info("Retrieved {} recent expenses", result.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error fetching recent expenses", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/expenses/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "OK");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Dashboard API");
        health.put("version", "1.0");
        
        logger.debug("Health check requested - OK");
        return ResponseEntity.ok(health);
    }
}