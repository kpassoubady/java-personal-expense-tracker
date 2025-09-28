package com.expensetracker.app.e2e.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;
import com.expensetracker.app.e2e.utils.ScreenshotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenshotOnFailureListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotOnFailureListener.class);
    
    @Override
    public void onTestFailure(ITestResult result) {
        try {
            String testName = result.getMethod().getMethodName();
            logger.info("Capturing screenshot for failed test: {}", testName);
            ScreenshotUtils.captureFailureScreenshot(testName, result.getThrowable());
        } catch (Exception e) {
            logger.warn("Failed to capture screenshot on test failure", e);
        }
    }
}
