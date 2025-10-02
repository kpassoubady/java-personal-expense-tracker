package com.expensetracker.app.controller;

import com.expensetracker.app.service.CategoryService;
import com.expensetracker.app.service.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Dashboard operations.
 *
 * Provides RESTful endpoints for refreshing dashboard data including
 * summary statistics, charts data, and recent activities.
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DashboardRestController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardRestController.class);

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;

    /**
     * GET /api/dashboard/refresh - Get refreshed dashboard data
     *
     * @return ResponseEntity with updated dashboard statistics and data
     */
    @GetMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshDashboard() {
        try {
            logger.info("Refreshing dashboard data...");
            
            Map<String, Object> dashboardData = buildDashboardData();
            addMetadata(dashboardData, "success");
            
            logger.info("Dashboard data refreshed successfully");
            return ResponseEntity.ok(dashboardData);
            
        } catch (Exception e) {
            logger.error("Error refreshing dashboard data", e);
            return buildErrorResponse("Failed to refresh dashboard data", e);
        }
    }
    
    /**
     * Build complete dashboard data map with statistics and trends.
     *
     * @return Map containing all dashboard data
     */
    private Map<String, Object> buildDashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        // Basic statistics
        data.put("totalExpenses", expenseService.getTotalExpenses());
        data.put("expenseCount", expenseService.getExpenseCount());
        data.put("categoryCount", categoryService.getAllCategories().size());
        
        // Activity data
        data.put("recentExpenses", expenseService.getRecentExpenses(5));
        data.put("categorySummary", expenseService.getExpenseSummaryByCategory());
        data.put("monthlyTrends", expenseService.getMonthlyExpenseSummary());
        
        // Monthly totals
        data.putAll(calculateMonthlyTotals());
        
        return data;
    }
    
    /**
     * Calculate current and previous month expense totals.
     *
     * @return Map containing current and previous month totals
     */
    private Map<String, Object> calculateMonthlyTotals() {
        MonthlyDateRanges ranges = new MonthlyDateRanges();
        
        return Map.of(
            "currentMonthTotal", expenseService.getTotalByDateRange(ranges.currentStart(), ranges.currentEnd()),
            "previousMonthTotal", expenseService.getTotalByDateRange(ranges.previousStart(), ranges.previousEnd())
        );
    }
    
    /**
     * Add metadata fields to response map.
     *
     * @param data response map to add metadata to
     * @param status status value (success/error)
     */
    private void addMetadata(Map<String, Object> data, String status) {
        data.put("lastUpdated", LocalDateTime.now());
        data.put("status", status);
    }
    
    /**
     * Build consistent error response structure.
     *
     * @param message user-friendly error message
     * @param e exception that occurred
     * @return ResponseEntity with error details
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("error", e.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Helper record for calculating monthly date ranges.
     * Encapsulates logic for current and previous month date calculations.
     */
    private record MonthlyDateRanges() {
        private static final LocalDate NOW = LocalDate.now();
        
        public LocalDate currentStart() { 
            return NOW.withDayOfMonth(1); 
        }
        
        public LocalDate currentEnd() { 
            return NOW.withDayOfMonth(NOW.lengthOfMonth()); 
        }
        
        public LocalDate previousStart() { 
            return NOW.minusMonths(1).withDayOfMonth(1); 
        }
        
        public LocalDate previousEnd() { 
            LocalDate prevMonth = NOW.minusMonths(1);
            return prevMonth.withDayOfMonth(prevMonth.lengthOfMonth()); 
        }
    }

    /**
     * GET /api/dashboard/category-stats - Get category statistics
     *
     * @return ResponseEntity with category statistics data
     */
    @GetMapping("/category-stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats() {
        try {
            logger.info("Fetching category statistics...");

            Map<String, Object> response = new HashMap<>();
            response.put("categorySummary", expenseService.getExpenseSummaryByCategory());
            addMetadata(response, "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching category statistics", e);
            return buildErrorResponse("Failed to fetch category statistics", e);
        }
    }

    /**
     * GET /api/dashboard/monthly-trends - Get monthly trends data
     *
     * @return ResponseEntity with monthly trends data
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<Map<String, Object>> getMonthlyTrends() {
        try {
            logger.info("Fetching monthly trends...");

            Map<String, Object> response = new HashMap<>();
            response.putAll(calculateMonthlyTotals());
            response.put("monthlyTrends", expenseService.getMonthlyExpenseSummary());
            addMetadata(response, "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching monthly trends", e);
            return buildErrorResponse("Failed to fetch monthly trends", e);
        }
    }

    /**
     * GET /api/dashboard/summary - Get dashboard summary statistics
     *
     * @return ResponseEntity with summary statistics
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        try {
            logger.info("Fetching dashboard summary...");

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalExpenses", expenseService.getTotalExpenses());
            summary.put("expenseCount", expenseService.getExpenseCount());
            summary.put("categoryCount", categoryService.getAllCategories().size());
            addMetadata(summary, "success");
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error fetching dashboard summary", e);
            return buildErrorResponse("Failed to fetch dashboard summary", e);
        }
    }
}