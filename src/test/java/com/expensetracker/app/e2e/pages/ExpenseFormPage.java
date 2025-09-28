package com.expensetracker.app.e2e.pages;

import com.expensetracker.app.e2e.config.WebDriverConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for the Expense Form page (Add/Edit Expense)
 * Contains locators and methods for expense form functionality
 */
public class ExpenseFormPage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpenseFormPage.class);
    
    // Page-specific locators
    private static final By FORM_TITLE = By.tagName("h1");
    private static final By EXPENSE_FORM = By.id("expenseForm");
    private static final By DESCRIPTION_INPUT = By.id("description");
    private static final By AMOUNT_INPUT = By.id("amount");
    private static final By CATEGORY_SELECT = By.id("category");
    private static final By DATE_INPUT = By.id("date");
    private static final By NOTES_TEXTAREA = By.id("notes");
    private static final By SAVE_BUTTON = By.id("saveBtn");
    private static final By CANCEL_BUTTON = By.id("cancelBtn");
    private static final By DELETE_BUTTON = By.id("deleteBtn");
    
    // Validation error locators
    private static final By DESCRIPTION_ERROR = By.id("description-error");
    private static final By AMOUNT_ERROR = By.id("amount-error");
    private static final By CATEGORY_ERROR = By.id("category-error");
    private static final By DATE_ERROR = By.id("date-error");
    private static final By FORM_ERRORS = By.cssSelector(".field-error");
    
    // Page Factory elements
    @FindBy(id = "description")
    private WebElement descriptionInput;
    
    @FindBy(id = "amount")
    private WebElement amountInput;
    
    @FindBy(id = "category")
    private WebElement categorySelect;
    
    @FindBy(id = "date")
    private WebElement dateInput;
    
    @FindBy(id = "notes")
    private WebElement notesTextarea;
    
    @FindBy(id = "saveBtn")
    private WebElement saveButton;
    
    @FindBy(id = "cancelBtn")
    private WebElement cancelButton;
    
    public ExpenseFormPage() {
        super();
        logger.info("ExpenseFormPage initialized");
    }
    
    @Override
    public String getExpectedPageTitle() {
        return "Expense Tracker - Add Expense";
    }
    
    @Override
    public String getExpectedUrlFragment() {
        return "/expense";
    }
    
    /**
     * Verify expense form page is loaded
     */
    public void verifyExpenseFormLoaded() {
        logger.info("Verifying expense form is loaded");
        verifyPage();
        waitForPageLoad();
        
        // Verify form is present
        if (isVisible(EXPENSE_FORM)) {
            logger.info("Expense form is visible");
        }
        
        // Verify form title
        String title = getText(FORM_TITLE);
        logger.info("Expense form title: {}", title);
        
        logger.info("Expense form verification completed");
    }
    
    /**
     * Fill description field
     */
    public ExpenseFormPage enterDescription(String description) {
        logger.info("Entering description: {}", description);
        type(DESCRIPTION_INPUT, description);
        return this;
    }
    
    /**
     * Fill amount field
     */
    public ExpenseFormPage enterAmount(String amount) {
        logger.info("Entering amount: {}", amount);
        type(AMOUNT_INPUT, amount);
        return this;
    }
    
    /**
     * Select category from dropdown
     */
    public ExpenseFormPage selectCategory(String category) {
        logger.info("Selecting category: {}", category);
        selectByText(CATEGORY_SELECT, category);
        return this;
    }
    
    /**
     * Fill date field
     */
    public ExpenseFormPage enterDate(String date) {
        logger.info("Entering date: {}", date);
        type(DATE_INPUT, date);
        return this;
    }
    
    /**
     * Fill notes field
     */
    public ExpenseFormPage enterNotes(String notes) {
        logger.info("Entering notes: {}", notes);
        type(NOTES_TEXTAREA, notes);
        return this;
    }
    
    /**
     * Fill complete expense form
     */
    public ExpenseFormPage fillExpenseForm(String description, String amount, String category, String date, String notes) {
        logger.info("Filling complete expense form - Description: {}, Amount: {}, Category: {}, Date: {}", 
            description, amount, category, date);
        
        enterDescription(description);
        enterAmount(amount);
        selectCategory(category);
        enterDate(date);
        
        if (notes != null && !notes.trim().isEmpty()) {
            enterNotes(notes);
        }
        
        logger.info("Expense form filled successfully");
        return this;
    }
    
    /**
     * Fill expense form without notes
     */
    public ExpenseFormPage fillExpenseForm(String description, String amount, String category, String date) {
        return fillExpenseForm(description, amount, category, date, null);
    }
    
    /**
     * Click Save button to submit the form
     */
    public ExpensesPage saveExpense() {
        logger.info("Saving expense");
        click(SAVE_BUTTON);
        waitForPageLoad();
        logger.info("Expense save action completed");
        return new ExpensesPage();
    }
    
    /**
     * Click Cancel button to return without saving
     */
    public ExpensesPage cancelExpense() {
        logger.info("Canceling expense form");
        click(CANCEL_BUTTON);
        waitForPageLoad();
        logger.info("Expense form canceled");
        return new ExpensesPage();
    }
    
    /**
     * Click Delete button (for edit mode)
     */
    public ExpensesPage deleteExpense() {
        logger.info("Deleting expense");
        if (isVisible(DELETE_BUTTON)) {
            click(DELETE_BUTTON);
            handleDeleteConfirmation();
            waitForPageLoad();
            logger.info("Expense deleted");
        } else {
            logger.warn("Delete button not visible - might not be in edit mode");
        }
        return new ExpensesPage();
    }
    
    /**
     * Handle delete confirmation dialog
     */
    private void handleDeleteConfirmation() {
        try {
            By confirmButton = By.id("confirmDeleteBtn");
            if (isVisible(confirmButton)) {
                click(confirmButton);
                logger.debug("Delete confirmation handled");
            }
        } catch (Exception e) {
            logger.debug("No delete confirmation dialog found");
        }
    }
    
    /**
     * Get current description value
     */
    public String getDescription() {
        return getAttribute(DESCRIPTION_INPUT, "value");
    }
    
    /**
     * Get current amount value
     */
    public String getAmount() {
        return getAttribute(AMOUNT_INPUT, "value");
    }
    
    /**
     * Get currently selected category
     */
    public String getSelectedCategory() {
        WebElement selected = WebDriverConfig.getDriver()
            .findElement(By.cssSelector("#category option:checked"));
        return selected.getText();
    }
    
    /**
     * Get current date value
     */
    public String getDate() {
        return getAttribute(DATE_INPUT, "value");
    }
    
    /**
     * Get current notes value
     */
    public String getNotes() {
        return getAttribute(NOTES_TEXTAREA, "value");
    }
    
    /**
     * Check if form is in edit mode (has delete button)
     */
    public boolean isEditMode() {
        return isVisible(DELETE_BUTTON);
    }
    
    /**
     * Check if form has validation errors
     */
    public boolean hasValidationErrors() {
        return !WebDriverConfig.getDriver().findElements(FORM_ERRORS).isEmpty();
    }
    
    /**
     * Get description validation error
     */
    public String getDescriptionError() {
        return isVisible(DESCRIPTION_ERROR) ? getText(DESCRIPTION_ERROR) : "";
    }
    
    /**
     * Get amount validation error
     */
    public String getAmountError() {
        return isVisible(AMOUNT_ERROR) ? getText(AMOUNT_ERROR) : "";
    }
    
    /**
     * Get category validation error
     */
    public String getCategoryError() {
        return isVisible(CATEGORY_ERROR) ? getText(CATEGORY_ERROR) : "";
    }
    
    /**
     * Get date validation error
     */
    public String getDateError() {
        return isVisible(DATE_ERROR) ? getText(DATE_ERROR) : "";
    }
    
    /**
     * Get all validation error messages
     */
    public String getAllValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        String descError = getDescriptionError();
        String amountError = getAmountError();
        String categoryError = getCategoryError();
        String dateError = getDateError();
        
        if (!descError.isEmpty()) errors.append("Description: ").append(descError).append("; ");
        if (!amountError.isEmpty()) errors.append("Amount: ").append(amountError).append("; ");
        if (!categoryError.isEmpty()) errors.append("Category: ").append(categoryError).append("; ");
        if (!dateError.isEmpty()) errors.append("Date: ").append(dateError).append("; ");
        
        return errors.toString();
    }
    
    /**
     * Clear all form fields
     */
    public ExpenseFormPage clearForm() {
        logger.info("Clearing expense form");
        
        type(DESCRIPTION_INPUT, "");
        type(AMOUNT_INPUT, "");
        type(DATE_INPUT, "");
        type(NOTES_TEXTAREA, "");
        
        // Reset category to first option
        selectByValue(CATEGORY_SELECT, "");
        
        logger.info("Expense form cleared");
        return this;
    }
    
    /**
     * Validate required fields are filled
     */
    public boolean areRequiredFieldsFilled() {
        String description = getDescription();
        String amount = getAmount();
        String category = getSelectedCategory();
        String date = getDate();
        
        return !description.isEmpty() && !amount.isEmpty() && 
               !category.isEmpty() && !date.isEmpty();
    }
    
    /**
     * Get form summary for logging/reporting
     */
    public String getFormSummary() {
        return String.format("ExpenseForm - Description: %s, Amount: %s, Category: %s, Date: %s, Notes: %s, EditMode: %s", 
            getDescription(), getAmount(), getSelectedCategory(), getDate(), getNotes(), isEditMode());
    }
}