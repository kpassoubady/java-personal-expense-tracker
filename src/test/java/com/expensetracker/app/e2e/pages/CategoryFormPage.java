package com.expensetracker.app.e2e.pages;

import com.expensetracker.app.e2e.config.WebDriverConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for the Category Form page (Add/Edit Category)
 * Contains locators and methods for category form functionality
 */
public class CategoryFormPage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryFormPage.class);
    
    // Page-specific locators
    private static final By FORM_TITLE = By.tagName("h1");
    private static final By CATEGORY_FORM = By.id("categoryForm");
    private static final By NAME_INPUT = By.id("name");
    private static final By DESCRIPTION_INPUT = By.id("description");
    private static final By SAVE_BUTTON = By.id("saveBtn");
    private static final By CANCEL_BUTTON = By.id("cancelBtn");
    private static final By DELETE_BUTTON = By.id("deleteBtn");
    
    // Validation error locators
    private static final By NAME_ERROR = By.id("name-error");
    private static final By DESCRIPTION_ERROR = By.id("description-error");
    private static final By FORM_ERRORS = By.cssSelector(".field-error");
    
    // Page Factory elements
    @FindBy(id = "name")
    private WebElement nameInput;
    
    @FindBy(id = "description")
    private WebElement descriptionInput;
    
    @FindBy(id = "saveBtn")
    private WebElement saveButton;
    
    @FindBy(id = "cancelBtn")
    private WebElement cancelButton;
    
    public CategoryFormPage() {
        super();
        logger.info("CategoryFormPage initialized");
    }
    
    @Override
    public String getExpectedPageTitle() {
        return "Expense Tracker - Add Category";
    }
    
    @Override
    public String getExpectedUrlFragment() {
        return "/category";
    }
    
    /**
     * Verify category form page is loaded
     */
    public void verifyCategoryFormLoaded() {
        logger.info("Verifying category form is loaded");
        verifyPage();
        waitForPageLoad();
        
        // Verify form is present
        if (isVisible(CATEGORY_FORM)) {
            logger.info("Category form is visible");
        }
        
        // Verify form title
        String title = getText(FORM_TITLE);
        logger.info("Category form title: {}", title);
        
        logger.info("Category form verification completed");
    }
    
    /**
     * Fill name field
     */
    public CategoryFormPage enterName(String name) {
        logger.info("Entering name: {}", name);
        type(NAME_INPUT, name);
        return this;
    }
    
    /**
     * Fill description field
     */
    public CategoryFormPage enterDescription(String description) {
        logger.info("Entering description: {}", description);
        type(DESCRIPTION_INPUT, description);
        return this;
    }
    
    /**
     * Fill complete category form
     */
    public CategoryFormPage fillCategoryForm(String name, String description) {
        logger.info("Filling complete category form - Name: {}, Description: {}", name, description);
        
        enterName(name);
        if (description != null && !description.trim().isEmpty()) {
            enterDescription(description);
        }
        
        logger.info("Category form filled successfully");
        return this;
    }
    
    /**
     * Click Save button to submit the form
     */
    public CategoriesPage saveCategory() {
        logger.info("Saving category");
        click(SAVE_BUTTON);
        waitForPageLoad();
        logger.info("Category save action completed");
        return new CategoriesPage();
    }
    
    /**
     * Click Cancel button to return without saving
     */
    public CategoriesPage cancelCategory() {
        logger.info("Canceling category form");
        click(CANCEL_BUTTON);
        waitForPageLoad();
        logger.info("Category form canceled");
        return new CategoriesPage();
    }
    
    /**
     * Click Delete button (for edit mode)
     */
    public CategoriesPage deleteCategory() {
        logger.info("Deleting category");
        if (isVisible(DELETE_BUTTON)) {
            click(DELETE_BUTTON);
            handleDeleteConfirmation();
            waitForPageLoad();
            logger.info("Category deleted");
        } else {
            logger.warn("Delete button not visible - might not be in edit mode");
        }
        return new CategoriesPage();
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
     * Get current name value
     */
    public String getName() {
        return getAttribute(NAME_INPUT, "value");
    }
    
    /**
     * Get current description value
     */
    public String getDescription() {
        return getAttribute(DESCRIPTION_INPUT, "value");
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
     * Get name validation error
     */
    public String getNameError() {
        return isVisible(NAME_ERROR) ? getText(NAME_ERROR) : "";
    }
    
    /**
     * Get description validation error
     */
    public String getDescriptionError() {
        return isVisible(DESCRIPTION_ERROR) ? getText(DESCRIPTION_ERROR) : "";
    }
    
    /**
     * Get all validation error messages
     */
    public String getAllValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        String nameError = getNameError();
        String descError = getDescriptionError();
        
        if (!nameError.isEmpty()) errors.append("Name: ").append(nameError).append("; ");
        if (!descError.isEmpty()) errors.append("Description: ").append(descError).append("; ");
        
        return errors.toString();
    }
    
    /**
     * Clear all form fields
     */
    public CategoryFormPage clearForm() {
        logger.info("Clearing category form");
        
        type(NAME_INPUT, "");
        type(DESCRIPTION_INPUT, "");
        
        logger.info("Category form cleared");
        return this;
    }
    
    /**
     * Validate required fields are filled
     */
    public boolean areRequiredFieldsFilled() {
        String name = getName();
        return !name.isEmpty();
    }
    
    /**
     * Get form summary for logging/reporting
     */
    public String getFormSummary() {
        return String.format("CategoryForm - Name: %s, Description: %s, EditMode: %s", 
            getName(), getDescription(), isEditMode());
    }
}