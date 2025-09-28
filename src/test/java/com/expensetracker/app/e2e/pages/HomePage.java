package com.expensetracker.app.e2e.pages;

import com.expensetracker.app.e2e.config.WebDriverConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for the Home/Dashboard page
 * Contains locators and methods for dashboard functionality
 */
public class HomePage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(HomePage.class);
    
    // Page-specific locators
    private static final By DASHBOARD_TITLE = By.tagName("h1");
    private static final By TOTAL_EXPENSES_CARD = By.id("totalExpensesCard");
    private static final By EXPENSE_COUNT_CARD = By.id("expenseCountCard");
    private static final By CATEGORIES_COUNT_CARD = By.id("categoriesCountCard");
    private static final By RECENT_EXPENSES_TABLE = By.id("recentExpensesTable");
    private static final By NO_EXPENSES_MESSAGE = By.id("noExpensesMessage");
    protected static final By ADD_EXPENSE_BUTTON = By.id("addExpenseBtn");
    protected static final By VIEW_ALL_EXPENSES_BUTTON = By.id("viewAllExpensesBtn");
    private static final By EXPENSE_CHART = By.id("expenseChart");
    
    // Page Factory elements
    @FindBy(tagName = "h1")
    private WebElement dashboardTitle;
    
    @FindBy(id = "totalExpensesCard")
    private WebElement totalExpensesCard;
    
    @FindBy(id = "expenseCountCard")
    private WebElement expenseCountCard;
    
    @FindBy(id = "categoriesCountCard")
    private WebElement categoriesCountCard;
    
    @FindBy(id = "addExpenseBtn")
    private WebElement addExpenseButton;
    
    @FindBy(id = "viewAllExpensesBtn")
    private WebElement viewAllExpensesButton;
    
    public HomePage() {
        super();
        logger.info("HomePage initialized");
    }
    
    @Override
    public String getExpectedPageTitle() {
        return "Expense Tracker - Dashboard";
    }
    
    @Override
    public String getExpectedUrlFragment() {
        return "/home";
    }
    
    /**
     * Verify dashboard elements are loaded
     */
    public void verifyDashboardLoaded() {
        logger.info("Verifying dashboard is loaded");
        verifyPage();
        
        // Verify dashboard title
        waitForPageLoad();
        String title = getText(DASHBOARD_TITLE);
        logger.info("Dashboard title: {}", title);
        
        // Verify essential dashboard elements
        if (isVisible(TOTAL_EXPENSES_CARD)) {
            logger.info("Total expenses card is visible");
        }
        
        if (isVisible(EXPENSE_COUNT_CARD)) {
            logger.info("Expense count card is visible");
        }
        
        if (isVisible(CATEGORIES_COUNT_CARD)) {
            logger.info("Categories count card is visible");
        }
        
        logger.info("Dashboard verification completed");
    }
    
    /**
     * Get total expenses amount from dashboard
     */
    public String getTotalExpenses() {
        logger.debug("Getting total expenses from dashboard");
        if (isVisible(TOTAL_EXPENSES_CARD)) {
            return getText(By.cssSelector("#totalExpensesCard .card-text"));
        }
        return "0";
    }
    
    /**
     * Get expense count from dashboard
     */
    public String getExpenseCount() {
        logger.debug("Getting expense count from dashboard");
        if (isVisible(EXPENSE_COUNT_CARD)) {
            return getText(By.cssSelector("#expenseCountCard .card-text"));
        }
        return "0";
    }
    
    /**
     * Get categories count from dashboard
     */
    public String getCategoriesCount() {
        logger.debug("Getting categories count from dashboard");
        if (isVisible(CATEGORIES_COUNT_CARD)) {
            return getText(By.cssSelector("#categoriesCountCard .card-text"));
        }
        return "0";
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
     * Click View All Expenses button to navigate to expenses list
     */
    public ExpensesPage clickViewAllExpenses() {
        logger.info("Clicking View All Expenses button");
        click(VIEW_ALL_EXPENSES_BUTTON);
        return new ExpensesPage();
    }
    
    /**
     * Check if recent expenses table is visible
     */
    public boolean hasRecentExpenses() {
        return isVisible(RECENT_EXPENSES_TABLE);
    }
    
    /**
     * Check if no expenses message is shown
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
     * Get recent expenses from the table
     */
    public int getRecentExpensesCount() {
        if (hasRecentExpenses()) {
            return WebDriverConfig.getDriver()
                .findElements(By.cssSelector("#recentExpensesTable tbody tr"))
                .size();
        }
        return 0;
    }
    
    /**
     * Check if expense chart is visible
     */
    public boolean hasExpenseChart() {
        return isVisible(EXPENSE_CHART);
    }
    
    /**
     * Verify dashboard statistics are numeric
     */
    public void verifyStatisticsAreNumeric() {
        logger.info("Verifying dashboard statistics are numeric");
        
        String totalExpenses = getTotalExpenses().replace("$", "").replace(",", "");
        String expenseCount = getExpenseCount();
        String categoriesCount = getCategoriesCount();
        
        try {
            Double.parseDouble(totalExpenses);
            Integer.parseInt(expenseCount);
            Integer.parseInt(categoriesCount);
            logger.info("All statistics are numeric - Total: {}, Count: {}, Categories: {}", 
                totalExpenses, expenseCount, categoriesCount);
        } catch (NumberFormatException e) {
            logger.error("Statistics are not numeric - Total: {}, Count: {}, Categories: {}", 
                totalExpenses, expenseCount, categoriesCount);
            throw new AssertionError("Dashboard statistics are not numeric values");
        }
    }
    
    /**
     * Get dashboard page summary for logging/reporting
     */
    public String getDashboardSummary() {
        return String.format("Dashboard Summary - Total Expenses: %s, Expense Count: %s, Categories Count: %s, Has Recent Expenses: %s", 
            getTotalExpenses(), getExpenseCount(), getCategoriesCount(), hasRecentExpenses());
    }
}