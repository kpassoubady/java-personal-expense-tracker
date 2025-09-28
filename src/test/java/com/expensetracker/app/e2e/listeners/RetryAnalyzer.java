package com.expensetracker.app.e2e.listeners;

import com.expensetracker.app.e2e.config.TestConfig;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestNG Retry Analyzer for handling flaky tests
 * Retries failed tests up to a configurable maximum number of attempts
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;
    private int maxRetryCount;
    
    public RetryAnalyzer() {
        TestConfig config = TestConfig.getInstance();
        this.maxRetryCount = config.getMaxRetryCount();
        logger.debug("RetryAnalyzer initialized with max retry count: {}", maxRetryCount);
    }
    
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            
            String testName = result.getMethod().getMethodName();
            String className = result.getTestClass().getName();
            Throwable throwable = result.getThrowable();
            
            logger.warn("Retrying test: {}.{} (Attempt {} of {})", 
                className, testName, retryCount, maxRetryCount);
            
            if (throwable != null) {
                logger.warn("Retry reason: {}", throwable.getMessage());
            }
            
            // Log retry information to ExtentReports if available
            try {
                if (ExtentReportListener.getCurrentTest() != null) {
                    ExtentReportListener.logWarning(
                        String.format("Test failed - Retrying (Attempt %d of %d)", retryCount, maxRetryCount));
                }
            } catch (Exception e) {
                logger.debug("Could not log retry to ExtentReports", e);
            }
            
            return true;
        }
        
        logger.error("Test failed after {} retry attempts: {}.{}", 
            maxRetryCount, result.getTestClass().getName(), result.getMethod().getMethodName());
        
        // Log final failure to ExtentReports if available
        try {
            if (ExtentReportListener.getCurrentTest() != null) {
                ExtentReportListener.logFail(
                    String.format("Test failed permanently after %d retry attempts", maxRetryCount));
            }
        } catch (Exception e) {
            logger.debug("Could not log final failure to ExtentReports", e);
        }
        
        return false;
    }
    
    /**
     * Reset retry count (used for test data providers or manual resets)
     */
    public void resetRetryCount() {
        this.retryCount = 0;
        logger.debug("Retry count reset to 0");
    }
    
    /**
     * Get current retry count
     */
    public int getCurrentRetryCount() {
        return retryCount;
    }
    
    /**
     * Get maximum retry count
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }
    
    /**
     * Set maximum retry count (for dynamic configuration)
     */
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        logger.debug("Max retry count updated to: {}", maxRetryCount);
    }
}