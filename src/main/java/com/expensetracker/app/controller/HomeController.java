package com.expensetracker.app.controller;

import com.expensetracker.app.service.CategoryService;
import com.expensetracker.app.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Main controller for the home page and dashboard.
 * 
 * Provides overview statistics and navigation to main application features.
 */
@Controller
public class HomeController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Display the main dashboard/home page.
     * 
     * @param model Spring MVC model
     * @return the home/dashboard view template
     */
    @GetMapping("/")
    public String home(Model model) {
        // Initialize sample data if needed
        categoryService.createDefaultCategories();
        expenseService.createSampleExpenses();

        // Dashboard statistics
        model.addAttribute("totalExpenses", expenseService.getTotalExpenses());
        model.addAttribute("expenseCount", expenseService.getExpenseCount());
        model.addAttribute("categoryCount", categoryService.getCategoryCount());
        
        // Recent expenses (last 5)
        model.addAttribute("recentExpenses", expenseService.getRecentExpenses(5));
        
        // Top spending categories
        Map<String, BigDecimal> categorySummary = expenseService.getExpenseSummaryByCategory();
        model.addAttribute("categorySummary", categorySummary);
        
        return "home";
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
}