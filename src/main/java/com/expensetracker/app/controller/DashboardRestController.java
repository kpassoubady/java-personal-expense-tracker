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

            Map<String, Object> dashboardData = new HashMap<>();
            
            // Get basic statistics
            dashboardData.put("totalExpenses", expenseService.getTotalExpenses());
            dashboardData.put("expenseCount", expenseService.getExpenseCount());
            dashboardData.put("categoryCount", categoryService.getAllCategories().size());
            
            // Get recent activities
            dashboardData.put("recentExpenses", expenseService.getRecentExpenses(5));
            dashboardData.put("categorySummary", expenseService.getExpenseSummaryByCategory());
            dashboardData.put("monthlyTrends", expenseService.getMonthlyExpenseSummary());
            
            // Get monthly totals
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
            LocalDate startOfPrevMonth = now.minusMonths(1).withDayOfMonth(1);
            LocalDate endOfPrevMonth = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
            
            dashboardData.put("currentMonthTotal", expenseService.getTotalByDateRange(startOfMonth, endOfMonth));
            dashboardData.put("previousMonthTotal", expenseService.getTotalByDateRange(startOfPrevMonth, endOfPrevMonth));
            
            // Add metadata
            dashboardData.put("lastUpdated", LocalDateTime.now());
            dashboardData.put("status", "success");
            
            logger.info("Dashboard data refreshed successfully");
            
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            logger.error("Error refreshing dashboard data", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to refresh dashboard data");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
            response.put("lastUpdated", LocalDateTime.now());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching category statistics", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to fetch category statistics");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
            
            // Calculate monthly totals
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
            LocalDate startOfPrevMonth = now.minusMonths(1).withDayOfMonth(1);
            LocalDate endOfPrevMonth = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
            
            response.put("currentMonthTotal", expenseService.getTotalByDateRange(startOfMonth, endOfMonth));
            response.put("previousMonthTotal", expenseService.getTotalByDateRange(startOfPrevMonth, endOfPrevMonth));
            response.put("monthlyTrends", expenseService.getMonthlyExpenseSummary());
            response.put("lastUpdated", LocalDateTime.now());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching monthly trends", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to fetch monthly trends");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
            summary.put("lastUpdated", LocalDateTime.now());
            summary.put("status", "success");
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error fetching dashboard summary", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to fetch dashboard summary");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}