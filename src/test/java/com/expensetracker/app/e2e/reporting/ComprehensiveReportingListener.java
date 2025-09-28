package com.expensetracker.app.e2e.reporting;

import com.expensetracker.app.e2e.config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Comprehensive test reporting listener that orchestrates all reporting systems
 */
public class ComprehensiveReportingListener implements ITestListener, ISuiteListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveReportingListener.class);
    
    private TestExecutionSummary summary;
    private TestMetricsCollector metricsCollector;
    private EmailNotificationService emailService;
    private List<String> failedTestNames;
    
    @Override
    public void onStart(ISuite suite) {
        logger.info("Starting comprehensive test reporting for suite: {}", suite.getName());
        
        // Initialize summary
        summary = new TestExecutionSummary();
        summary.setStartTime(LocalDateTime.now());
        failedTestNames = new ArrayList<>();
        
        // Initialize metrics collector
        TestConfig config = TestConfig.getInstance();
        metricsCollector = new TestMetricsCollector(config.getReportsDir() + "/metrics");
        
        // Initialize email service (if configured)
        initializeEmailService();
        
        // Set environment information
        summary.setEnvironment(System.getProperty("environment", "local"));
        summary.setBrowser(System.getProperty("browser", "chrome"));
        summary.setBuildNumber(System.getProperty("BUILD_NUMBER", "local"));
        summary.setGitBranch(System.getProperty("GIT_BRANCH", "unknown"));
        summary.setGitCommit(System.getProperty("GIT_COMMIT", "unknown"));
    }
    
    @Override
    public void onFinish(ISuite suite) {
        logger.info("Finishing comprehensive test reporting for suite: {}", suite.getName());
        
        summary.setEndTime(LocalDateTime.now());
        summary.setFailedTestNames(failedTestNames);
        
        // Set report paths
        TestConfig config = TestConfig.getInstance();
        summary.setExtentReportPath(config.getReportsDir() + "/ExtentReport.html");
        summary.setAllureReportPath("target/allure-reports/index.html");
        summary.setJacocoReportPath("target/site/jacoco/index.html");
        
        // Generate comprehensive reports
        generateAllReports();
        
        // Send notifications if configured
        sendNotifications();
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        logger.debug("Test started: {}", result.getName());
        summary.setTotalTests(summary.getTotalTests() + 1);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        logger.debug("Test passed: {}", result.getName());
        summary.setPassedTests(summary.getPassedTests() + 1);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: {} - {}", result.getName(), result.getThrowable().getMessage());
        summary.setFailedTests(summary.getFailedTests() + 1);
        failedTestNames.add(result.getName() + ": " + result.getThrowable().getMessage());
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}", result.getName());
        summary.setSkippedTests(summary.getSkippedTests() + 1);
    }
    
    private void initializeEmailService() {
        try {
            Properties reportingConfig = loadReportingConfig();
            
            if ("true".equals(reportingConfig.getProperty("email.notification.enabled", "false"))) {
                String[] toAddresses = reportingConfig.getProperty("email.to.addresses", "").split(",");
                
                emailService = new EmailNotificationService(
                    reportingConfig.getProperty("email.smtp.host"),
                    reportingConfig.getProperty("email.smtp.port"),
                    reportingConfig.getProperty("email.username", ""),
                    reportingConfig.getProperty("email.password", ""),
                    reportingConfig.getProperty("email.from.address"),
                    toAddresses
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize email service", e);
        }
    }
    
    private Properties loadReportingConfig() {
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("reporting-config.properties"));
        } catch (Exception e) {
            logger.warn("Failed to load reporting configuration", e);
        }
        return props;
    }
    
    private void generateAllReports() {
        try {
            // Collect and export test metrics
            metricsCollector.collectExecutionMetrics(summary);
            metricsCollector.exportToCSV();
            metricsCollector.exportToJSON();
            metricsCollector.generateDashboard();
            
            // Generate custom HTML summary report
            generateCustomSummaryReport();
            
            logger.info("All reports generated successfully");
        } catch (Exception e) {
            logger.error("Failed to generate comprehensive reports", e);
        }
    }
    
    private void generateCustomSummaryReport() {
        // Generate a custom HTML summary that links all reports together
        String reportPath = TestConfig.getInstance().getReportsDir() + "/test-summary.html";
        
        try (java.io.FileWriter writer = new java.io.FileWriter(reportPath)) {
            writer.write(getCustomSummaryHTML());
            logger.info("Custom test summary report generated: {}", reportPath);
        } catch (Exception e) {
            logger.error("Failed to generate custom summary report", e);
        }
    }
    
    private String getCustomSummaryHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>Expense Tracker - Test Execution Summary</title>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f7fa; }\n");
        html.append(".container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }\n");
        html.append(".header { text-align: center; color: #2c3e50; margin-bottom: 40px; }\n");
        html.append(".summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 40px; }\n");
        html.append(".summary-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px; text-align: center; }\n");
        html.append(".summary-card.success { background: linear-gradient(135deg, #56ab2f 0%, #a8e6cf 100%); }\n");
        html.append(".summary-card.failure { background: linear-gradient(135deg, #ff416c 0%, #ff4b2b 100%); }\n");
        html.append(".card-value { font-size: 2.5em; font-weight: bold; margin-bottom: 10px; }\n");
        html.append(".card-label { font-size: 0.9em; opacity: 0.9; }\n");
        html.append(".reports-section { margin-top: 40px; }\n");
        html.append(".report-links { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }\n");
        html.append(".report-link { background-color: #ecf0f1; padding: 20px; border-radius: 8px; text-decoration: none; color: #2c3e50; transition: transform 0.3s ease; }\n");
        html.append(".report-link:hover { transform: translateY(-5px); box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1); }\n");
        html.append(".report-title { font-size: 1.2em; font-weight: bold; margin-bottom: 10px; }\n");
        html.append(".report-description { font-size: 0.9em; color: #7f8c8d; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        html.append("<div class='container'>\n");
        html.append("<div class='header'>\n");
        html.append("<h1>Expense Tracker Test Execution Summary</h1>\n");
        html.append("<p>Build #").append(summary.getBuildNumber()).append(" | Branch: ").append(summary.getGitBranch()).append("</p>\n");
        html.append("</div>\n");
        
        // Summary cards
        html.append("<div class='summary-grid'>\n");
        html.append("<div class='summary-card'>\n");
        html.append("<div class='card-value'>").append(summary.getTotalTests()).append("</div>\n");
        html.append("<div class='card-label'>Total Tests</div>\n");
        html.append("</div>\n");
        
        html.append("<div class='summary-card success'>\n");
        html.append("<div class='card-value'>").append(summary.getPassedTests()).append("</div>\n");
        html.append("<div class='card-label'>Passed</div>\n");
        html.append("</div>\n");
        
        html.append("<div class='summary-card failure'>\n");
        html.append("<div class='card-value'>").append(summary.getFailedTests()).append("</div>\n");
        html.append("<div class='card-label'>Failed</div>\n");
        html.append("</div>\n");
        
        html.append("<div class='summary-card'>\n");
        html.append("<div class='card-value'>").append(String.format("%.1f%%", summary.getSuccessRate())).append("</div>\n");
        html.append("<div class='card-label'>Success Rate</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        
        // Report links
        html.append("<div class='reports-section'>\n");
        html.append("<h2>Test Reports</h2>\n");
        html.append("<div class='report-links'>\n");
        
        html.append("<a href='ExtentReport.html' class='report-link'>\n");
        html.append("<div class='report-title'>ExtentReports</div>\n");
        html.append("<div class='report-description'>Detailed test execution report with screenshots and logs</div>\n");
        html.append("</a>\n");
        
        html.append("<a href='../allure-reports/index.html' class='report-link'>\n");
        html.append("<div class='report-title'>Allure Report</div>\n");
        html.append("<div class='report-description'>Advanced test analytics with trends and categories</div>\n");
        html.append("</a>\n");
        
        html.append("<a href='../site/jacoco/index.html' class='report-link'>\n");
        html.append("<div class='report-title'>JaCoCo Coverage</div>\n");
        html.append("<div class='report-description'>Code coverage analysis and metrics</div>\n");
        html.append("</a>\n");
        
        html.append("<a href='metrics/metrics_dashboard.html' class='report-link'>\n");
        html.append("<div class='report-title'>Test Metrics</div>\n");
        html.append("<div class='report-description'>Custom test metrics and KPI dashboard</div>\n");
        html.append("</a>\n");
        
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</body>\n</html>");
        
        return html.toString();
    }
    
    private void sendNotifications() {
        try {
            if (emailService != null && (summary.getFailedTests() > 0 || shouldSendSuccessEmail())) {
                emailService.sendTestExecutionSummary(summary);
            }
        } catch (Exception e) {
            logger.error("Failed to send email notifications", e);
        }
    }
    
    private boolean shouldSendSuccessEmail() {
        // Send success email for scheduled builds or if all tests pass after previous failures
        String buildNumber = summary.getBuildNumber();
        return !"local".equals(buildNumber) && summary.getFailedTests() == 0;
    }
}