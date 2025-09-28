package com.expensetracker.app.e2e.tests.journey;

import com.expensetracker.app.e2e.base.BaseTest;
import com.expensetracker.app.e2e.listeners.ExtentReportListener;
import com.expensetracker.app.e2e.listeners.RetryAnalyzer;
import com.expensetracker.app.e2e.pages.*;
import com.expensetracker.app.e2e.tests.common.TestCommonUtils;
import com.expensetracker.app.e2e.utils.AssertUtils;
import org.testng.annotations.Test;

/**
 * Comprehensive category management journey tests
 * Tests complete category workflows including creation, editing, and deletion
 */
public class CategoryManagementJourneyTest extends BaseTest {
    
    @Test(groups = {"journey", "category", "crud"}, 
          description = "Create new category with custom styling",
          retryAnalyzer = RetryAnalyzer.class)
    public void testCreateCategoryWithCustomStyling() {
        ExtentReportListener.logInfo("Starting create category with custom styling test");
        
        // Navigate to categories page
        driver.get(config.getBaseUrl() + "/categories");
        CategoriesPage categoriesPage = new CategoriesPage();
        categoriesPage.verifyCategoriesPageLoaded();
        
        // Get initial category count
        int initialCount = categoriesPage.getCategoryCount();
        ExtentReportListener.logInfo("Initial category count: " + initialCount);
        
        // Create new category with custom styling
        String categoryName = "Custom Category " + System.currentTimeMillis();
        String categoryDescription = "Test category with custom styling and properties";
        
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, categoryName, categoryDescription);
        
        // Verify category was created
        int updatedCount = categoriesPage.getCategoryCount();
        AssertUtils.assertGreaterThan(updatedCount, initialCount, 
            "Category count should increase after creating category");
        
        // Verify category exists in the list
        AssertUtils.assertTrue(categoriesPage.isCategoryPresent(categoryName), 
            "Created category should be visible in categories list");
        
        // Verify category details
        CategoriesPage.CategoryDetails categoryDetails = categoriesPage.getCategoryDetails(categoryName);
        AssertUtils.assertTrue(categoryDetails.getName().equals(categoryName), 
            "Category name should match: " + categoryName);
        AssertUtils.assertTrue(categoryDetails.getDescription().contains(categoryDescription), 
            "Category description should match: " + categoryDescription);
        
        ExtentReportListener.logPass("Category creation with custom styling completed successfully");
    }
    
    @Test(groups = {"journey", "category", "edit"}, 
          description = "Edit category properties",
          retryAnalyzer = RetryAnalyzer.class)
    public void testEditCategoryProperties() {
        ExtentReportListener.logInfo("Starting edit category properties test");
        
        // Setup: Create a category first
        driver.get(config.getBaseUrl() + "/categories");
        CategoriesPage categoriesPage = new CategoriesPage();
        categoriesPage.verifyCategoriesPageLoaded();
        
        String originalName = "Original Category " + System.currentTimeMillis();
        String originalDescription = "Original description";
        
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, originalName, originalDescription);
        
        // Edit the category
        String newName = "Updated Category " + System.currentTimeMillis();
        String newDescription = "Updated description with new properties";
        
        categoriesPage = TestCommonUtils.editCategory(categoriesPage, originalName, newName, newDescription);
        
        // Verify old category is gone
        AssertUtils.assertFalse(categoriesPage.isCategoryPresent(originalName), 
            "Original category should no longer exist after edit");
        
        // Verify new category exists with updated details
        AssertUtils.assertTrue(categoriesPage.isCategoryPresent(newName), 
            "Updated category should exist after edit");
        
        CategoriesPage.CategoryDetails updatedDetails = categoriesPage.getCategoryDetails(newName);
        AssertUtils.assertTrue(updatedDetails.getName().equals(newName), 
            "Updated category name should match: " + newName);
        AssertUtils.assertTrue(updatedDetails.getDescription().contains(newDescription), 
            "Updated category description should match: " + newDescription);
        
        ExtentReportListener.logPass("Category edit completed successfully");
    }
    
    @Test(groups = {"journey", "category", "delete"}, 
          description = "Delete category - should prevent if has expenses",
          retryAnalyzer = RetryAnalyzer.class)
    public void testDeleteCategoryWithExpenseValidation() {
        ExtentReportListener.logInfo("Starting delete category with expense validation test");
        
        // Setup: Create a category first
        driver.get(config.getBaseUrl() + "/categories");
        CategoriesPage categoriesPage = new CategoriesPage();
        categoriesPage.verifyCategoriesPageLoaded();
        
        String testCategoryName = "Test Delete Category " + System.currentTimeMillis();
        categoriesPage = TestCommonUtils.createTestCategory(categoriesPage);
        
        // Create a fresh category for deletion test
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, testCategoryName, 
            "Category for deletion testing");
        
        // Navigate to home to create an expense with this category
        HomePage homePage = categoriesPage.navigateToHome();
        homePage.verifyDashboardLoaded();
        
        // Create an expense using the test category
        homePage = TestCommonUtils.createCompleteExpense(homePage, 
            "Expense for category test " + System.currentTimeMillis(),
            "50.00", testCategoryName, TestCommonUtils.generatePastDate(), "Test expense");
        
        // Navigate back to categories
        categoriesPage = homePage.navigateToCategories();
        categoriesPage.verifyCategoriesPageLoaded();
        
        // Test deletion - should be prevented because category has expenses
        categoriesPage = TestCommonUtils.deleteCategoryWithValidation(categoriesPage, testCategoryName, true);
        
        ExtentReportListener.logPass("Category deletion validation test completed successfully");
        
        // Test deleting category without expenses
        String emptyCategoryName = "Empty Category " + System.currentTimeMillis();
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, emptyCategoryName, 
            "Empty category for deletion");
        
        // This deletion should succeed
        categoriesPage = TestCommonUtils.deleteCategoryWithValidation(categoriesPage, emptyCategoryName, false);
        
        ExtentReportListener.logPass("Empty category deletion test completed successfully");
    }
    
    @Test(groups = {"journey", "category", "statistics"}, 
          description = "Verify category usage statistics",
          retryAnalyzer = RetryAnalyzer.class)
    public void testCategoryUsageStatistics() {
        ExtentReportListener.logInfo("Starting category usage statistics test");
        
        // Navigate to categories page
        driver.get(config.getBaseUrl() + "/categories");
        CategoriesPage categoriesPage = new CategoriesPage();
        categoriesPage.verifyCategoriesPageLoaded();
        
        // Create a test category
        String statisticsTestCategory = "Statistics Test Category " + System.currentTimeMillis();
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, statisticsTestCategory, 
            "Category for testing usage statistics");
        
        // Get initial expense count for the category (should be 0)
        int initialExpenseCount = categoriesPage.getCategoryExpenseCount(statisticsTestCategory);
        AssertUtils.assertTrue(initialExpenseCount == 0, 
            "New category should have 0 expenses initially");
        ExtentReportListener.logInfo("Initial expense count for category: " + initialExpenseCount);
        
        // Navigate to home and create multiple expenses with this category
        HomePage homePage = categoriesPage.navigateToHome();
        homePage.verifyDashboardLoaded();
        
        // Create 3 expenses with the test category
        for (int i = 1; i <= 3; i++) {
            homePage = TestCommonUtils.createCompleteExpense(homePage,
                "Statistics Expense " + i + " " + System.currentTimeMillis(),
                String.valueOf(25 * i), statisticsTestCategory,
                TestCommonUtils.generatePastDate(), "Statistics test expense " + i);
        }
        
        // Navigate back to categories and verify updated statistics
        categoriesPage = homePage.navigateToCategories();
        categoriesPage.verifyCategoriesPageLoaded();
        
        // Verify the category now shows 3 expenses
        int updatedExpenseCount = categoriesPage.getCategoryExpenseCount(statisticsTestCategory);
        AssertUtils.assertTrue(updatedExpenseCount == 3, 
            "Category should show 3 expenses after creating 3 expenses, but found: " + updatedExpenseCount);
        
        ExtentReportListener.logInfo("Updated expense count for category: " + updatedExpenseCount);
        
        // Verify categories are sorted properly
        boolean categoriesSorted = categoriesPage.areCategoriesSorted();
        AssertUtils.assertTrue(categoriesSorted, "Categories should be sorted alphabetically");
        
        // Verify system categories count
        int systemCategoriesCount = categoriesPage.getSystemCategoriesCount();
        AssertUtils.assertGreaterThan(systemCategoriesCount, 0, 
            "System should have default categories");
        ExtentReportListener.logInfo("System categories count: " + systemCategoriesCount);
        
        ExtentReportListener.logPass("Category usage statistics verification completed successfully");
    }
    
    @Test(groups = {"journey", "category", "search"}, 
          description = "Test category search and filtering functionality",
          retryAnalyzer = RetryAnalyzer.class)
    public void testCategorySearchAndFilter() {
        ExtentReportListener.logInfo("Starting category search and filter test");
        
        // Navigate to categories page
        driver.get(config.getBaseUrl() + "/categories");
        CategoriesPage categoriesPage = new CategoriesPage();
        categoriesPage.verifyCategoriesPageLoaded();
        
        // Create multiple test categories with different names
        String searchCategory1 = "Search Test Category A " + System.currentTimeMillis();
        String searchCategory2 = "Search Test Category B " + System.currentTimeMillis();
        String filterCategory = "Filter Test Different " + System.currentTimeMillis();
        
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, searchCategory1, 
            "First search test category");
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, searchCategory2, 
            "Second search test category");
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, filterCategory, 
            "Different category for filter testing");
        
        // Get initial category count
        int initialCount = categoriesPage.getCategoryCount();
        
        // Test search functionality
        ExtentReportListener.logInfo("Testing category search functionality");
        categoriesPage.searchCategories("Search Test");
        
        // Verify search results - should show categories with "Search Test" in name
        AssertUtils.assertTrue(categoriesPage.isCategoryPresent(searchCategory1), 
            "Search should show first search test category");
        AssertUtils.assertTrue(categoriesPage.isCategoryPresent(searchCategory2), 
            "Search should show second search test category");
        
        // Clear search
        categoriesPage.clearSearch();
        
        // Verify all categories are shown again
        int finalCount = categoriesPage.getCategoryCount();
        AssertUtils.assertTrue(finalCount == initialCount, 
            "Category count should return to initial after clearing search");
        
        ExtentReportListener.logPass("Category search and filter test completed successfully");
    }
    
    @Test(groups = {"journey", "category", "workflow"}, 
          description = "Complete category management workflow",
          retryAnalyzer = RetryAnalyzer.class)
    public void testCompleteCategoryManagementWorkflow() {
        ExtentReportListener.logInfo("Starting complete category management workflow test");
        
        // Navigate to categories page
        driver.get(config.getBaseUrl() + "/categories");
        CategoriesPage categoriesPage = new CategoriesPage();
        categoriesPage.verifyCategoriesPageLoaded();
        
        // Record initial state
        int initialCount = categoriesPage.getCategoryCount();
        int initialSystemCount = categoriesPage.getSystemCategoriesCount();
        
        ExtentReportListener.logInfo("Initial state - Categories: " + initialCount + 
            ", System Categories: " + initialSystemCount);
        
        // 1. Create multiple categories
        String workflowCategory1 = "Workflow Category 1 " + System.currentTimeMillis();
        String workflowCategory2 = "Workflow Category 2 " + System.currentTimeMillis();
        
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, workflowCategory1, 
            "First workflow category");
        categoriesPage = TestCommonUtils.createCompleteCategory(categoriesPage, workflowCategory2, 
            "Second workflow category");
        
        // 2. Verify categories were created
        int afterCreationCount = categoriesPage.getCategoryCount();
        AssertUtils.assertTrue(afterCreationCount == initialCount + 2, 
            "Should have 2 more categories after creation");
        
        // 3. Use categories in expenses (navigate to home and create expenses)
        HomePage homePage = categoriesPage.navigateToHome();
        homePage = TestCommonUtils.createCompleteExpense(homePage,
            "Workflow Expense 1", "75.00", workflowCategory1, 
            TestCommonUtils.generatePastDate(), "First workflow expense");
        
        homePage = TestCommonUtils.createCompleteExpense(homePage,
            "Workflow Expense 2", "125.00", workflowCategory2, 
            TestCommonUtils.generatePastDate(), "Second workflow expense");
        
        // 4. Return to categories and verify usage statistics
        categoriesPage = homePage.navigateToCategories();
        categoriesPage.verifyCategoriesPageLoaded();
        
        int category1ExpenseCount = categoriesPage.getCategoryExpenseCount(workflowCategory1);
        int category2ExpenseCount = categoriesPage.getCategoryExpenseCount(workflowCategory2);
        
        AssertUtils.assertTrue(category1ExpenseCount >= 1, 
            "First category should have at least 1 expense");
        AssertUtils.assertTrue(category2ExpenseCount >= 1, 
            "Second category should have at least 1 expense");
        
        // 5. Edit one of the categories
        String editedCategoryName = "Edited " + workflowCategory1;
        categoriesPage = TestCommonUtils.editCategory(categoriesPage, workflowCategory1, 
            editedCategoryName, "Updated workflow category description");
        
        // 6. Verify editing worked
        AssertUtils.assertTrue(categoriesPage.isCategoryPresent(editedCategoryName), 
            "Edited category should exist with new name");
        
        ExtentReportListener.logPass("Complete category management workflow completed successfully");
    }
}