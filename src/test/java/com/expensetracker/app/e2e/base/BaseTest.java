package com.expensetracker.app.e2e.base;

import com.expensetracker.app.e2e.config.TestConfig;
import com.expensetracker.app.e2e.config.WebDriverConfig;
import com.expensetracker.app.e2e.utils.ScreenshotUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base test class for all E2E tests
 * Provides WebDriver setup, teardown, and common utilities
 */
public abstract class BaseTest {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected TestConfig config;
    protected WebDriver driver; // Expose driver to subclasses
    
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        logger.info("=== Starting E2E Test Suite ===");
        config = TestConfig.getInstance();
        logger.info("\n{}", config.getAllProperties());
        
        // Create necessary directories
        createTestDirectories();
        
        // Initialize any suite-level resources
        initializeSuiteResources();
    }
    
    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        logger.info("Setting up test class: {}", this.getClass().getSimpleName());
    }
    
    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "headless", "environment"})
    public void beforeMethod(
            @Optional("chrome") String browser,
            @Optional("false") String headless,
            @Optional("local") String environment,
            ITestResult result) {
        
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        
        logger.info("Starting test: {}.{}", className, testName);
        
        // Initialize WebDriver
        boolean isHeadless = Boolean.parseBoolean(headless) || config.isHeadless();
        WebDriverConfig.initializeDriver(browser, isHeadless);
        this.driver = WebDriverConfig.getDriver(); // Assign to instance variable
        
        // Navigate to base URL if not a navigation test
        if (!testName.contains("Navigation") && !testName.contains("Load")) {
            String baseUrl = config.getBaseUrl();
            logger.info("Navigating to: {}", baseUrl);
            driver.get(baseUrl);
            
            // Wait for page load
            waitForPageLoad();
        }
    }
    
    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        
        // Take screenshot on failure
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("Test failed: {}.{}", className, testName);
            takeScreenshotOnFailure(testName, result.getThrowable());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("Test passed: {}.{}", className, testName);
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.warn("Test skipped: {}.{}", className, testName);
        }
        
        // Cleanup WebDriver
        try {
            WebDriverConfig.quitDriver();
            this.driver = null;
        } catch (Exception e) {
            logger.warn("Error during WebDriver cleanup", e);
        }
        
        logger.info("Finished test: {}.{}", className, testName);
    }
    
    @AfterClass(alwaysRun = true)
    public void afterClass() {
        logger.info("Tearing down test class: {}", this.getClass().getSimpleName());
    }
    
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        logger.info("=== E2E Test Suite Completed ===");
        
        // Cleanup old test artifacts
        cleanupOldTestArtifacts();
        
        // Log suite completion
        logger.info("E2E Test Suite execution completed successfully");
    }
    
    /**
     * Wait for page to load completely
     */
    protected void waitForPageLoad() {
        try {
            Thread.sleep(2000); // Basic wait - can be enhanced with WebDriverWait
            logger.debug("Page load wait completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Page load wait interrupted", e);
        }
    }
    
    /**
     * Take screenshot on test failure
     */
    private void takeScreenshotOnFailure(String testName, Throwable throwable) {
        try {
            String screenshotPath = ScreenshotUtils.captureFailureScreenshot(testName, throwable);
            logger.info("Screenshot captured for failed test: {}", screenshotPath);
        } catch (Exception e) {
            logger.warn("Could not capture screenshot for failed test: {}", testName, e);
        }
    }
    
    /**
     * Create necessary test directories
     */
    private void createTestDirectories() {
        try {
            String[] directories = {
                config.getScreenshotsDir(),
                config.getReportsDir(),
                config.getDownloadsDir()
            };
            
            for (String dir : directories) {
                Path path = java.nio.file.Paths.get(dir);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                    logger.debug("Created directory: {}", dir);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to create test directories", e);
        }
    }
    
    /**
     * Initialize suite-level resources
     */
    private void initializeSuiteResources() {
        logger.debug("Initializing suite-level resources");
        
        // Add any suite-level initialization here
        // e.g., database setup, test data preparation, etc.
        
        logger.debug("Suite-level resources initialized");
    }
    
    /**
     * Cleanup old test artifacts
     */
    private void cleanupOldTestArtifacts() {
        try {
            logger.debug("Cleaning up old test artifacts");
            ScreenshotUtils.cleanupOldScreenshots(7); // Keep last 7 days
            logger.debug("Old test artifacts cleanup completed");
        } catch (Exception e) {
            logger.warn("Error during test artifacts cleanup", e);
        }
    }
    
    /**
     * Get current WebDriver instance
     */
    protected WebDriver getDriver() {
        return this.driver;
    }
    
    /**
     * Get test configuration
     */
    protected TestConfig getConfig() {
        return this.config;
    }
}