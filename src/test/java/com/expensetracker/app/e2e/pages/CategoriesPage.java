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
 * Page Object for the Categories page
 * Contains locators and methods for category management functionality
 */
public class CategoriesPage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoriesPage.class);
    
    // Page-specific locators
    private static final By CATEGORIES_TITLE = By.tagName("h1");
    private static final By ADD_CATEGORY_BUTTON = By.id("addCategoryBtn");
    private static final By CATEGORIES_TABLE = By.id("categoriesTable");
    private static final By TABLE_ROWS = By.cssSelector("#categoriesTable tbody tr");
    private static final By NO_CATEGORIES_MESSAGE = By.id("noCategoriesMessage");
    private static final By SEARCH_INPUT = By.id("searchInput");
    private static final By SEARCH_BUTTON = By.id("searchBtn");
    private static final By CLEAR_SEARCH_BUTTON = By.id("clearSearchBtn");
    
    // Table column locators
    private static final By NAME_COLUMN = By.cssSelector("td:nth-child(1)");
    private static final By DESCRIPTION_COLUMN = By.cssSelector("td:nth-child(2)");
    private static final By EXPENSE_COUNT_COLUMN = By.cssSelector("td:nth-child(3)");
    private static final By ACTIONS_COLUMN = By.cssSelector("td:nth-child(4)");
    
    // Action buttons in table rows
    private static final String EDIT_BUTTON_XPATH = "//tr[contains(., '%s')]//a[contains(@class, 'btn-primary')]";
    private static final String DELETE_BUTTON_XPATH = "//tr[contains(., '%s')]//button[contains(@class, 'btn-danger')]";
    
    // Page Factory elements
    @FindBy(id = "addCategoryBtn")
    private WebElement addCategoryButton;
    
    @FindBy(id = "searchInput")
    private WebElement searchInput;
    
    @FindBy(id = "searchBtn")
    private WebElement searchButton;
    
    @FindBy(id = "clearSearchBtn")
    private WebElement clearSearchButton;
    
    public CategoriesPage() {
        super();
        logger.info("CategoriesPage initialized");
    }
    
    @Override
    public String getExpectedPageTitle() {
        return "Expense Tracker - Categories";
    }
    
    @Override
    public String getExpectedUrlFragment() {
        return "/categories";
    }
    
    /**
     * Verify categories page is loaded
     */
    public void verifyCategoriesPageLoaded() {
        logger.info("Verifying categories page is loaded");
        verifyPage();
        waitForPageLoad();
        
        // Verify page title
        String title = getText(CATEGORIES_TITLE);
        logger.info("Categories page title: {}", title);
        
        // Verify Add Category button is visible
        if (isVisible(ADD_CATEGORY_BUTTON)) {
            logger.info("Add Category button is visible");
        }
        
        logger.info("Categories page verification completed");
    }
    
    /**
     * Click Add Category button to navigate to category form
     */
    public CategoryFormPage clickAddCategory() {
        logger.info("Clicking Add Category button");
        click(ADD_CATEGORY_BUTTON);
        return new CategoryFormPage();
    }
    
    /**
     * Get the number of categories currently displayed
     */
    public int getCategoryCount() {
        if (isVisible(CATEGORIES_TABLE)) {
            List<WebElement> rows = WebDriverConfig.getDriver().findElements(TABLE_ROWS);
            logger.info("Found {} categories in the table", rows.size());
            return rows.size();
        }
        logger.info("No categories table found, returning 0");
        return 0;
    }
    
    /**
     * Check if categories table is visible
     */
    public boolean hasCategoriesTable() {
        return isVisible(CATEGORIES_TABLE);
    }
    
    /**
     * Check if no categories message is displayed
     */
    public boolean hasNoCategoriesMessage() {
        return isVisible(NO_CATEGORIES_MESSAGE);
    }
    
    /**
     * Get no categories message text
     */
    public String getNoCategoriesMessage() {
        if (hasNoCategoriesMessage()) {
            return getText(NO_CATEGORIES_MESSAGE);
        }
        return "";
    }
    
    /**
     * Search for categories by name
     */
    public void searchCategories(String searchTerm) {
        logger.info("Searching for categories with term: {}", searchTerm);
        type(SEARCH_INPUT, searchTerm);
        click(SEARCH_BUTTON);
        waitForPageLoad();
        logger.info("Search completed for term: {}", searchTerm);
    }
    
    /**
     * Clear search
     */
    public void clearSearch() {
        logger.info("Clearing search");
        click(CLEAR_SEARCH_BUTTON);
        waitForPageLoad();
        logger.info("Search cleared");
    }
    
    /**
     * Edit category by name
     */
    public CategoryFormPage editCategory(String categoryName) {
        logger.info("Editing category with name: {}", categoryName);
        By editButton = By.xpath(String.format(EDIT_BUTTON_XPATH, categoryName));
        WaitUtils.waitForElementClickable(editButton);
        click(editButton);
        return new CategoryFormPage();
    }
    
    /**
     * Delete category by name
     */
    public void deleteCategory(String categoryName) {
        logger.info("Deleting category with name: {}", categoryName);
        By deleteButton = By.xpath(String.format(DELETE_BUTTON_XPATH, categoryName));
        WaitUtils.waitForElementClickable(deleteButton);
        click(deleteButton);
        
        // Handle confirmation dialog if present
        handleDeleteConfirmation();
        waitForPageLoad();
        logger.info("Category deleted: {}", categoryName);
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
     * Verify category exists in the table
     */
    public boolean isCategoryPresent(String categoryName) {
        if (!hasCategoriesTable()) {
            return false;
        }
        
        By categoryRow = By.xpath(String.format("//tr[contains(., '%s')]", categoryName));
        return isVisible(categoryRow);
    }
    
    /**
     * Get category details by name
     */
    public CategoryDetails getCategoryDetails(String categoryName) {
        logger.debug("Getting category details for: {}", categoryName);
        
        if (!isCategoryPresent(categoryName)) {
            throw new RuntimeException("Category not found: " + categoryName);
        }
        
        By categoryRow = By.xpath(String.format("//tr[contains(., '%s')]", categoryName));
        WebElement row = WebDriverConfig.getDriver().findElement(categoryRow);
        
        String name = row.findElement(NAME_COLUMN).getText();
        String description = row.findElement(DESCRIPTION_COLUMN).getText();
        String expenseCount = row.findElement(EXPENSE_COUNT_COLUMN).getText();
        
        return new CategoryDetails(name, description, expenseCount);
    }
    
    /**
     * Get all category names currently visible
     */
    public List<String> getAllCategoryNames() {
        List<WebElement> rows = WebDriverConfig.getDriver().findElements(TABLE_ROWS);
        return rows.stream()
            .map(row -> row.findElement(NAME_COLUMN).getText())
            .toList();
    }
    
    /**
     * Get category expense count by name
     */
    public int getCategoryExpenseCount(String categoryName) {
        try {
            CategoryDetails details = getCategoryDetails(categoryName);
            return Integer.parseInt(details.getExpenseCount());
        } catch (Exception e) {
            logger.warn("Could not get expense count for category: {}", categoryName);
            return 0;
        }
    }
    
    /**
     * Verify categories are sorted alphabetically
     */
    public boolean areCategoriesSorted() {
        List<String> categoryNames = getAllCategoryNames();
        if (categoryNames.size() <= 1) {
            return true;
        }
        
        for (int i = 1; i < categoryNames.size(); i++) {
            if (categoryNames.get(i - 1).compareToIgnoreCase(categoryNames.get(i)) > 0) {
                logger.warn("Categories are not sorted: {} comes before {}", 
                    categoryNames.get(i - 1), categoryNames.get(i));
                return false;
            }
        }
        
        logger.info("Categories are sorted alphabetically");
        return true;
    }
    
    /**
     * Get default/system categories count
     */
    public int getSystemCategoriesCount() {
        // Default categories that should exist in the system
        String[] systemCategories = {"Food", "Transportation", "Entertainment", "Utilities", "Healthcare", "Other"};
        int count = 0;
        
        for (String category : systemCategories) {
            if (isCategoryPresent(category)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Inner class for category details
     */
    public static class CategoryDetails {
        private final String name;
        private final String description;
        private final String expenseCount;
        
        public CategoryDetails(String name, String description, String expenseCount) {
            this.name = name;
            this.description = description;
            this.expenseCount = expenseCount;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getExpenseCount() { return expenseCount; }
        
        @Override
        public String toString() {
            return String.format("CategoryDetails{name='%s', description='%s', expenseCount='%s'}", 
                name, description, expenseCount);
        }
    }
}