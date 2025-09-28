package com.expensetracker.app.e2e.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * WebDriver configuration and management
 * Handles browser setup, options, and driver lifecycle
 */
public class WebDriverConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(WebDriverConfig.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();
    
    // Configuration constants
    private static final int DEFAULT_TIMEOUT = 30;
    private static final int DEFAULT_POLL_TIMEOUT = 500;
    
    /**
     * Initialize WebDriver based on browser type
     */
    public static void initializeDriver(String browser, boolean headless) {
        logger.info("Initializing WebDriver for browser: {} (headless: {})", browser, headless);
        
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "chrome":
                driver = createChromeDriver(headless);
                break;
            case "firefox":
                driver = createFirefoxDriver(headless);
                break;
            default:
                logger.warn("Unsupported browser '{}', defaulting to Chrome", browser);
                driver = createChromeDriver(headless);
        }
        
        // Configure driver settings
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().window().maximize();
        
        // Set up WebDriverWait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        wait.pollingEvery(Duration.ofMillis(DEFAULT_POLL_TIMEOUT));
        
        driverThreadLocal.set(driver);
        waitThreadLocal.set(wait);
        
        logger.info("WebDriver initialized successfully: {}", driver.getClass().getSimpleName());
    }
    
    /**
     * Create Chrome WebDriver with options
     */
    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        // Performance and stability options
        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--disable-extensions",
            "--disable-plugins",
            "--disable-images",
            "--disable-background-timer-throttling",
            "--disable-backgrounding-occluded-windows",
            "--disable-renderer-backgrounding",
            "--window-size=1920,1080"
        );
        
        // Set download directory
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", System.getProperty("user.dir") + "/target/downloads");
        prefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", prefs);
        
        return new ChromeDriver(options);
    }
    
    /**
     * Create Firefox WebDriver with options
     */
    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        
        FirefoxOptions options = new FirefoxOptions();
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        // Performance options
        options.addArguments("--window-size=1920,1080");
        
        // Set download preferences
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.dir", System.getProperty("user.dir") + "/target/downloads");
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf,text/csv,application/excel");
        
        return new FirefoxDriver(options);
    }
    
    /**
     * Get current WebDriver instance
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialized. Call initializeDriver() first.");
        }
        return driver;
    }
    
    /**
     * Get current WebDriverWait instance
     */
    public static WebDriverWait getWait() {
        WebDriverWait wait = waitThreadLocal.get();
        if (wait == null) {
            throw new IllegalStateException("WebDriverWait not initialized. Call initializeDriver() first.");
        }
        return wait;
    }
    
    /**
     * Create a custom WebDriverWait with specific timeout
     */
    public static WebDriverWait getWait(int timeoutSeconds) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
    }
    
    /**
     * Quit WebDriver and clean up resources
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver quit successfully");
            } catch (Exception e) {
                logger.error("Error while quitting WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
                waitThreadLocal.remove();
            }
        }
    }
    
    /**
     * Check if WebDriver is initialized
     */
    public static boolean isDriverInitialized() {
        return driverThreadLocal.get() != null;
    }
    
    /**
     * Get browser name from WebDriver
     */
    public static String getBrowserName() {
        if (!isDriverInitialized()) {
            return "unknown";
        }
        
        WebDriver driver = getDriver();
        String browserName = driver.getClass().getSimpleName().toLowerCase();
        
        if (browserName.contains("chrome")) {
            return "chrome";
        } else if (browserName.contains("firefox")) {
            return "firefox";
        } else {
            return browserName;
        }
    }
}