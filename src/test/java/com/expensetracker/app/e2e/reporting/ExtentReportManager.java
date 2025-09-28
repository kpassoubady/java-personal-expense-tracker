package com.expensetracker.app.e2e.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.expensetracker.app.e2e.config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced ExtentReports configuration with custom styling and advanced features
 */
public class ExtentReportManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ExtentReportManager.class);
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    
    /**
     * Initialize ExtentReports with enhanced configuration
     */
    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            createInstance();
        }
        return extentReports;
    }
    
    private static void createInstance() {
        TestConfig config = TestConfig.getInstance();
        String reportsDir = config.getReportsDir();
        
        // Ensure reports directory exists
        File dir = new File(reportsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String reportPath = reportsDir + "/ExtentReport_" + timestamp + ".html";
        
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        configureSparkReporter(sparkReporter);
        
        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        setSystemInfo(config);
        
        logger.info("ExtentReports initialized: {}", reportPath);
    }
    
    private static void configureSparkReporter(ExtentSparkReporter sparkReporter) {
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Expense Tracker - E2E Test Report");
        sparkReporter.config().setReportName("Personal Expense Tracker Test Execution Report");
        sparkReporter.config().setEncoding("UTF-8");
        sparkReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
        
        // Custom CSS for enhanced styling
        String customCSS = """
            .brand-logo { 
                background-color: #3498db !important; 
                color: white !important;
            }
            .test-item.pass { 
                border-left: 5px solid #27ae60 !important; 
            }
            .test-item.fail { 
                border-left: 5px solid #e74c3c !important; 
            }
            .test-item.skip { 
                border-left: 5px solid #f39c12 !important; 
            }
            .dashboard-view .card { 
                box-shadow: 0 4px 8px rgba(0,0,0,0.1) !important; 
            }
            .category-summary .category-item { 
                transition: all 0.3s ease !important; 
            }
            .category-summary .category-item:hover { 
                transform: translateY(-2px) !important; 
            }
            """;
        
        sparkReporter.config().setCss(customCSS);
        
        // Custom JavaScript for enhanced functionality
        String customJS = """
            document.addEventListener('DOMContentLoaded', function() {
                // Add custom brand logo text
                var brandLogo = document.querySelector('.brand-logo');
                if (brandLogo) {
                    brandLogo.innerHTML = 'Expense Tracker Test Report';
                }
                
                // Add test execution summary tooltip
                var summaryCards = document.querySelectorAll('.card-panel');
                summaryCards.forEach(function(card) {
                    card.setAttribute('title', 'Click for detailed view');
                });
            });
            """;
        
        sparkReporter.config().setJs(customJS);
    }
    
    private static void setSystemInfo(TestConfig config) {
        extentReports.setSystemInfo("Application", "Personal Expense Tracker");
        extentReports.setSystemInfo("Environment", config.getEnvironment());
        extentReports.setSystemInfo("Base URL", config.getBaseUrl());
        extentReports.setSystemInfo("Browser", config.getBrowser());
        extentReports.setSystemInfo("Headless Mode", String.valueOf(config.isHeadless()));
        extentReports.setSystemInfo("Parallel Execution", System.getProperty("parallelCount", "1"));
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
        extentReports.setSystemInfo("User", System.getProperty("user.name"));
        extentReports.setSystemInfo("Test Execution Time", LocalDateTime.now().toString());
        
        // CI/CD Information
        extentReports.setSystemInfo("Build Number", System.getProperty("BUILD_NUMBER", "local"));
        extentReports.setSystemInfo("Build URL", System.getProperty("BUILD_URL", "local"));
        extentReports.setSystemInfo("Git Branch", System.getProperty("GIT_BRANCH", "local"));
        extentReports.setSystemInfo("Git Commit", System.getProperty("GIT_COMMIT", "local"));
    }
    
    public static void setTest(ExtentTest test) {
        extentTest.set(test);
    }
    
    public static ExtentTest getTest() {
        return extentTest.get();
    }
    
    public static void removeTest() {
        extentTest.remove();
    }
    
    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}