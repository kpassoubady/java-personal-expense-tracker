package com.expensetracker.app.e2e.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.expensetracker.app.e2e.config.TestConfig;
import com.expensetracker.app.e2e.utils.ScreenshotUtils;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ExtentReports listener for comprehensive test reporting
 * Implements ITestListener to capture test execution events
 */
public class ExtentReportListener implements ITestListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ExtentReportListener.class);
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private TestConfig config;
    
    @Override
    public void onStart(org.testng.ITestContext context) {
        logger.info("Starting ExtentReports for test suite: {}", context.getName());
        config = TestConfig.getInstance();
        initializeExtentReports(context);
    }
    
    /**
     * Initialize ExtentReports with configuration
     */
    private void initializeExtentReports(org.testng.ITestContext context) {
        try {
            // Create reports directory
            String reportsDir = config.getReportsDir();
            File dir = new File(reportsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Generate report filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String reportPath = reportsDir + File.separator + "ExtentReport_" + timestamp + ".html";
            
            // Configure ExtentSparkReporter
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            configureSparkReporter(sparkReporter, context);
            
            // Initialize ExtentReports
            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            setSystemInfo();
            
            logger.info("ExtentReports initialized successfully. Report path: {}", reportPath);
        } catch (Exception e) {
            logger.error("Failed to initialize ExtentReports", e);
        }
    }
    
    /**
     * Configure Spark Reporter settings
     */
    private void configureSparkReporter(ExtentSparkReporter sparkReporter, org.testng.ITestContext context) {
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Expense Tracker - Test Automation Report");
        sparkReporter.config().setReportName("E2E Test Execution Report");
        sparkReporter.config().setEncoding("UTF-8");
        sparkReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
        
        // Additional configuration
        sparkReporter.config().setJs("document.getElementsByClassName('brand-logo')[0].innerHTML='Expense Tracker Test Report';");
        sparkReporter.config().setCss(".brand-logo { background-color: #3498db; }");
    }
    
    /**
     * Set system information in the report
     */
    private void setSystemInfo() {
        extentReports.setSystemInfo("Application", "Personal Expense Tracker");
        extentReports.setSystemInfo("Environment", config.getEnvironment());
        extentReports.setSystemInfo("Base URL", config.getBaseUrl());
        extentReports.setSystemInfo("Browser", config.getBrowser());
        extentReports.setSystemInfo("Headless Mode", String.valueOf(config.isHeadless()));
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
        extentReports.setSystemInfo("User", System.getProperty("user.name"));
        extentReports.setSystemInfo("Timezone", System.getProperty("user.timezone"));
        extentReports.setSystemInfo("Test Execution Time", LocalDateTime.now().toString());
        
        // Additional browser information
        if (config.isHeadless()) {
            extentReports.setSystemInfo("Browser Mode", "Headless");
        } else {
            extentReports.setSystemInfo("Browser Mode", "UI");
        }
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Starting test: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        String description = getTestDescription(result);
        
        ExtentTest test = extentReports.createTest(testName)
                .assignCategory(className)
                .assignAuthor("Automation Team");
        
        if (!description.isEmpty()) {
            test.info(description);
        }
        
        // Add test parameters if any
        Object[] parameters = result.getParameters();
        if (parameters.length > 0) {
            test.info("Test Parameters: " + java.util.Arrays.toString(parameters));
        }
        
        extentTest.set(test);
        logger.debug("ExtentTest created for: {}", testName);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test PASSED: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.pass(MarkupHelper.createLabel("Test PASSED", ExtentColor.GREEN));
            
            // Add execution time
            long executionTime = result.getEndMillis() - result.getStartMillis();
            test.info("Execution Time: " + executionTime + " ms");
            
            logger.debug("Test success logged in ExtentReports");
        }
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test FAILED: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        if (test != null) {
            // Log failure
            test.fail(MarkupHelper.createLabel("Test FAILED", ExtentColor.RED));
            
            // Log exception details
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                test.fail("Error: " + throwable.getMessage());
                test.fail("Stack Trace: " + getStackTrace(throwable));
            }
            
            // Capture and attach screenshot
            try {
                String screenshotPath = ScreenshotUtils.captureFailureScreenshot(result.getMethod().getMethodName(), throwable);
                if (screenshotPath != null && !screenshotPath.isEmpty()) {
                    test.addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
                    logger.info("Screenshot attached to report: {}", screenshotPath);
                }
            } catch (Exception e) {
                logger.warn("Could not capture screenshot for failed test", e);
                test.warning("Could not capture screenshot: " + e.getMessage());
            }
            
            // Add execution time
            long executionTime = result.getEndMillis() - result.getStartMillis();
            test.info("Execution Time: " + executionTime + " ms");
            
            logger.debug("Test failure logged in ExtentReports with screenshot");
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test SKIPPED: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.skip(MarkupHelper.createLabel("Test SKIPPED", ExtentColor.ORANGE));
            
            // Log skip reason if available
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                test.skip("Reason: " + throwable.getMessage());
            }
            
            logger.debug("Test skip logged in ExtentReports");
        }
    }
    
    @Override
    public void onFinish(org.testng.ITestContext context) {
        logger.info("Finishing ExtentReports for test suite: {}", context.getName());
        
        if (extentReports != null) {
            // Add suite-level information
            int totalTests = context.getAllTestMethods().length;
            int passedTests = context.getPassedTests().size();
            int failedTests = context.getFailedTests().size();
            int skippedTests = context.getSkippedTests().size();
            
            logger.info("Test Suite Summary - Total: {}, Passed: {}, Failed: {}, Skipped: {}", 
                totalTests, passedTests, failedTests, skippedTests);
            
            // Flush reports
            extentReports.flush();
            logger.info("ExtentReports flushed successfully");
        }
        
        // Clean up ThreadLocal
        extentTest.remove();
    }
    
    /**
     * Get test description from annotations or method name
     */
    private String getTestDescription(ITestResult result) {
        try {
            org.testng.annotations.Test testAnnotation = result.getMethod()
                .getConstructorOrMethod()
                .getMethod()
                .getAnnotation(org.testng.annotations.Test.class);
            
            if (testAnnotation != null && !testAnnotation.description().isEmpty()) {
                return testAnnotation.description();
            }
        } catch (Exception e) {
            logger.debug("Could not get test description from annotation", e);
        }
        
        return "Test: " + result.getMethod().getMethodName();
    }
    
    /**
     * Get formatted stack trace
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder stackTrace = new StringBuilder();
        stackTrace.append(throwable.toString()).append("\n");
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace.append("\tat ").append(element.toString()).append("\n");
        }
        
        return stackTrace.toString();
    }
    
    /**
     * Get current ExtentTest instance (for manual logging)
     */
    public static ExtentTest getCurrentTest() {
        return extentTest.get();
    }
    
    /**
     * Log info message to current test
     */
    public static void logInfo(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.info(message);
        }
    }
    
    /**
     * Log warning message to current test
     */
    public static void logWarning(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.warning(message);
        }
    }
    
    /**
     * Log pass message to current test
     */
    public static void logPass(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.pass(message);
        }
    }
    
    /**
     * Log fail message to current test
     */
    public static void logFail(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.fail(message);
        }
    }
    
    /**
     * Add screenshot to current test
     */
    public static void addScreenshot(String screenshotPath, String title) {
        ExtentTest test = extentTest.get();
        if (test != null && screenshotPath != null) {
            try {
                test.addScreenCaptureFromPath(screenshotPath, title);
            } catch (Exception e) {
                logger.warn("Could not add screenshot to report", e);
            }
        }
    }
}