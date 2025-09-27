package com.expensetracker.app.controller;

import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.service.CategoryService;
import com.expensetracker.app.service.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Main controller for the home page and dashboard.
 * 
 * Provides comprehensive dashboard with expense summary, analytics, 
 * top categories, recent expenses, and monthly trends.
 */
@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Display the main dashboard/home page with comprehensive analytics.
     * 
     * @param model Spring MVC model
     * @return the home/dashboard view template
     */
    @GetMapping("/")
    public String home(Model model) {
        try {
            logger.info("Loading dashboard data...");
            
            // Initialize sample data if needed
            categoryService.createDefaultCategories();
            expenseService.createSampleExpenses();

            // Core dashboard statistics
            BigDecimal totalExpenses = expenseService.getTotalExpenses();
            long expenseCount = expenseService.getExpenseCount();
            long categoryCount = categoryService.getCategoryCount();
            
            model.addAttribute("totalExpenses", totalExpenses);
            model.addAttribute("expenseCount", expenseCount);
            model.addAttribute("categoryCount", categoryCount);
            
            // Calculate average expense
            BigDecimal averageExpense = expenseCount > 0 
                ? totalExpenses.divide(BigDecimal.valueOf(expenseCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            model.addAttribute("averageExpense", averageExpense);

            // Recent expenses (last 5)
            List<Expense> recentExpenses = expenseService.getRecentExpenses(5);
            model.addAttribute("recentExpenses", recentExpenses);
            logger.debug("Loaded {} recent expenses", recentExpenses.size());

            // Top spending categories with detailed statistics
            List<Map<String, Object>> categoryStats = expenseService.getTopSpendingCategories(5);
            model.addAttribute("categoryStats", categoryStats);
            
            // Category summary for pie chart
            Map<String, BigDecimal> categorySummary = expenseService.getExpenseSummaryByCategory();
            model.addAttribute("categorySummary", categorySummary);

            // Dashboard analytics including monthly trends
            Map<String, Object> analytics = expenseService.getExpenseAnalytics(null, null);
            model.addAttribute("analytics", analytics);
            
            // Monthly trends for current year
            Map<String, BigDecimal> monthlyTrends = expenseService.getMonthlyExpenseSummary();
            model.addAttribute("monthlyTrends", monthlyTrends);
            
            // Current month data
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
            
            BigDecimal currentMonthTotal = expenseService.getTotalByDateRange(startOfMonth, endOfMonth);
            model.addAttribute("currentMonthTotal", currentMonthTotal);
            model.addAttribute("currentMonthName", now.getMonth().toString());
            
            // Previous month comparison
            LocalDate previousMonth = now.minusMonths(1);
            LocalDate startOfPreviousMonth = previousMonth.withDayOfMonth(1);
            LocalDate endOfPreviousMonth = previousMonth.withDayOfMonth(previousMonth.lengthOfMonth());
            
            BigDecimal previousMonthTotal = expenseService.getTotalByDateRange(startOfPreviousMonth, endOfPreviousMonth);
            model.addAttribute("previousMonthTotal", previousMonthTotal);
            
            // Calculate month-over-month change
            BigDecimal monthlyChange = BigDecimal.ZERO;
            String changeDirection = "neutral";
            if (previousMonthTotal.compareTo(BigDecimal.ZERO) > 0) {
                monthlyChange = currentMonthTotal.subtract(previousMonthTotal)
                    .divide(previousMonthTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                changeDirection = monthlyChange.compareTo(BigDecimal.ZERO) > 0 ? "up" : 
                                 monthlyChange.compareTo(BigDecimal.ZERO) < 0 ? "down" : "neutral";
            }
            model.addAttribute("monthlyChange", monthlyChange.abs());
            model.addAttribute("changeDirection", changeDirection);
            
            // Dashboard status
            model.addAttribute("dashboardStatus", "success");
            model.addAttribute("lastUpdated", LocalDate.now());
            
            logger.info("Dashboard data loaded successfully - Total: {}, Count: {}, Categories: {}", 
                       totalExpenses, expenseCount, categoryCount);
            
            return "home/dashboard";
            
        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            
            // Set error state and minimal data
            model.addAttribute("dashboardStatus", "error");
            model.addAttribute("errorMessage", "Unable to load dashboard data: " + e.getMessage());
            
            // Provide safe defaults
            model.addAttribute("totalExpenses", BigDecimal.ZERO);
            model.addAttribute("expenseCount", 0L);
            model.addAttribute("categoryCount", 0L);
            model.addAttribute("averageExpense", BigDecimal.ZERO);
            model.addAttribute("recentExpenses", List.of());
            model.addAttribute("categoryStats", List.of());
            model.addAttribute("categorySummary", Map.of());
            model.addAttribute("monthlyTrends", Map.of());
            model.addAttribute("currentMonthTotal", BigDecimal.ZERO);
            model.addAttribute("previousMonthTotal", BigDecimal.ZERO);
            model.addAttribute("monthlyChange", BigDecimal.ZERO);
            model.addAttribute("changeDirection", "neutral");
            model.addAttribute("currentMonthName", LocalDate.now().getMonth().toString());
            
            // Still return the dashboard view but in error state
            return "home/dashboard";
        }
    }

    /**
     * Display the main dashboard (alternative mapping).
     * 
     * @param model Spring MVC model
     * @return redirect to home page
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "redirect:/";
    }

    /**
     * Display the about page.
     * 
     * @return the about view template
     */
    @GetMapping("/about")
    public String about() {
        return "home/about";
    }
    
    @GetMapping("/demo-layout")
    public String demoLayout(Model model) {
        model.addAttribute("pageTitle", "Layout Demo");
        model.addAttribute("success", "This is a success message using the new layout!");
        return "home/demo-layout-simple";
    }
}