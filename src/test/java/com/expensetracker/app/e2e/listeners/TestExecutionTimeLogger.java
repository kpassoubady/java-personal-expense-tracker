package com.expensetracker.app.e2e.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestExecutionTimeLogger implements ITestListener {
    private long startTime;
    @Override
    public void onTestStart(ITestResult result) {
        startTime = System.currentTimeMillis();
    }
    @Override
    public void onTestSuccess(ITestResult result) {
        logExecutionTime(result);
    }
    @Override
    public void onTestFailure(ITestResult result) {
        logExecutionTime(result);
    }
    private void logExecutionTime(ITestResult result) {
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Test " + result.getName() + " executed in " + duration + " ms");
    }
}
