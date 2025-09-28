package com.expensetracker.app.e2e.reporting;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data class to hold test execution summary information
 */
public class TestExecutionSummary {
    
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int skippedTests;

    private String executionTime;
    private String environment;
    private String browser;
    private String buildNumber;
    private String gitBranch;
    private String gitCommit;
    private List<String> failedTestNames;
    private String extentReportPath;
    private String allureReportPath;
    private String jacocoReportPath;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public TestExecutionSummary() {}
    
    // Getters and setters
    public int getTotalTests() {
        return totalTests;
    }
    
    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }
    
    public int getPassedTests() {
        return passedTests;
    }
    
    public void setPassedTests(int passedTests) {
        this.passedTests = passedTests;
    }
    
    public int getFailedTests() {
        return failedTests;
    }
    
    public void setFailedTests(int failedTests) {
        this.failedTests = failedTests;
    }
    
    public int getSkippedTests() {
        return skippedTests;
    }
    
    public void setSkippedTests(int skippedTests) {
        this.skippedTests = skippedTests;
    }
    
    public double getSuccessRate() {
        if (totalTests == 0) return 0.0;
        return (double) passedTests / totalTests * 100.0;
    }
    
    public String getExecutionTime() {
        if (startTime != null && endTime != null) {
            Duration duration = Duration.between(startTime, endTime);
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return executionTime != null ? executionTime : "Unknown";
    }
    
    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }
    
    public String getEnvironment() {
        return environment != null ? environment : "Unknown";
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public String getBrowser() {
        return browser != null ? browser : "Unknown";
    }
    
    public void setBrowser(String browser) {
        this.browser = browser;
    }
    
    public String getBuildNumber() {
        return buildNumber != null ? buildNumber : "local";
    }
    
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }
    
    public String getGitBranch() {
        return gitBranch != null ? gitBranch : "unknown";
    }
    
    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }
    
    public String getGitCommit() {
        return gitCommit != null ? gitCommit : "unknown";
    }
    
    public void setGitCommit(String gitCommit) {
        this.gitCommit = gitCommit;
    }
    
    public List<String> getFailedTestNames() {
        return failedTestNames;
    }
    
    public void setFailedTestNames(List<String> failedTestNames) {
        this.failedTestNames = failedTestNames;
    }
    
    public String getExtentReportPath() {
        return extentReportPath != null ? extentReportPath : "#";
    }
    
    public void setExtentReportPath(String extentReportPath) {
        this.extentReportPath = extentReportPath;
    }
    
    public String getAllureReportPath() {
        return allureReportPath != null ? allureReportPath : "#";
    }
    
    public void setAllureReportPath(String allureReportPath) {
        this.allureReportPath = allureReportPath;
    }
    
    public String getJacocoReportPath() {
        return jacocoReportPath != null ? jacocoReportPath : "#";
    }
    
    public void setJacocoReportPath(String jacocoReportPath) {
        this.jacocoReportPath = jacocoReportPath;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    @Override
    public String toString() {
        return String.format("TestExecutionSummary{totalTests=%d, passedTests=%d, failedTests=%d, skippedTests=%d, successRate=%.2f%%}", 
                           totalTests, passedTests, failedTests, skippedTests, getSuccessRate());
    }
}