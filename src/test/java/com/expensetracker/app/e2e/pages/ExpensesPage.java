package com.expensetracker.app.e2e.pages;

import com.expensetracker.app.e2e.config.WebDriverConfig;
import com.expensetracker.app.e2e.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Page Object for the Expenses List page
 * Contains locators and methods for expense management functionality
 */
public class ExpensesPage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpensesPage.class);
    
    // Page-specific locators
    private static final By EXPENSES_TITLE = By.tagName("h1");
    private static final By ADD_EXPENSE_BUTTON = By.id("addExpenseBtn");
    private static final By EXPENSES_TABLE = By.id("expensesTable");
    private static final By TABLE_ROWS = By.cssSelector("#expensesTable tbody tr");
    private static final By NO_EXPENSES_MESSAGE = By.id("noExpensesMessage");
    private static final By SEARCH_INPUT = By.id("searchInput");
    private static final By CATEGORY_FILTER = By.id("categoryFilter");
    private static final By DATE_FROM_FILTER = By.id("dateFromFilter");
    private static final By DATE_TO_FILTER = By.id("dateToFilter");
    private static final By APPLY_FILTERS_BUTTON = By.id("applyFiltersBtn");
    private static final By CLEAR_FILTERS_BUTTON = By.id("clearFiltersBtn");
    private static final By TOTAL_AMOUNT_DISPLAY = By.id("totalAmountDisplay");
    private static final By PAGINATION = By.cssSelector(".pagination");
    
    // Table column locators
    private static final By DESCRIPTION_COLUMN = By.cssSelector("td:nth-child(1)");
    private static final By AMOUNT_COLUMN = By.cssSelector("td:nth-child(2)");
    private static final By CATEGORY_COLUMN = By.cssSelector("td:nth-child(3)");
    private static final By DATE_COLUMN = By.cssSelector("td:nth-child(4)");
    private static final By ACTIONS_COLUMN = By.cssSelector("td:nth-child(5)");
    
    // Action buttons in table rows
    private static final String EDIT_BUTTON_XPATH = "//tr[contains(., '%s')]//a[contains(@class, 'btn-primary')]";
    private static final String DELETE_BUTTON_XPATH = "//tr[contains(., '%s')]//button[contains(@class, 'btn-danger')]";
    
    // Page Factory elements
    @FindBy(id = "addExpenseBtn")
    private WebElement addExpenseButton;
    
    @FindBy(id = "searchInput")
    private WebElement searchInput;
    
    @FindBy(id = "categoryFilter")
    private WebElement categoryFilter;
    
    @FindBy(id = "applyFiltersBtn")
    private WebElement applyFiltersButton;
    
    @FindBy(id = "clearFiltersBtn")
    private WebElement clearFiltersButton;
    
    public ExpensesPage() {
        super();
        logger.info("ExpensesPage initialized");
    }
    
    @Override
    public String getExpectedPageTitle() {
        return "Expenses - Expense Tracker";  // Match actual page title
    }
    
    @Override
    public String getExpectedUrlFragment() {
        return "/expenses";
    }
    
    /**
     * Verify expenses page is loaded
     */
    public void verifyExpensesPageLoaded() {
        logger.info("Verifying expenses page is loaded");
        verifyPage();
        waitForPageLoad();
        
        // Verify page title
        String title = getText(EXPENSES_TITLE);
        logger.info("Expenses page title: {}", title);
        
        // Verify Add Expense button is visible
        if (isVisible(ADD_EXPENSE_BUTTON)) {
            logger.info("Add Expense button is visible");
        }
        
        logger.info("Expenses page verification completed");
    }
    
    /**
     * Click Add Expense button to navigate to expense form
     */
    public ExpenseFormPage clickAddExpense() {
        logger.info("Clicking Add Expense button");
        click(ADD_EXPENSE_BUTTON);
        return new ExpenseFormPage();
    }
    
    /**
     * Get the number of expenses currently displayed
     */
    public int getExpenseCount() {
        if (isVisible(EXPENSES_TABLE)) {
            List<WebElement> rows = WebDriverConfig.getDriver().findElements(TABLE_ROWS);
            logger.info("Found {} expenses in the table", rows.size());
            return rows.size();
        }
        logger.info("No expenses table found, returning 0");
        return 0;
    }
    
    /**
     * Check if expenses table is visible
     */
    public boolean hasExpensesTable() {
        return isVisible(EXPENSES_TABLE);
    }
    
    /**
     * Check if no expenses message is displayed
     */
    public boolean hasNoExpensesMessage() {
        return isVisible(NO_EXPENSES_MESSAGE);
    }
    
    /**
     * Get no expenses message text
     */
    public String getNoExpensesMessage() {
        if (hasNoExpensesMessage()) {
            return getText(NO_EXPENSES_MESSAGE);
        }
        return "";
    }
    
    /**
     * Search for expenses by description
     */
    public void searchExpenses(String searchTerm) {
        logger.info("Searching for expenses with term: {}", searchTerm);
        type(SEARCH_INPUT, searchTerm);
        click(APPLY_FILTERS_BUTTON);
        waitForPageLoad();
        logger.info("Search completed for term: {}", searchTerm);
    }
    
    /**
     * Filter expenses by category
     */
    public void filterByCategory(String category) {
        logger.info("Filtering expenses by category: {}", category);
        selectByText(CATEGORY_FILTER, category);
        click(APPLY_FILTERS_BUTTON);
        waitForPageLoad();
        logger.info("Category filter applied: {}", category);
    }
    
    /**
     * Filter expenses by date range
     */
    public void filterByDateRange(String fromDate, String toDate) {
        logger.info("Filtering expenses by date range: {} to {}", fromDate, toDate);
        type(DATE_FROM_FILTER, fromDate);
        type(DATE_TO_FILTER, toDate);
        click(APPLY_FILTERS_BUTTON);
        waitForPageLoad();
        logger.info("Date range filter applied: {} to {}", fromDate, toDate);
    }
    
    /**
     * Clear all filters
     */
    public void clearFilters() {
        logger.info("Clearing all filters");
        click(CLEAR_FILTERS_BUTTON);
        waitForPageLoad();
        logger.info("All filters cleared");
    }
    
    /**
     * Get total amount displayed
     */
    public String getTotalAmount() {
        if (isVisible(TOTAL_AMOUNT_DISPLAY)) {
            return getText(TOTAL_AMOUNT_DISPLAY);
        }
        return "0.00";
    }
    
    /**
     * Edit expense by description
     */
    public ExpenseFormPage editExpense(String description) {
        logger.info("Editing expense with description: {}", description);
        By editButton = By.xpath(String.format(EDIT_BUTTON_XPATH, description));
        WaitUtils.waitForElementClickable(editButton);
        click(editButton);
        return new ExpenseFormPage();
    }
    
    /**
     * Delete expense by description
     */
    public void deleteExpense(String description) {
        logger.info("Deleting expense with description: {}", description);
        By deleteButton = By.xpath(String.format(DELETE_BUTTON_XPATH, description));
        WaitUtils.waitForElementClickable(deleteButton);
        click(deleteButton);
        
        // Handle confirmation dialog if present
        handleDeleteConfirmation();
        waitForPageLoad();
        logger.info("Expense deleted: {}", description);
    }
    
    /**
     * Handle delete confirmation dialog
     */
    private void handleDeleteConfirmation() {
        try {
            // Wait for confirmation dialog
            By confirmButton = By.id("confirmDeleteBtn");
            if (WaitUtils.isElementVisible(confirmButton)) {
                click(confirmButton);
                logger.debug("Delete confirmation handled");
            }
        } catch (Exception e) {
            logger.debug("No delete confirmation dialog found");
        }
    }
    
    /**
     * Verify expense exists in the table
     */
    public boolean isExpensePresent(String description) {
        if (!hasExpensesTable()) {
            return false;
        }
        
        By expenseRow = By.xpath(String.format("//tr[contains(., '%s')]", description));
        return isVisible(expenseRow);
    }
    
    /**
     * Get expense details by description
     */
    public ExpenseDetails getExpenseDetails(String description) {
        logger.debug("Getting expense details for: {}", description);
        
        if (!isExpensePresent(description)) {
            throw new RuntimeException("Expense not found: " + description);
        }
        
        By expenseRow = By.xpath(String.format("//tr[contains(., '%s')]", description));
        WebElement row = WebDriverConfig.getDriver().findElement(expenseRow);
        
        String desc = row.findElement(DESCRIPTION_COLUMN).getText();
        String amount = row.findElement(AMOUNT_COLUMN).getText();
        String category = row.findElement(CATEGORY_COLUMN).getText();
        String date = row.findElement(DATE_COLUMN).getText();
        
        return new ExpenseDetails(desc, amount, category, date);
    }
    
    /**
     * Get all expense descriptions currently visible
     */
    public List<String> getAllExpenseDescriptions() {
        List<WebElement> rows = WebDriverConfig.getDriver().findElements(TABLE_ROWS);
        return rows.stream()
            .map(row -> row.findElement(DESCRIPTION_COLUMN).getText())
            .toList();
    }
    
    /**
     * Check if pagination is present
     */
    public boolean hasPagination() {
        return isVisible(PAGINATION);
    }
    
    /**
     * Navigate to next page if pagination exists
     */
    public void goToNextPage() {
        if (hasPagination()) {
            By nextButton = By.cssSelector(".pagination .page-item:last-child .page-link");
            if (isVisible(nextButton) && !getAttribute(nextButton, "class").contains("disabled")) {
                click(nextButton);
                waitForPageLoad();
                logger.info("Navigated to next page");
            }
        }
    }
    
    /**
     * Navigate to previous page if pagination exists
     */
    public void goToPreviousPage() {
        if (hasPagination()) {
            By prevButton = By.cssSelector(".pagination .page-item:first-child .page-link");
            if (isVisible(prevButton) && !getAttribute(prevButton, "class").contains("disabled")) {
                click(prevButton);
                waitForPageLoad();
                logger.info("Navigated to previous page");
            }
        }
    }
    
    /**
     * Inner class for expense details
     */
    public static class ExpenseDetails {
        private final String description;
        private final String amount;
        private final String category;
        private final String date;
        
        public ExpenseDetails(String description, String amount, String category, String date) {
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.date = date;
        }
        
        public String getDescription() { return description; }
        public String getAmount() { return amount; }
        public String getCategory() { return category; }
        public String getDate() { return date; }
        
        @Override
        public String toString() {
            return String.format("ExpenseDetails{description='%s', amount='%s', category='%s', date='%s'}", 
                description, amount, category, date);
        }
    }
}