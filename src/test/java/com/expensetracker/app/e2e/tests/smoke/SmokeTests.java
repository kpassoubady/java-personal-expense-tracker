package com.expensetracker.app.e2e.tests.smoke;

import com.expensetracker.app.e2e.base.BaseTest;
import com.expensetracker.app.e2e.listeners.ExtentReportListener;
import com.expensetracker.app.e2e.listeners.RetryAnalyzer;
import com.expensetracker.app.e2e.pages.HomePage;
import com.expensetracker.app.e2e.utils.AssertUtils;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

/**
 * Smoke tests for basic application functionality
 * Tests critical paths and core features
 */
public class SmokeTests extends BaseTest {
    
    @Test(groups = {"smoke", "critical"}, 
          description = "Verify application loads and dashboard is accessible",
          retryAnalyzer = RetryAnalyzer.class)
    public void testApplicationLoad() {
        ExtentReportListener.logInfo("Starting application load test");
        
        // Navigate to home page
        driver.get(config.getBaseUrl());
        ExtentReportListener.logInfo("Navigated to: " + config.getBaseUrl());
        
        // Create HomePage instance and verify
        HomePage homePage = new HomePage();
        homePage.verifyDashboardLoaded();
        ExtentReportListener.logPass("Dashboard loaded successfully");
        
        // Verify page title
        AssertUtils.assertPageTitleContains("Expense Tracker", "Page title should contain 'Expense Tracker'");
        ExtentReportListener.logPass("Page title verification successful");
        
        // Verify essential dashboard elements by checking if they exist
        boolean hasAddButton = homePage.isVisible(By.id("addExpenseBtn"));
        boolean hasViewAllButton = homePage.isVisible(By.id("viewAllExpensesBtn"));
        
        AssertUtils.assertTrue(hasAddButton, "Add Expense button should be visible");
        AssertUtils.assertTrue(hasViewAllButton, "View All Expenses button should be visible");
        ExtentReportListener.logPass("Dashboard elements verification successful");
        
        // Log dashboard summary
        String summary = homePage.getDashboardSummary();
        ExtentReportListener.logInfo("Dashboard Summary: " + summary);
    }
    
    @Test(groups = {"smoke", "navigation"}, 
          description = "Verify navigation between main pages",
          retryAnalyzer = RetryAnalyzer.class)
    public void testMainNavigation() {
        ExtentReportListener.logInfo("Starting main navigation test");
        
        // Start from home page
        driver.get(config.getBaseUrl());
        HomePage homePage = new HomePage();
        homePage.verifyDashboardLoaded();
        ExtentReportListener.logInfo("Starting from Dashboard");
        
        // Navigate to Expenses page
        var expensesPage = homePage.navigateToExpenses();
        expensesPage.verifyExpensesPageLoaded();
        ExtentReportListener.logPass("Navigation to Expenses page successful");
        
        // Navigate to Categories page
        var categoriesPage = expensesPage.navigateToCategories();
        categoriesPage.verifyCategoriesPageLoaded();
        ExtentReportListener.logPass("Navigation to Categories page successful");
        
        // Navigate back to Home
        homePage = categoriesPage.navigateToHome();
        homePage.verifyDashboardLoaded();
        ExtentReportListener.logPass("Navigation back to Dashboard successful");
        
        ExtentReportListener.logInfo("Main navigation test completed successfully");
    }
    
    @Test(groups = {"smoke", "data"}, 
          description = "Verify default categories exist",
          retryAnalyzer = RetryAnalyzer.class)
    public void testDefaultCategoriesExist() {
        ExtentReportListener.logInfo("Starting default categories test");
        
        // Navigate to categories page
        driver.get(config.getBaseUrl() + "/categories");
        var categoriesPage = new com.expensetracker.app.e2e.pages.CategoriesPage();
        categoriesPage.verifyCategoriesPageLoaded();
        ExtentReportListener.logInfo("Navigated to Categories page");
        
        // Verify system categories exist
        int systemCategoriesCount = categoriesPage.getSystemCategoriesCount();
        ExtentReportListener.logInfo("Found " + systemCategoriesCount + " system categories");
        
        AssertUtils.assertGreaterThan(systemCategoriesCount, 0, "At least one system category should exist");
        ExtentReportListener.logPass("Default categories verification successful");
        
        // Check for specific default categories
        String[] expectedCategories = {"Food", "Transportation", "Other"};
        for (String category : expectedCategories) {
            if (categoriesPage.isCategoryPresent(category)) {
                ExtentReportListener.logPass("Found expected category: " + category);
            } else {
                ExtentReportListener.logWarning("Expected category not found: " + category);
            }
        }
    }
    
    @Test(groups = {"smoke", "ui"}, 
          description = "Verify dashboard statistics display correctly",
          retryAnalyzer = RetryAnalyzer.class)
    public void testDashboardStatistics() {
        ExtentReportListener.logInfo("Starting dashboard statistics test");
        
        // Navigate to home page
        driver.get(config.getBaseUrl());
        HomePage homePage = new HomePage();
        homePage.verifyDashboardLoaded();
        
        // Verify statistics are numeric and properly formatted
        homePage.verifyStatisticsAreNumeric();
        ExtentReportListener.logPass("Dashboard statistics are properly formatted");
        
        // Get and validate statistics values
        String totalExpenses = homePage.getTotalExpenses();
        String expenseCount = homePage.getExpenseCount();
        String categoriesCount = homePage.getCategoriesCount();
        
        ExtentReportListener.logInfo("Total Expenses: " + totalExpenses);
        ExtentReportListener.logInfo("Expense Count: " + expenseCount);
        ExtentReportListener.logInfo("Categories Count: " + categoriesCount);
        
        // Verify non-negative values
        AssertUtils.assertGreaterThanOrEqual(Integer.parseInt(expenseCount), 0, 
            "Expense count should be non-negative");
        AssertUtils.assertGreaterThan(Integer.parseInt(categoriesCount), 0, 
            "Categories count should be greater than 0");
        
        ExtentReportListener.logPass("Dashboard statistics validation successful");
    }
}