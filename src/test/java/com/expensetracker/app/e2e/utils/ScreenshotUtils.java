package com.expensetracker.app.e2e.utils;

import com.expensetracker.app.e2e.config.TestConfig;
import com.expensetracker.app.e2e.config.WebDriverConfig;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for capturing and managing screenshots
 * Supports various screenshot types and automatic file naming
 */
public class ScreenshotUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String SCREENSHOT_EXTENSION = ".png";
    
    /**
     * Capture screenshot with automatic naming
     */
    public static String captureScreenshot(String testName) {
        return captureScreenshot(testName, "");
    }
    
    /**
     * Capture screenshot with custom suffix
     */
    public static String captureScreenshot(String testName, String suffix) {
        if (!WebDriverConfig.isDriverInitialized()) {
            logger.warn("WebDriver not initialized, cannot capture screenshot");
            return null;
        }
        
        try {
            WebDriver driver = WebDriverConfig.getDriver();
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            
            // Generate filename
            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            String browser = WebDriverConfig.getBrowserName();
            String filename = generateScreenshotFilename(testName, suffix, browser, timestamp);
            
            // Create screenshots directory if it doesn't exist
            String screenshotsDir = TestConfig.getInstance().getScreenshotsDir();
            Path screenshotPath = Paths.get(screenshotsDir, filename);
            Files.createDirectories(screenshotPath.getParent());
            
            // Capture and save screenshot
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            File destFile = screenshotPath.toFile();
            
            FileUtils.copyFile(sourceFile, destFile);
            
            String absolutePath = destFile.getAbsolutePath();
            logger.info("Screenshot captured: {}", absolutePath);
            
            return absolutePath;
            
        } catch (IOException e) {
            logger.error("Failed to capture screenshot for test: {}", testName, e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while capturing screenshot: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Capture screenshot for test failure
     */
    public static String captureFailureScreenshot(String testName, Throwable throwable) {
        String screenshotPath = captureScreenshot(testName, "failure");
        
        if (screenshotPath != null) {
            // Log additional failure information
            logger.error("Test failed: {} - Screenshot: {}", testName, screenshotPath);
            if (throwable != null) {
                logger.error("Failure cause: {}", throwable.getMessage());
            }
        }
        
        return screenshotPath;
    }
    
    /**
     * Capture screenshot for test step
     */
    public static String captureStepScreenshot(String testName, String stepName) {
        return captureScreenshot(testName, "step_" + sanitizeFilename(stepName));
    }
    
    /**
     * Capture screenshot with custom directory
     */
    public static String captureScreenshot(String testName, String suffix, String customDir) {
        if (!WebDriverConfig.isDriverInitialized()) {
            logger.warn("WebDriver not initialized, cannot capture screenshot");
            return null;
        }
        
        try {
            WebDriver driver = WebDriverConfig.getDriver();
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            
            // Generate filename
            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            String browser = WebDriverConfig.getBrowserName();
            String filename = generateScreenshotFilename(testName, suffix, browser, timestamp);
            
            // Create custom directory if it doesn't exist
            Path screenshotPath = Paths.get(customDir, filename);
            Files.createDirectories(screenshotPath.getParent());
            
            // Capture and save screenshot
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            File destFile = screenshotPath.toFile();
            
            FileUtils.copyFile(sourceFile, destFile);
            
            String absolutePath = destFile.getAbsolutePath();
            logger.info("Screenshot captured in custom directory: {}", absolutePath);
            
            return absolutePath;
            
        } catch (IOException e) {
            logger.error("Failed to capture screenshot in custom directory for test: {}", testName, e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while capturing screenshot: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Generate screenshot filename with consistent naming convention
     */
    private static String generateScreenshotFilename(String testName, String suffix, String browser, String timestamp) {
        StringBuilder filename = new StringBuilder();
        
        // Add test name
        filename.append(sanitizeFilename(testName));
        
        // Add suffix if provided
        if (suffix != null && !suffix.isEmpty()) {
            filename.append("_").append(sanitizeFilename(suffix));
        }
        
        // Add browser
        filename.append("_").append(browser);
        
        // Add timestamp
        filename.append("_").append(timestamp);
        
        // Add extension
        filename.append(SCREENSHOT_EXTENSION);
        
        return filename.toString();
    }
    
    /**
     * Sanitize filename by removing/replacing invalid characters
     */
    private static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        
        // Replace invalid characters with underscore
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_") // Replace multiple underscores with single
                      .toLowerCase();
    }
    
    /**
     * Get screenshot file size in a human-readable format
     */
    public static String getScreenshotFileSize(String screenshotPath) {
        if (screenshotPath == null) {
            return "Unknown";
        }
        
        try {
            File file = new File(screenshotPath);
            if (!file.exists()) {
                return "File not found";
            }
            
            long bytes = file.length();
            if (bytes < 1024) {
                return bytes + " B";
            } else if (bytes < 1024 * 1024) {
                return String.format("%.1f KB", bytes / 1024.0);
            } else {
                return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            }
            
        } catch (Exception e) {
            logger.error("Error getting file size for screenshot: {}", screenshotPath, e);
            return "Error";
        }
    }
    
    /**
     * Clean up old screenshots (older than specified days)
     */
    public static void cleanupOldScreenshots(int daysOld) {
        try {
            String screenshotsDir = TestConfig.getInstance().getScreenshotsDir();
            Path screenshotsPath = Paths.get(screenshotsDir);
            
            if (!Files.exists(screenshotsPath)) {
                logger.info("Screenshots directory does not exist: {}", screenshotsDir);
                return;
            }
            
            long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L);
            
            Files.walk(screenshotsPath)
                 .filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(SCREENSHOT_EXTENSION))
                 .filter(path -> {
                     try {
                         return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                     } catch (IOException e) {
                         return false;
                     }
                 })
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                         logger.debug("Deleted old screenshot: {}", path);
                     } catch (IOException e) {
                         logger.warn("Could not delete old screenshot: {}", path, e);
                     }
                 });
            
            logger.info("Cleaned up screenshots older than {} days", daysOld);
            
        } catch (Exception e) {
            logger.error("Error during screenshot cleanup", e);
        }
    }
    
    /**
     * Get relative path for screenshot (useful for HTML reports)
     */
    public static String getRelativeScreenshotPath(String absolutePath) {
        if (absolutePath == null) {
            return null;
        }
        
        try {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            Path screenshotPath = Paths.get(absolutePath);
            return currentDir.relativize(screenshotPath).toString();
        } catch (Exception e) {
            logger.warn("Could not get relative path for screenshot: {}", absolutePath);
            return absolutePath;
        }
    }
}