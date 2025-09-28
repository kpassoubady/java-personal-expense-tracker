package com.expensetracker.app.e2e.tests.journey;

import com.expensetracker.app.e2e.base.BaseTest;
import com.expensetracker.app.e2e.listeners.ExtentReportListener;
import com.expensetracker.app.e2e.listeners.RetryAnalyzer;
import com.expensetracker.app.e2e.pages.*;
import com.expensetracker.app.e2e.tests.common.TestCommonUtils;
import com.expensetracker.app.e2e.utils.AssertUtils;
import org.testng.annotations.Test;

/**
 * Comprehensive expense management journey tests
 * Tests complete expense workflows from creation to deletion
 */
public class ExpenseManagementJourneyTest extends BaseTest {
    
    @Test(groups = {"journey", "expense", "crud"}, 
          description = "Complete expense creation workflow with dashboard verification",
          retryAnalyzer = RetryAnalyzer.class)
    public void testCompleteExpenseCreationWorkflow() {
        ExtentReportListener.logInfo("Starting complete expense creation workflow test");
        
        // Navigate to home page and verify initial state
        driver.get(config.getBaseUrl());
        HomePage homePage = new HomePage();
        homePage.verifyDashboardLoaded();
        ExtentReportListener.logInfo("Dashboard loaded successfully");
        
        // Get initial dashboard statistics
        int initialExpenseCount = Integer.parseInt(homePage.getExpenseCount());
        String initialTotalExpenses = homePage.getTotalExpenses();
        ExtentReportListener.logInfo("Initial state - Expenses: " + initialExpenseCount + 
            ", Total: " + initialTotalExpenses);
        
        // Create a complete expense
        String testDescription = "Journey Test Expense " + System.currentTimeMillis();
        String testAmount = "75.50";
        String testCategory = "Food";
        String testDate = TestCommonUtils.generatePastDate();
        String testNotes = "Test expense for journey validation";
        
        homePage = TestCommonUtils.createCompleteExpense(homePage, testDescription, 
            testAmount, testCategory, testDate, testNotes);
        
        // Wait for dashboard update
        TestCommonUtils.waitForDashboardUpdate(homePage);
        
        // Verify dashboard statistics updated
        int updatedExpenseCount = Integer.parseInt(homePage.getExpenseCount());
        AssertUtils.assertGreaterThan(updatedExpenseCount, initialExpenseCount, 
            "Expense count should increase after adding expense");
        ExtentReportListener.logPass("Dashboard statistics updated correctly after expense creation");
        
        // Navigate to expenses page and verify expense exists
        ExpensesPage expensesPage = homePage.navigateToExpenses();
        expensesPage.verifyExpensesPageLoaded();
        
        AssertUtils.assertTrue(expensesPage.isExpensePresent(testDescription), 
            "Created expense should be visible in expenses list");
        ExtentReportListener.logPass("Expense creation workflow completed successfully");
        
        // Verify expense details
        ExpensesPage.ExpenseDetails expenseDetails = expensesPage.getExpenseDetails(testDescription);
        AssertUtils.assertTrue(expenseDetails.getAmount().contains(testAmount), 
            "Expense amount should match: " + testAmount);
        AssertUtils.assertTrue(expenseDetails.getCategory().contains(testCategory), 
            "Expense category should match: " + testCategory);
        ExtentReportListener.logPass("Expense details verification successful");
    }
    
    @Test(groups = {"journey", "expense", "edit"}, 
          description = "Edit existing expense with validation",
          retryAnalyzer = RetryAnalyzer.class)
    public void testEditExpenseWithValidation() {
        ExtentReportListener.logInfo("Starting edit expense with validation test");
        
        // Setup: Create an expense first
        driver.get(config.getBaseUrl());
        HomePage homePage = new HomePage();
        homePage.verifyDashboardLoaded();
        
        String originalDescription = "Original Expense " + System.currentTimeMillis();
        String originalAmount = "100.00";
        String originalCategory = "Food";
        String originalNotes = "Original notes";
        
        homePage = TestCommonUtils.createCompleteExpense(homePage, originalDescription, 
            originalAmount, originalCategory, TestCommonUtils.generatePastDate(), originalNotes);
        
        // Navigate to expenses page
        ExpensesPage expensesPage = homePage.navigateToExpenses();
        expensesPage.verifyExpensesPageLoaded();
        
        // Edit the expense
        String newDescription = "Updated Expense " + System.currentTimeMillis();
        String newAmount = "150.75";
        String newCategory = "Transportation";
        String newNotes = "Updated notes for testing";
        
        expensesPage = TestCommonUtils.editExpense(expensesPage, originalDescription,
            newDescription, newAmount, newCategory, newNotes);
        
        // Verify old expense is gone
        AssertUtils.assertFalse(expensesPage.isExpensePresent(originalDescription), 
            "Original expense should no longer exist after edit");
        
        // Verify new expense exists with updated details
        AssertUtils.assertTrue(expensesPage.isExpensePresent(newDescription), 
            "Updated expense should exist after edit");
        
        ExpensesPage.ExpenseDetails updatedDetails = expensesPage.getExpenseDetails(newDescription);
        AssertUtils.assertTrue(updatedDetails.getAmount().contains(newAmount), 
            "Updated amount should match: " + newAmount);
        AssertUtils.assertTrue(updatedDetails.getCategory().contains(newCategory), 
            "Updated category should match: " + newCategory);
        
        ExtentReportListener.logPass("Expense edit with validation completed successfully");
    }
    
    @Test(groups = {"journey", "expense", "delete"}, 
          description = "Delete expense with confirmation",
          retryAnalyzer = RetryAnalyzer.class)
    public void testDeleteExpenseWithConfirmation() {
        ExtentReportListener.logInfo("Starting delete expense with confirmation test");
        
        // Setup: Create an expense to delete
        driver.get(config.getBaseUrl());
        HomePage homePage = new HomePage();
        homePage.verifyDashboardLoaded();
        
        String expenseToDelete = "Delete Test Expense " + System.currentTimeMillis();
        
        // Create specific expense for deletion
        homePage = TestCommonUtils.createCompleteExpense(homePage, expenseToDelete, 
            "25.00", "Other", TestCommonUtils.generatePastDate(), "Expense for deletion test");
        
        // Get initial count
        int initialCount = Integer.parseInt(homePage.getExpenseCount());
        
        // Navigate to expenses and delete
        ExpensesPage expensesPage = homePage.navigateToExpenses();
        expensesPage.verifyExpensesPageLoaded();
        
        expensesPage = TestCommonUtils.deleteExpenseWithConfirmation(expensesPage, expenseToDelete);
        
        // Navigate back to dashboard and verify count decreased
        homePage = expensesPage.navigateToHome();
        TestCommonUtils.waitForDashboardUpdate(homePage);
        
        int updatedCount = Integer.parseInt(homePage.getExpenseCount());
        AssertUtils.assertLessThan(updatedCount, initialCount, 
            "Expense count should decrease after deletion");
        
        ExtentReportListener.logPass("Expense deletion with confirmation completed successfully");
    }
}