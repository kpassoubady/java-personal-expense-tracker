package com.expensetracker.app.e2e.reporting;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Allure reporting listener for detailed test analytics
 */
public class AllureReportListener implements ITestListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AllureReportListener.class);
    
    @Override
    public void onTestStart(ITestResult result) {
        logger.debug("Starting test: {}", result.getName());
        
        // Set test information
        Allure.epic("Expense Tracker E2E Tests");
        Allure.feature(getFeatureName(result));
        Allure.story(getStoryName(result));
        
        // Add environment information
        Allure.parameter("Browser", System.getProperty("browser", "chrome"));
        Allure.parameter("Environment", System.getProperty("environment", "local"));
        Allure.parameter("Headless", System.getProperty("headless", "false"));
        
        // Add execution timestamp
        Allure.parameter("Execution Time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}", result.getName());
        
        // Add success information to Allure
        Allure.step("Test completed successfully", () -> {
            long executionTime = result.getEndMillis() - result.getStartMillis();
            Allure.parameter("Execution Duration", executionTime + " ms");
        });
        
        // Attach screenshot for passed tests if enabled
        attachScreenshotIfAvailable(result, "success");
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: {} - {}", result.getName(), result.getThrowable().getMessage());
        
        // Add failure information to Allure
        Allure.step("Test failed with error", () -> {
            Allure.attachment("Error Message", result.getThrowable().getMessage());
            Allure.attachment("Stack Trace", getStackTrace(result.getThrowable()));
        });
        
        // Attach screenshot for failed tests
        attachScreenshotIfAvailable(result, "failure");
        
        // Add failure category
        categorizeFailure(result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}", result.getName());
        
        if (result.getThrowable() != null) {
            Allure.step("Test skipped", () -> {
                Allure.attachment("Skip Reason", result.getThrowable().getMessage());
            });
        }
    }
    
    private String getFeatureName(ITestResult result) {
        String className = result.getTestClass().getName();
        if (className.contains("journey")) {
            return "User Journey Tests";
        } else if (className.contains("smoke")) {
            return "Smoke Tests";
        } else if (className.contains("regression")) {
            return "Regression Tests";
        }
        return "Functional Tests";
    }
    
    private String getStoryName(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        // Convert camelCase to readable format
        return methodName.replaceAll("([A-Z])", " $1").toLowerCase().trim();
    }
    
    private void attachScreenshotIfAvailable(ITestResult result, String type) {
        try {
            // This would integrate with your ScreenshotUtils
            String screenshotPath = getScreenshotPath(result, type);
            if (screenshotPath != null && new File(screenshotPath).exists()) {
                Allure.addAttachment("Screenshot - " + type, "image/png", 
                                   new ByteArrayInputStream(java.nio.file.Files.readAllBytes(new File(screenshotPath).toPath())), 
                                   ".png");
            }
        } catch (Exception e) {
            logger.warn("Failed to attach screenshot to Allure report", e);
        }
    }
    
    private String getScreenshotPath(ITestResult result, String type) {
        // This should integrate with your existing ScreenshotUtils
        // Return the path to the screenshot file
        return null; // Placeholder - implement based on your ScreenshotUtils
    }
    
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        
        if (throwable.getCause() != null) {
            sb.append("Caused by: ").append(getStackTrace(throwable.getCause()));
        }
        
        return sb.toString();
    }
    
    private void categorizeFailure(ITestResult result) {
        String errorMessage = result.getThrowable().getMessage().toLowerCase();
        
        if (errorMessage.contains("timeout") || errorMessage.contains("wait")) {
            Allure.label("defect_type", "Timeout/Wait Issue");
            Allure.label("severity", "major");
        } else if (errorMessage.contains("element not found") || errorMessage.contains("no such element")) {
            Allure.label("defect_type", "Element Locator Issue");
            Allure.label("severity", "critical");
        } else if (errorMessage.contains("assertion") || errorMessage.contains("expected")) {
            Allure.label("defect_type", "Assertion Failure");
            Allure.label("severity", "normal");
        } else if (errorMessage.contains("connection") || errorMessage.contains("network")) {
            Allure.label("defect_type", "Network/Connection Issue");
            Allure.label("severity", "critical");
        } else {
            Allure.label("defect_type", "Unknown");
            Allure.label("severity", "normal");
        }
    }
}