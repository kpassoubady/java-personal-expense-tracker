package com.expensetracker.app.e2e.tests.common;

import com.expensetracker.app.e2e.listeners.ExtentReportListener;
import com.expensetracker.app.e2e.pages.*;
import com.expensetracker.app.e2e.utils.AssertUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Common utility methods for UI tests to avoid code duplication
 * Provides reusable methods for common testing workflows
 */
public class TestCommonUtils {
    
    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Create a complete expense with all required fields
     * @param homePage The home page instance
     * @param description The expense description
     * @param amount The expense amount
     * @param category The expense category
     * @param date The expense date (YYYY-MM-DD format)
     * @param notes Optional notes
     * @return HomePage instance after expense creation
     */
    public static HomePage createCompleteExpense(HomePage homePage, String description, 
                                               String amount, String category, String date, String notes) {
        ExtentReportListener.logInfo("Creating expense: " + description + " - $" + amount);
        
        // Navigate to expense form
        ExpenseFormPage expenseFormPage = homePage.clickAddExpense();
        expenseFormPage.verifyExpenseFormLoaded();
        
        // Fill expense form
        expenseFormPage.fillExpenseForm(description, amount, category, date, notes);
        
        // Save expense and return to home page
        ExpensesPage expensesPage = expenseFormPage.saveExpense();
        
        ExtentReportListener.logPass("Expense created successfully: " + description);
        return expensesPage.navigateToHome();
    }
    
    /**
     * Create a test expense with generated data
     * @param homePage The home page instance
     * @return HomePage instance after expense creation
     */
    public static HomePage createTestExpense(HomePage homePage) {
        String description = "Test Expense " + System.currentTimeMillis();
        String amount = String.valueOf(50 + random.nextInt(200)); // Random amount between 50-250
        String category = "Food"; // Default category
        String date = LocalDate.now().format(DATE_FORMAT);
        String notes = "Test expense notes";
        
        return createCompleteExpense(homePage, description, amount, category, date, notes);
    }
    
    /**
     * Edit an existing expense
     * @param expensesPage The expenses page instance
     * @param originalDescription The original expense description to find
     * @param newDescription The new description
     * @param newAmount The new amount
     * @param newCategory The new category
     * @param newNotes The new notes
     * @return ExpensesPage instance after editing
     */
    public static ExpensesPage editExpense(ExpensesPage expensesPage, String originalDescription,
                                          String newDescription, String newAmount, 
                                          String newCategory, String newNotes) {
        ExtentReportListener.logInfo("Editing expense: " + originalDescription);
        
        // Find and edit expense
        ExpenseFormPage expenseFormPage = expensesPage.editExpense(originalDescription);
        expenseFormPage.verifyExpenseFormLoaded();
        
        // Clear and fill form with new data
        expenseFormPage.clearForm();
        expenseFormPage.fillExpenseForm(newDescription, newAmount, newCategory, 
            LocalDate.now().format(DATE_FORMAT), newNotes);
        
        // Save changes
        ExpensesPage updatedExpensesPage = expenseFormPage.saveExpense();
        
        ExtentReportListener.logPass("Expense edited successfully: " + newDescription);
        return updatedExpensesPage;
    }
    
    /**
     * Delete an expense with confirmation
     * @param expensesPage The expenses page instance
     * @param description The expense description to delete
     * @return ExpensesPage instance after deletion
     */
    public static ExpensesPage deleteExpenseWithConfirmation(ExpensesPage expensesPage, String description) {
        ExtentReportListener.logInfo("Deleting expense: " + description);
        
        // Verify expense exists before deletion
        AssertUtils.assertTrue(expensesPage.isExpensePresent(description), 
            "Expense should exist before deletion: " + description);
        
        // Delete expense
        expensesPage.deleteExpense(description);
        
        // Verify expense is deleted
        AssertUtils.assertFalse(expensesPage.isExpensePresent(description), 
            "Expense should be deleted: " + description);
        
        ExtentReportListener.logPass("Expense deleted successfully: " + description);
        return expensesPage;
    }
    
    /**
     * Create a new category with all properties
     * @param categoriesPage The categories page instance
     * @param name The category name
     * @param description The category description
     * @return CategoriesPage instance after creation
     */
    public static CategoriesPage createCompleteCategory(CategoriesPage categoriesPage, 
                                                       String name, String description) {
        ExtentReportListener.logInfo("Creating category: " + name);
        
        // Navigate to category form
        CategoryFormPage categoryFormPage = categoriesPage.clickAddCategory();
        categoryFormPage.verifyCategoryFormLoaded();
        
        // Fill category form
        categoryFormPage.fillCategoryForm(name, description);
        
        // Save category
        CategoriesPage updatedCategoriesPage = categoryFormPage.saveCategory();
        
        ExtentReportListener.logPass("Category created successfully: " + name);
        return updatedCategoriesPage;
    }
    
    /**
     * Create a test category with generated data
     * @param categoriesPage The categories page instance
     * @return CategoriesPage instance after creation
     */
    public static CategoriesPage createTestCategory(CategoriesPage categoriesPage) {
        String name = "Test Category " + System.currentTimeMillis();
        String description = "Test category description for " + name;
        
        return createCompleteCategory(categoriesPage, name, description);
    }
    
    /**
     * Edit an existing category
     * @param categoriesPage The categories page instance
     * @param originalName The original category name
     * @param newName The new category name
     * @param newDescription The new category description
     * @return CategoriesPage instance after editing
     */
    public static CategoriesPage editCategory(CategoriesPage categoriesPage, String originalName,
                                             String newName, String newDescription) {
        ExtentReportListener.logInfo("Editing category: " + originalName);
        
        // Edit category
        CategoryFormPage categoryFormPage = categoriesPage.editCategory(originalName);
        categoryFormPage.verifyCategoryFormLoaded();
        
        // Update form
        categoryFormPage.clearForm();
        categoryFormPage.fillCategoryForm(newName, newDescription);
        
        // Save changes
        CategoriesPage updatedCategoriesPage = categoryFormPage.saveCategory();
        
        ExtentReportListener.logPass("Category edited successfully: " + newName);
        return updatedCategoriesPage;
    }
    
    /**
     * Attempt to delete a category and verify behavior
     * @param categoriesPage The categories page instance
     * @param categoryName The category name to delete
     * @param shouldBePreventedIfHasExpenses Whether deletion should be prevented if category has expenses
     * @return CategoriesPage instance after deletion attempt
     */
    public static CategoriesPage deleteCategoryWithValidation(CategoriesPage categoriesPage, 
                                                             String categoryName, 
                                                             boolean shouldBePreventedIfHasExpenses) {
        ExtentReportListener.logInfo("Attempting to delete category: " + categoryName);
        
        // Check if category has expenses
        int expenseCount = categoriesPage.getCategoryExpenseCount(categoryName);
        boolean hasExpenses = expenseCount > 0;
        
        if (hasExpenses && shouldBePreventedIfHasExpenses) {
            ExtentReportListener.logInfo("Category has " + expenseCount + " expenses, deletion should be prevented");
            // Attempt deletion - should be prevented
            categoriesPage.deleteCategory(categoryName);
            // Verify category still exists
            AssertUtils.assertTrue(categoriesPage.isCategoryPresent(categoryName), 
                "Category with expenses should not be deleted: " + categoryName);
            ExtentReportListener.logPass("Category deletion correctly prevented: " + categoryName);
        } else {
            ExtentReportListener.logInfo("Category has no expenses, deletion should proceed");
            // Delete category
            categoriesPage.deleteCategory(categoryName);
            // Verify category is deleted
            AssertUtils.assertFalse(categoriesPage.isCategoryPresent(categoryName), 
                "Category should be deleted: " + categoryName);
            ExtentReportListener.logPass("Category deleted successfully: " + categoryName);
        }
        
        return categoriesPage;
    }
    
    /**
     * Verify dashboard statistics accuracy
     * @param homePage The home page instance
     * @param expectedExpenseCount Expected number of expenses
     * @param expectedCategoriesCount Expected number of categories
     */
    public static void verifyDashboardStatistics(HomePage homePage, int expectedExpenseCount, 
                                                int expectedCategoriesCount) {
        ExtentReportListener.logInfo("Verifying dashboard statistics");
        
        // Get dashboard statistics
        int actualExpenseCount = Integer.parseInt(homePage.getExpenseCount());
        int actualCategoriesCount = Integer.parseInt(homePage.getCategoriesCount());
        
        // Verify statistics
        AssertUtils.assertTrue(actualExpenseCount == expectedExpenseCount, 
            "Dashboard expense count should match expected: expected " + expectedExpenseCount + ", actual " + actualExpenseCount);
        AssertUtils.assertTrue(actualCategoriesCount == expectedCategoriesCount, 
            "Dashboard categories count should match expected: expected " + expectedCategoriesCount + ", actual " + actualCategoriesCount);
        
        // Verify statistics are properly formatted
        homePage.verifyStatisticsAreNumeric();
        
        ExtentReportListener.logPass("Dashboard statistics verification successful");
        ExtentReportListener.logInfo("Expenses: " + actualExpenseCount + ", Categories: " + actualCategoriesCount);
    }
    
    /**
     * Test search and filter functionality on expenses page
     * @param expensesPage The expenses page instance
     * @param searchTerm The term to search for
     * @param category The category to filter by (optional)
     * @param fromDate The start date for date filter (optional)
     * @param toDate The end date for date filter (optional)
     * @return ExpensesPage instance after filtering
     */
    public static ExpensesPage testSearchAndFilters(ExpensesPage expensesPage, String searchTerm,
                                                   String category, String fromDate, String toDate) {
        ExtentReportListener.logInfo("Testing search and filters");
        
        // Get initial expense count
        int initialCount = expensesPage.getExpenseCount();
        ExtentReportListener.logInfo("Initial expense count: " + initialCount);
        
        // Apply search if provided
        if (searchTerm != null && !searchTerm.isEmpty()) {
            expensesPage.searchExpenses(searchTerm);
            ExtentReportListener.logInfo("Applied search filter: " + searchTerm);
        }
        
        // Apply category filter if provided
        if (category != null && !category.isEmpty()) {
            expensesPage.filterByCategory(category);
            ExtentReportListener.logInfo("Applied category filter: " + category);
        }
        
        // Apply date range filter if provided
        if (fromDate != null && toDate != null) {
            expensesPage.filterByDateRange(fromDate, toDate);
            ExtentReportListener.logInfo("Applied date filter: " + fromDate + " to " + toDate);
        }
        
        // Verify filtering results
        int filteredCount = expensesPage.getExpenseCount();
        ExtentReportListener.logInfo("Filtered expense count: " + filteredCount);
        
        // Clear all filters
        expensesPage.clearFilters();
        int finalCount = expensesPage.getExpenseCount();
        
        // Verify filters were cleared
        AssertUtils.assertTrue(finalCount == initialCount, 
            "Expense count should return to initial after clearing filters: expected " + initialCount + ", actual " + finalCount);
        
        ExtentReportListener.logPass("Search and filter functionality verified");
        return expensesPage;
    }
    
    /**
     * Test pagination functionality
     * @param expensesPage The expenses page instance
     * @return ExpensesPage instance after pagination test
     */
    public static ExpensesPage testPagination(ExpensesPage expensesPage) {
        ExtentReportListener.logInfo("Testing pagination functionality");
        
        if (expensesPage.hasPagination()) {
            ExtentReportListener.logInfo("Pagination is available");
            
            // Test next page navigation
            expensesPage.goToNextPage();
            ExtentReportListener.logPass("Successfully navigated to next page");
            
            // Test previous page navigation
            expensesPage.goToPreviousPage();
            ExtentReportListener.logPass("Successfully navigated to previous page");
        } else {
            ExtentReportListener.logInfo("No pagination available - not enough data");
        }
        
        return expensesPage;
    }
    
    /**
     * Set browser window size for responsive testing
     * @param driver The WebDriver instance
     * @param width Window width
     * @param height Window height
     */
    public static void setWindowSize(WebDriver driver, int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
        ExtentReportListener.logInfo("Set window size to: " + width + "x" + height);
    }
    
    /**
     * Test responsive behavior at different screen sizes
     * @param driver The WebDriver instance
     * @param homePage The home page instance
     */
    public static void testResponsiveBehavior(WebDriver driver, HomePage homePage) {
        ExtentReportListener.logInfo("Testing responsive behavior");
        
        // Test desktop view
        setWindowSize(driver, 1920, 1080);
        homePage.verifyDashboardLoaded();
        ExtentReportListener.logPass("Desktop view verified");
        
        // Test tablet view
        setWindowSize(driver, 768, 1024);
        homePage.verifyDashboardLoaded();
        ExtentReportListener.logPass("Tablet view verified");
        
        // Test mobile view
        setWindowSize(driver, 375, 667);
        homePage.verifyDashboardLoaded();
        ExtentReportListener.logPass("Mobile view verified");
        
        // Reset to default size
        driver.manage().window().maximize();
        ExtentReportListener.logInfo("Reset to maximized window");
    }
    
    /**
     * Generate a random future date for testing
     * @return Formatted date string (YYYY-MM-DD)
     */
    public static String generateFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(random.nextInt(365) + 1);
        return futureDate.format(DATE_FORMAT);
    }
    
    /**
     * Generate a random past date for testing
     * @return Formatted date string (YYYY-MM-DD)
     */
    public static String generatePastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(random.nextInt(365) + 1);
        return pastDate.format(DATE_FORMAT);
    }
    
    /**
     * Wait for dashboard to update after changes
     * @param homePage The home page instance
     */
    public static void waitForDashboardUpdate(HomePage homePage) {
        ExtentReportListener.logInfo("Waiting for dashboard update");
        // Small wait to ensure dashboard statistics update
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        homePage.verifyDashboardLoaded();
        ExtentReportListener.logInfo("Dashboard update completed");
    }
    
    /**
     * Verify chart elements are present on dashboard
     * @param homePage The home page instance
     */
    public static void verifyDashboardCharts(HomePage homePage) {
        ExtentReportListener.logInfo("Verifying dashboard charts");
        
        // Verify expense chart is present
        boolean hasExpenseChart = homePage.hasExpenseChart();
        AssertUtils.assertTrue(hasExpenseChart, "Dashboard should have expense chart");
        
        // Verify recent expenses section
        boolean hasRecentExpenses = homePage.hasRecentExpenses();
        ExtentReportListener.logInfo("Recent expenses section present: " + hasRecentExpenses);
        
        ExtentReportListener.logPass("Dashboard charts verification completed");
    }
}