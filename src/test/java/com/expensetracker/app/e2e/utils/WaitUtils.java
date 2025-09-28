package com.expensetracker.app.e2e.utils;

import com.expensetracker.app.e2e.config.WebDriverConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Custom WebDriver wait utilities for common web element conditions
 * Provides reusable wait methods with proper logging and error handling
 */
public class WaitUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(WaitUtils.class);
    private static final int DEFAULT_TIMEOUT = 30;
    private static final int SHORT_TIMEOUT = 10;
    private static final int LONG_TIMEOUT = 60;
    
    /**
     * Wait for element to be visible
     */
    public static WebElement waitForElementVisible(By locator) {
        return waitForElementVisible(locator, DEFAULT_TIMEOUT);
    }
    
    public static WebElement waitForElementVisible(By locator, int timeoutSeconds) {
        try {
            logger.debug("Waiting for element to be visible: {}", locator);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            logger.debug("Element is now visible: {}", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for element to be visible: {} ({}s)", locator, timeoutSeconds);
            throw new TimeoutException("Element not visible within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Wait for element to be clickable
     */
    public static WebElement waitForElementClickable(By locator) {
        return waitForElementClickable(locator, DEFAULT_TIMEOUT);
    }
    
    public static WebElement waitForElementClickable(By locator, int timeoutSeconds) {
        try {
            logger.debug("Waiting for element to be clickable: {}", locator);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            logger.debug("Element is now clickable: {}", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for element to be clickable: {} ({}s)", locator, timeoutSeconds);
            throw new TimeoutException("Element not clickable within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Wait for element to be present in DOM
     */
    public static WebElement waitForElementPresent(By locator) {
        return waitForElementPresent(locator, DEFAULT_TIMEOUT);
    }
    
    public static WebElement waitForElementPresent(By locator, int timeoutSeconds) {
        try {
            logger.debug("Waiting for element to be present: {}", locator);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            logger.debug("Element is now present: {}", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for element to be present: {} ({}s)", locator, timeoutSeconds);
            throw new TimeoutException("Element not present within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Wait for element to disappear
     */
    public static boolean waitForElementInvisible(By locator) {
        return waitForElementInvisible(locator, DEFAULT_TIMEOUT);
    }
    
    public static boolean waitForElementInvisible(By locator, int timeoutSeconds) {
        try {
            logger.debug("Waiting for element to be invisible: {}", locator);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            boolean isInvisible = wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            logger.debug("Element is now invisible: {}", locator);
            return isInvisible;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for element to be invisible: {} ({}s)", locator, timeoutSeconds);
            throw new TimeoutException("Element still visible after " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Wait for text to be present in element
     */
    public static boolean waitForTextPresent(By locator, String text) {
        return waitForTextPresent(locator, text, DEFAULT_TIMEOUT);
    }
    
    public static boolean waitForTextPresent(By locator, String text, int timeoutSeconds) {
        try {
            logger.debug("Waiting for text '{}' to be present in element: {}", text, locator);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            boolean textPresent = wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
            logger.debug("Text '{}' is now present in element: {}", text, locator);
            return textPresent;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for text '{}' in element: {} ({}s)", text, locator, timeoutSeconds);
            throw new TimeoutException("Text '" + text + "' not found in element within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Wait for attribute to contain value
     */
    public static boolean waitForAttributeContains(By locator, String attribute, String value) {
        return waitForAttributeContains(locator, attribute, value, DEFAULT_TIMEOUT);
    }
    
    public static boolean waitForAttributeContains(By locator, String attribute, String value, int timeoutSeconds) {
        try {
            logger.debug("Waiting for attribute '{}' to contain '{}' in element: {}", attribute, value, locator);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            boolean attributeContains = wait.until(ExpectedConditions.attributeContains(locator, attribute, value));
            logger.debug("Attribute '{}' now contains '{}' in element: {}", attribute, value, locator);
            return attributeContains;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for attribute '{}' to contain '{}' in element: {} ({}s)", attribute, value, locator, timeoutSeconds);
            throw new TimeoutException("Attribute '" + attribute + "' does not contain '" + value + "' within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Wait for page title to contain text
     */
    public static boolean waitForTitleContains(String title) {
        return waitForTitleContains(title, DEFAULT_TIMEOUT);
    }
    
    public static boolean waitForTitleContains(String title, int timeoutSeconds) {
        try {
            logger.debug("Waiting for page title to contain: '{}'", title);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            boolean titleContains = wait.until(ExpectedConditions.titleContains(title));
            logger.debug("Page title now contains: '{}'", title);
            return titleContains;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for title to contain '{}' ({}s)", title, timeoutSeconds);
            throw new TimeoutException("Title does not contain '" + title + "' within " + timeoutSeconds + " seconds", e);
        }
    }
    
    /**
     * Wait for URL to contain text
     */
    public static boolean waitForUrlContains(String url) {
        return waitForUrlContains(url, DEFAULT_TIMEOUT);
    }
    
    public static boolean waitForUrlContains(String url, int timeoutSeconds) {
        try {
            logger.debug("Waiting for URL to contain: '{}'", url);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            boolean urlContains = wait.until(ExpectedConditions.urlContains(url));
            logger.debug("URL now contains: '{}'", url);
            return urlContains;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for URL to contain '{}' ({}s)", url, timeoutSeconds);
            throw new TimeoutException("URL does not contain '" + url + "' within " + timeoutSeconds + " seconds", e);
        }
    }
    
    /**
     * Wait for number of elements to be present
     */
    public static List<WebElement> waitForNumberOfElements(By locator, int expectedCount) {
        return waitForNumberOfElements(locator, expectedCount, DEFAULT_TIMEOUT);
    }
    
    public static List<WebElement> waitForNumberOfElements(By locator, int expectedCount, int timeoutSeconds) {
        try {
            logger.debug("Waiting for {} elements to be present: {}", expectedCount, locator);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            List<WebElement> elements = wait.until(ExpectedConditions.numberOfElementsToBe(locator, expectedCount));
            logger.debug("Found {} elements: {}", expectedCount, locator);
            return elements;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for {} elements: {} ({}s)", expectedCount, locator, timeoutSeconds);
            throw new TimeoutException("Expected " + expectedCount + " elements not found within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Wait for at least number of elements to be present
     */
    public static List<WebElement> waitForMinimumNumberOfElements(By locator, int minimumCount) {
        return waitForMinimumNumberOfElements(locator, minimumCount, DEFAULT_TIMEOUT);
    }
    
    public static List<WebElement> waitForMinimumNumberOfElements(By locator, int minimumCount, int timeoutSeconds) {
        try {
            logger.debug("Waiting for at least {} elements to be present: {}", minimumCount, locator);
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            List<WebElement> elements = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, minimumCount - 1));
            logger.debug("Found {} elements (minimum {}): {}", elements.size(), minimumCount, locator);
            return elements;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for at least {} elements: {} ({}s)", minimumCount, locator, timeoutSeconds);
            throw new TimeoutException("Less than " + minimumCount + " elements found within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Wait for alert to be present
     */
    public static boolean waitForAlert() {
        return waitForAlert(SHORT_TIMEOUT);
    }
    
    public static boolean waitForAlert(int timeoutSeconds) {
        try {
            logger.debug("Waiting for alert to be present");
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            wait.until(ExpectedConditions.alertIsPresent());
            logger.debug("Alert is now present");
            return true;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for alert ({}s)", timeoutSeconds);
            throw new TimeoutException("Alert not present within " + timeoutSeconds + " seconds", e);
        }
    }
    
    /**
     * Wait for page to load (basic implementation)
     */
    public static void waitForPageLoad() {
        waitForPageLoad(DEFAULT_TIMEOUT);
    }
    
    public static void waitForPageLoad(int timeoutSeconds) {
        try {
            logger.debug("Waiting for page to load");
            WebDriverWait wait = new WebDriverWait(WebDriverConfig.getDriver(), Duration.ofSeconds(timeoutSeconds));
            wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
            logger.debug("Page load complete");
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for page load ({}s)", timeoutSeconds);
            throw new TimeoutException("Page did not load within " + timeoutSeconds + " seconds", e);
        }
    }
    
    /**
     * Check if element exists without waiting
     */
    public static boolean isElementPresent(By locator) {
        try {
            WebDriverConfig.getDriver().findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Check if element is visible without waiting
     */
    public static boolean isElementVisible(By locator) {
        try {
            WebElement element = WebDriverConfig.getDriver().findElement(locator);
            return element.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Fluent wait with custom polling interval
     */
    public static WebElement fluentWait(By locator, int timeoutSeconds, int pollingIntervalMillis) {
        try {
            logger.debug("Starting fluent wait for element: {} (timeout: {}s, polling: {}ms)", 
                        locator, timeoutSeconds, pollingIntervalMillis);
            
            org.openqa.selenium.support.ui.FluentWait<org.openqa.selenium.WebDriver> wait = 
                new org.openqa.selenium.support.ui.FluentWait<>(WebDriverConfig.getDriver())
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(pollingIntervalMillis))
                    .ignoring(NoSuchElementException.class);
            
            WebElement element = wait.until(driver -> driver.findElement(locator));
            logger.debug("Element found with fluent wait: {}", locator);
            return element;
            
        } catch (TimeoutException e) {
            logger.error("Fluent wait timeout for element: {} ({}s)", locator, timeoutSeconds);
            throw new TimeoutException("Element not found within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
}