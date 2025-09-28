package com.expensetracker.app.e2e.pages;

import com.expensetracker.app.e2e.config.TestConfig;
import com.expensetracker.app.e2e.config.WebDriverConfig;
import com.expensetracker.app.e2e.utils.AssertUtils;
import com.expensetracker.app.e2e.utils.ScreenshotUtils;
import com.expensetracker.app.e2e.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Page Object class providing common functionality for all pages
 * Implements Page Object Model pattern with utility methods
 */
public abstract class BasePage {
    
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    protected TestConfig config;
    
    // Common locators present on most pages
    protected static final By NAVBAR = By.cssSelector(".navbar");
    protected static final By NAVBAR_BRAND = By.cssSelector(".navbar-brand");
    protected static final By HOME_LINK = By.linkText("Dashboard");
    protected static final By EXPENSES_LINK = By.linkText("Expenses");
    protected static final By CATEGORIES_LINK = By.linkText("Categories");
    protected static final By LOADING_SPINNER = By.id("loadingSpinner");
    protected static final By ALERT_SUCCESS = By.cssSelector(".alert-success");
    protected static final By ALERT_ERROR = By.cssSelector(".alert-danger");
    protected static final By ALERT_WARNING = By.cssSelector(".alert-warning");
    protected static final By ALERT_INFO = By.cssSelector(".alert-info");
    
    public BasePage() {
        this.config = TestConfig.getInstance();
        PageFactory.initElements(WebDriverConfig.getDriver(), this);
        logger.debug("Initialized page: {}", this.getClass().getSimpleName());
    }
    
    /**
     * Get the expected page title (to be implemented by subclasses)
     */
    public abstract String getExpectedPageTitle();
    
    /**
     * Get the expected URL fragment (to be implemented by subclasses)
     */
    public abstract String getExpectedUrlFragment();
    
    /**
     * Verify that we are on the correct page
     */
    public void verifyPage() {
        logger.info("Verifying page: {}", this.getClass().getSimpleName());
        
        // Wait for page to load
        WaitUtils.waitForPageLoad();
        
        // Verify URL contains expected fragment
        if (!getExpectedUrlFragment().isEmpty()) {
            AssertUtils.assertCurrentUrlContains(getExpectedUrlFragment(), 
                "Should be on " + this.getClass().getSimpleName());
        }
        
        // Verify title contains expected text
        if (!getExpectedPageTitle().isEmpty()) {
            AssertUtils.assertPageTitleContains(getExpectedPageTitle(), 
                "Page title should contain expected text for " + this.getClass().getSimpleName());
        }
        
        // Verify navbar is present
        AssertUtils.assertElementVisible(NAVBAR, "Navbar should be visible");
        
        logger.info("Page verification successful: {}", this.getClass().getSimpleName());
    }
    
    /**
     * Navigate to home page
     */
    public HomePage navigateToHome() {
        logger.info("Navigating to home page");
        click(HOME_LINK);
        return new HomePage();
    }
    
    /**
     * Navigate to expenses page
     */
    public ExpensesPage navigateToExpenses() {
        logger.info("Navigating to expenses page");
        click(EXPENSES_LINK);
        return new ExpensesPage();
    }
    
    /**
     * Navigate to categories page
     */
    public CategoriesPage navigateToCategories() {
        logger.info("Navigating to categories page");
        click(CATEGORIES_LINK);
        return new CategoriesPage();
    }
    
    /**
     * Wait for page to load completely
     */
    protected void waitForPageLoad() {
        WaitUtils.waitForPageLoad();
        waitForLoadingSpinnerToDisappear();
    }
    
    /**
     * Wait for loading spinner to disappear
     */
    protected void waitForLoadingSpinnerToDisappear() {
        try {
            WaitUtils.waitForElementInvisible(LOADING_SPINNER, 10);
        } catch (Exception e) {
            // Loading spinner may not be present, which is fine
            logger.debug("Loading spinner not found or already invisible");
        }
    }
    
    /**
     * Click element with wait and error handling
     */
    protected void click(By locator) {
        logger.debug("Clicking element: {}", locator);
        WebElement element = WaitUtils.waitForElementClickable(locator);
        scrollToElement(element);
        element.click();
        logger.debug("Successfully clicked element: {}", locator);
    }
    
    /**
     * Type text into element with wait and clear
     */
    protected void type(By locator, String text) {
        logger.debug("Typing '{}' into element: {}", text, locator);
        WebElement element = WaitUtils.waitForElementVisible(locator);
        scrollToElement(element);
        element.clear();
        element.sendKeys(text);
        logger.debug("Successfully typed '{}' into element: {}", text, locator);
    }
    
    /**
     * Get text from element with wait
     */
    protected String getText(By locator) {
        logger.debug("Getting text from element: {}", locator);
        WebElement element = WaitUtils.waitForElementVisible(locator);
        String text = element.getText().trim();
        logger.debug("Got text '{}' from element: {}", text, locator);
        return text;
    }
    
    /**
     * Get attribute value from element
     */
    protected String getAttribute(By locator, String attributeName) {
        logger.debug("Getting attribute '{}' from element: {}", attributeName, locator);
        WebElement element = WaitUtils.waitForElementPresent(locator);
        String value = element.getAttribute(attributeName);
        logger.debug("Got attribute '{}' value '{}' from element: {}", attributeName, value, locator);
        return value;
    }
    
    /**
     * Check if element is visible
     */
    public boolean isVisible(By locator) {
        return WaitUtils.isElementVisible(locator);
    }
    
    /**
     * Check if element is present
     */
    protected boolean isPresent(By locator) {
        return WaitUtils.isElementPresent(locator);
    }
    
    /**
     * Scroll element into view
     */
    protected void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) WebDriverConfig.getDriver())
                .executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element);
            Thread.sleep(500); // Small delay for smooth scrolling
        } catch (Exception e) {
            logger.warn("Could not scroll to element: {}", e.getMessage());
        }
    }
    
    /**
     * Scroll to element by locator
     */
    protected void scrollToElement(By locator) {
        WebElement element = WaitUtils.waitForElementPresent(locator);
        scrollToElement(element);
    }
    
    /**
     * Perform hover action
     */
    protected void hover(By locator) {
        logger.debug("Hovering over element: {}", locator);
        WebElement element = WaitUtils.waitForElementVisible(locator);
        Actions actions = new Actions(WebDriverConfig.getDriver());
        actions.moveToElement(element).perform();
        logger.debug("Successfully hovered over element: {}", locator);
    }
    
    /**
     * Double click element
     */
    protected void doubleClick(By locator) {
        logger.debug("Double clicking element: {}", locator);
        WebElement element = WaitUtils.waitForElementClickable(locator);
        Actions actions = new Actions(WebDriverConfig.getDriver());
        actions.doubleClick(element).perform();
        logger.debug("Successfully double clicked element: {}", locator);
    }
    
    /**
     * Right click element
     */
    protected void rightClick(By locator) {
        logger.debug("Right clicking element: {}", locator);
        WebElement element = WaitUtils.waitForElementClickable(locator);
        Actions actions = new Actions(WebDriverConfig.getDriver());
        actions.contextClick(element).perform();
        logger.debug("Successfully right clicked element: {}", locator);
    }
    
    /**
     * Select dropdown option by visible text
     */
    protected void selectByText(By locator, String text) {
        logger.debug("Selecting option '{}' from dropdown: {}", text, locator);
        WebElement dropdown = WaitUtils.waitForElementClickable(locator);
        org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(dropdown);
        select.selectByVisibleText(text);
        logger.debug("Successfully selected option '{}' from dropdown: {}", text, locator);
    }
    
    /**
     * Select dropdown option by value
     */
    protected void selectByValue(By locator, String value) {
        logger.debug("Selecting option with value '{}' from dropdown: {}", value, locator);
        WebElement dropdown = WaitUtils.waitForElementClickable(locator);
        org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(dropdown);
        select.selectByValue(value);
        logger.debug("Successfully selected option with value '{}' from dropdown: {}", value, locator);
    }
    
    /**
     * Check if alert is present and get its text
     */
    public String getAlertText(By alertLocator) {
        if (isVisible(alertLocator)) {
            return getText(alertLocator);
        }
        return "";
    }
    
    /**
     * Get success message if present
     */
    public String getSuccessMessage() {
        return getAlertText(ALERT_SUCCESS);
    }
    
    /**
     * Get error message if present
     */
    public String getErrorMessage() {
        return getAlertText(ALERT_ERROR);
    }
    
    /**
     * Get warning message if present
     */
    public String getWarningMessage() {
        return getAlertText(ALERT_WARNING);
    }
    
    /**
     * Get info message if present
     */
    public String getInfoMessage() {
        return getAlertText(ALERT_INFO);
    }
    
    /**
     * Wait for success message to appear
     */
    public void waitForSuccessMessage() {
        WaitUtils.waitForElementVisible(ALERT_SUCCESS);
    }
    
    /**
     * Wait for error message to appear
     */
    public void waitForErrorMessage() {
        WaitUtils.waitForElementVisible(ALERT_ERROR);
    }
    
    /**
     * Refresh the current page
     */
    public void refresh() {
        logger.info("Refreshing page");
        WebDriverConfig.getDriver().navigate().refresh();
        waitForPageLoad();
    }
    
    /**
     * Go back to previous page
     */
    public void goBack() {
        logger.info("Navigating back");
        WebDriverConfig.getDriver().navigate().back();
        waitForPageLoad();
    }
    
    /**
     * Take screenshot of current page
     */
    public String takeScreenshot(String screenshotName) {
        return ScreenshotUtils.captureScreenshot(screenshotName);
    }
    
    /**
     * Execute JavaScript
     */
    protected Object executeJavaScript(String script, Object... args) {
        return ((JavascriptExecutor) WebDriverConfig.getDriver()).executeScript(script, args);
    }
    
    /**
     * Get current page URL
     */
    public String getCurrentUrl() {
        return WebDriverConfig.getDriver().getCurrentUrl();
    }
    
    /**
     * Get current page title
     */
    public String getCurrentTitle() {
        return WebDriverConfig.getDriver().getTitle();
    }
    
    /**
     * Switch to new window/tab
     */
    protected void switchToNewWindow() {
        String originalWindow = WebDriverConfig.getDriver().getWindowHandle();
        for (String windowHandle : WebDriverConfig.getDriver().getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                WebDriverConfig.getDriver().switchTo().window(windowHandle);
                break;
            }
        }
    }
    
    /**
     * Close current window and switch back
     */
    protected void closeCurrentWindowAndSwitchBack() {
        WebDriverConfig.getDriver().close();
        for (String windowHandle : WebDriverConfig.getDriver().getWindowHandles()) {
            WebDriverConfig.getDriver().switchTo().window(windowHandle);
            break;
        }
    }
}