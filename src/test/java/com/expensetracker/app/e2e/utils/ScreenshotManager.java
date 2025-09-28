package com.expensetracker.app.e2e.utils;

public class ScreenshotManager {
    public static String capture(String testName) {
        return ScreenshotUtils.captureScreenshot(testName);
    }
    
    public static String captureWithSuffix(String testName, String suffix) {
        return ScreenshotUtils.captureScreenshot(testName, suffix);
    }
}
