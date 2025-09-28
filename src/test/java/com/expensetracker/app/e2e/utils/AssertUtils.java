package com.expensetracker.app.e2e.utils;

import com.expensetracker.app.e2e.config.WebDriverConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Custom assertion utilities for Selenium tests
 * Provides enhanced assertions with better error messages and logging
 */
public class AssertUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(AssertUtils.class);
    
    /**
     * Assert element is present
     */
    public static void assertElementPresent(By locator, String message) {
        logger.debug("Asserting element is present: {}", locator);
        try {
            WebElement element = WebDriverConfig.getDriver().findElement(locator);
            Assert.assertNotNull(element, message + " - Element not found: " + locator);
            logger.debug("Element is present: {}", locator);
        } catch (Exception e) {
            logger.error("Element not present: {} - {}", locator, message);
            Assert.fail(message + " - Element not found: " + locator + ". Error: " + e.getMessage());
        }
    }
    
    public static void assertElementPresent(By locator) {
        assertElementPresent(locator, "Element should be present");
    }
    
    /**
     * Assert element is not present
     */
    public static void assertElementNotPresent(By locator, String message) {
        logger.debug("Asserting element is not present: {}", locator);
        try {
            WebElement element = WebDriverConfig.getDriver().findElement(locator);
            logger.error("Element unexpectedly present: {} - {}", locator, message);
            Assert.fail(message + " - Element should not be present: " + locator);
        } catch (org.openqa.selenium.NoSuchElementException e) {
            logger.debug("Element is not present as expected: {}", locator);
        }
    }
    
    public static void assertElementNotPresent(By locator) {
        assertElementNotPresent(locator, "Element should not be present");
    }
    
    /**
     * Assert element is visible
     */
    public static void assertElementVisible(By locator, String message) {
        logger.debug("Asserting element is visible: {}", locator);
        WebElement element = WaitUtils.waitForElementVisible(locator);
        Assert.assertTrue(element.isDisplayed(), message + " - Element not visible: " + locator);
        logger.debug("Element is visible: {}", locator);
    }
    
    public static void assertElementVisible(By locator) {
        assertElementVisible(locator, "Element should be visible");
    }
    
    /**
     * Assert element is not visible
     */
    public static void assertElementNotVisible(By locator, String message) {
        logger.debug("Asserting element is not visible: {}", locator);
        try {
            WebElement element = WebDriverConfig.getDriver().findElement(locator);
            Assert.assertFalse(element.isDisplayed(), message + " - Element should not be visible: " + locator);
            logger.debug("Element is not visible as expected: {}", locator);
        } catch (org.openqa.selenium.NoSuchElementException e) {
            logger.debug("Element is not present (therefore not visible): {}", locator);
        }
    }
    
    public static void assertElementNotVisible(By locator) {
        assertElementNotVisible(locator, "Element should not be visible");
    }
    
    /**
     * Assert element text equals expected value
     */
    public static void assertElementText(By locator, String expectedText, String message) {
        logger.debug("Asserting element text equals '{}': {}", expectedText, locator);
        WebElement element = WaitUtils.waitForElementVisible(locator);
        String actualText = element.getText().trim();
        Assert.assertEquals(actualText, expectedText, 
            message + " - Text mismatch in element: " + locator + ". Expected: '" + expectedText + "', Actual: '" + actualText + "'");
        logger.debug("Element text matches expected value '{}': {}", expectedText, locator);
    }
    
    public static void assertElementText(By locator, String expectedText) {
        assertElementText(locator, expectedText, "Element text should match expected value");
    }
    
    /**
     * Assert element text contains expected value
     */
    public static void assertElementTextContains(By locator, String expectedText, String message) {
        logger.debug("Asserting element text contains '{}': {}", expectedText, locator);
        WebElement element = WaitUtils.waitForElementVisible(locator);
        String actualText = element.getText().trim();
        Assert.assertTrue(actualText.contains(expectedText), 
            message + " - Text does not contain expected value in element: " + locator + 
            ". Expected to contain: '" + expectedText + "', Actual: '" + actualText + "'");
        logger.debug("Element text contains expected value '{}': {}", expectedText, locator);
    }
    
    public static void assertElementTextContains(By locator, String expectedText) {
        assertElementTextContains(locator, expectedText, "Element text should contain expected value");
    }
    
    /**
     * Assert element attribute equals expected value
     */
    public static void assertElementAttribute(By locator, String attribute, String expectedValue, String message) {
        logger.debug("Asserting element attribute '{}' equals '{}': {}", attribute, expectedValue, locator);
        WebElement element = WaitUtils.waitForElementPresent(locator);
        String actualValue = element.getAttribute(attribute);
        Assert.assertEquals(actualValue, expectedValue, 
            message + " - Attribute mismatch in element: " + locator + ". Attribute: " + attribute + 
            ". Expected: '" + expectedValue + "', Actual: '" + actualValue + "'");
        logger.debug("Element attribute '{}' matches expected value '{}': {}", attribute, expectedValue, locator);
    }
    
    public static void assertElementAttribute(By locator, String attribute, String expectedValue) {
        assertElementAttribute(locator, attribute, expectedValue, "Element attribute should match expected value");
    }
    
    /**
     * Assert element attribute contains expected value
     */
    public static void assertElementAttributeContains(By locator, String attribute, String expectedValue, String message) {
        logger.debug("Asserting element attribute '{}' contains '{}': {}", attribute, expectedValue, locator);
        WebElement element = WaitUtils.waitForElementPresent(locator);
        String actualValue = element.getAttribute(attribute);
        Assert.assertTrue(actualValue != null && actualValue.contains(expectedValue), 
            message + " - Attribute does not contain expected value in element: " + locator + 
            ". Attribute: " + attribute + ". Expected to contain: '" + expectedValue + "', Actual: '" + actualValue + "'");
        logger.debug("Element attribute '{}' contains expected value '{}': {}", attribute, expectedValue, locator);
    }
    
    public static void assertElementAttributeContains(By locator, String attribute, String expectedValue) {
        assertElementAttributeContains(locator, attribute, expectedValue, "Element attribute should contain expected value");
    }
    
    /**
     * Assert page title equals expected value
     */
    public static void assertPageTitle(String expectedTitle, String message) {
        logger.debug("Asserting page title equals '{}'", expectedTitle);
        String actualTitle = WebDriverConfig.getDriver().getTitle();
        Assert.assertEquals(actualTitle, expectedTitle, 
            message + " - Page title mismatch. Expected: '" + expectedTitle + "', Actual: '" + actualTitle + "'");
        logger.debug("Page title matches expected value: '{}'", expectedTitle);
    }
    
    public static void assertPageTitle(String expectedTitle) {
        assertPageTitle(expectedTitle, "Page title should match expected value");
    }
    
    /**
     * Assert page title contains expected value
     */
    public static void assertPageTitleContains(String expectedText, String message) {
        logger.debug("Asserting page title contains '{}'", expectedText);
        String actualTitle = WebDriverConfig.getDriver().getTitle();
        Assert.assertTrue(actualTitle.contains(expectedText), 
            message + " - Page title does not contain expected text. Expected to contain: '" + expectedText + "', Actual: '" + actualTitle + "'");
        logger.debug("Page title contains expected text: '{}'", expectedText);
    }
    
    public static void assertPageTitleContains(String expectedText) {
        assertPageTitleContains(expectedText, "Page title should contain expected text");
    }
    
    /**
     * Assert current URL equals expected value
     */
    public static void assertCurrentUrl(String expectedUrl, String message) {
        logger.debug("Asserting current URL equals '{}'", expectedUrl);
        String actualUrl = WebDriverConfig.getDriver().getCurrentUrl();
        Assert.assertEquals(actualUrl, expectedUrl, 
            message + " - URL mismatch. Expected: '" + expectedUrl + "', Actual: '" + actualUrl + "'");
        logger.debug("Current URL matches expected value: '{}'", expectedUrl);
    }
    
    public static void assertCurrentUrl(String expectedUrl) {
        assertCurrentUrl(expectedUrl, "Current URL should match expected value");
    }
    
    /**
     * Assert current URL contains expected value
     */
    public static void assertCurrentUrlContains(String expectedText, String message) {
        logger.debug("Asserting current URL contains '{}'", expectedText);
        String actualUrl = WebDriverConfig.getDriver().getCurrentUrl();
        Assert.assertTrue(actualUrl.contains(expectedText), 
            message + " - URL does not contain expected text. Expected to contain: '" + expectedText + "', Actual: '" + actualUrl + "'");
        logger.debug("Current URL contains expected text: '{}'", expectedText);
    }
    
    public static void assertCurrentUrlContains(String expectedText) {
        assertCurrentUrlContains(expectedText, "Current URL should contain expected text");
    }
    
    /**
     * Assert number of elements equals expected count
     */
    public static void assertElementCount(By locator, int expectedCount, String message) {
        logger.debug("Asserting element count equals {}: {}", expectedCount, locator);
        List<WebElement> elements = WebDriverConfig.getDriver().findElements(locator);
        int actualCount = elements.size();
        Assert.assertEquals(actualCount, expectedCount, 
            message + " - Element count mismatch for: " + locator + ". Expected: " + expectedCount + ", Actual: " + actualCount);
        logger.debug("Element count matches expected value {}: {}", expectedCount, locator);
    }
    
    public static void assertElementCount(By locator, int expectedCount) {
        assertElementCount(locator, expectedCount, "Element count should match expected value");
    }
    
    /**
     * Assert at least minimum number of elements are present
     */
    public static void assertMinimumElementCount(By locator, int minimumCount, String message) {
        logger.debug("Asserting minimum element count {}: {}", minimumCount, locator);
        List<WebElement> elements = WebDriverConfig.getDriver().findElements(locator);
        int actualCount = elements.size();
        Assert.assertTrue(actualCount >= minimumCount, 
            message + " - Insufficient element count for: " + locator + ". Expected minimum: " + minimumCount + ", Actual: " + actualCount);
        logger.debug("Element count {} meets minimum requirement {}: {}", actualCount, minimumCount, locator);
    }
    
    public static void assertMinimumElementCount(By locator, int minimumCount) {
        assertMinimumElementCount(locator, minimumCount, "Element count should meet minimum requirement");
    }
    
    /**
     * Assert element is enabled
     */
    public static void assertElementEnabled(By locator, String message) {
        logger.debug("Asserting element is enabled: {}", locator);
        WebElement element = WaitUtils.waitForElementPresent(locator);
        Assert.assertTrue(element.isEnabled(), message + " - Element should be enabled: " + locator);
        logger.debug("Element is enabled: {}", locator);
    }
    
    public static void assertElementEnabled(By locator) {
        assertElementEnabled(locator, "Element should be enabled");
    }
    
    /**
     * Assert element is disabled
     */
    public static void assertElementDisabled(By locator, String message) {
        logger.debug("Asserting element is disabled: {}", locator);
        WebElement element = WaitUtils.waitForElementPresent(locator);
        Assert.assertFalse(element.isEnabled(), message + " - Element should be disabled: " + locator);
        logger.debug("Element is disabled: {}", locator);
    }
    
    public static void assertElementDisabled(By locator) {
        assertElementDisabled(locator, "Element should be disabled");
    }
    
    /**
     * Assert checkbox/radio button is selected
     */
    public static void assertElementSelected(By locator, String message) {
        logger.debug("Asserting element is selected: {}", locator);
        WebElement element = WaitUtils.waitForElementPresent(locator);
        Assert.assertTrue(element.isSelected(), message + " - Element should be selected: " + locator);
        logger.debug("Element is selected: {}", locator);
    }
    
    public static void assertElementSelected(By locator) {
        assertElementSelected(locator, "Element should be selected");
    }
    
    /**
     * Assert checkbox/radio button is not selected
     */
    public static void assertElementNotSelected(By locator, String message) {
        logger.debug("Asserting element is not selected: {}", locator);
        WebElement element = WaitUtils.waitForElementPresent(locator);
        Assert.assertFalse(element.isSelected(), message + " - Element should not be selected: " + locator);
        logger.debug("Element is not selected: {}", locator);
    }
    
    public static void assertElementNotSelected(By locator) {
        assertElementNotSelected(locator, "Element should not be selected");
    }
    
    /**
     * Soft assertion that logs failures but doesn't stop execution
     */
    public static void softAssert(boolean condition, String message) {
        if (!condition) {
            logger.error("Soft assertion failed: {}", message);
        } else {
            logger.debug("Soft assertion passed: {}", message);
        }
    }
    
    /**
     * Assert that a value is within expected range
     */
    public static void assertValueInRange(double actual, double min, double max, String message) {
        logger.debug("Asserting value {} is between {} and {}", actual, min, max);
        Assert.assertTrue(actual >= min && actual <= max, 
            message + " - Value not in expected range. Value: " + actual + ", Range: [" + min + ", " + max + "]");
        logger.debug("Value {} is within expected range [{}, {}]", actual, min, max);
    }
    
    public static void assertValueInRange(double actual, double min, double max) {
        assertValueInRange(actual, min, max, "Value should be within expected range");
    }
    
    /**
     * Assert that actual value is greater than expected value
     */
    public static void assertGreaterThan(int actual, int expected, String message) {
        logger.debug("Asserting {} > {} - {}", actual, expected, message);
        assertThat(actual)
            .as(message)
            .isGreaterThan(expected);
        logger.debug("Assertion passed: {} > {}", actual, expected);
    }
    
    /**
     * Assert that actual value is greater than or equal to expected value
     */
    public static void assertGreaterThanOrEqual(int actual, int expected, String message) {
        logger.debug("Asserting {} >= {} - {}", actual, expected, message);
        assertThat(actual)
            .as(message)
            .isGreaterThanOrEqualTo(expected);
        logger.debug("Assertion passed: {} >= {}", actual, expected);
    }
    
    /**
     * Assert that actual value is less than expected value
     */
    public static void assertLessThan(int actual, int expected, String message) {
        logger.debug("Asserting {} < {} - {}", actual, expected, message);
        assertThat(actual)
            .as(message)
            .isLessThan(expected);
        logger.debug("Assertion passed: {} < {}", actual, expected);
    }
    
    /**
     * Assert that actual value is less than or equal to expected value
     */
    public static void assertLessThanOrEqual(int actual, int expected, String message) {
        logger.debug("Asserting {} <= {} - {}", actual, expected, message);
        assertThat(actual)
            .as(message)
            .isLessThanOrEqualTo(expected);
        logger.debug("Assertion passed: {} <= {}", actual, expected);
    }
    
    /**
     * Assert that condition is true
     */
    public static void assertTrue(boolean condition, String message) {
        logger.debug("Asserting condition is true - {}", message);
        Assert.assertTrue(condition, message);
        logger.debug("Assertion passed: condition is true");
    }
    
    /**
     * Assert that condition is false
     */
    public static void assertFalse(boolean condition, String message) {
        logger.debug("Asserting condition is false - {}", message);
        Assert.assertFalse(condition, message);
        logger.debug("Assertion passed: condition is false");
    }
}