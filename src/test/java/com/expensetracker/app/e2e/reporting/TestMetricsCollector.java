package com.expensetracker.app.e2e.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Test metrics and KPI tracking service
 */
public class TestMetricsCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(TestMetricsCollector.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private Map<String, Object> metrics;
    private String metricsDirectory;
    
    public TestMetricsCollector(String metricsDirectory) {
        this.metricsDirectory = metricsDirectory;
        this.metrics = new HashMap<>();
        initializeMetrics();
    }
    
    private void initializeMetrics() {
        // Ensure metrics directory exists
        File dir = new File(metricsDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Collect test execution metrics
     */
    public void collectExecutionMetrics(TestExecutionSummary summary) {
        metrics.clear();
        
        // Basic test metrics
        metrics.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        metrics.put("total_tests", summary.getTotalTests());
        metrics.put("passed_tests", summary.getPassedTests());
        metrics.put("failed_tests", summary.getFailedTests());
        metrics.put("skipped_tests", summary.getSkippedTests());
        metrics.put("success_rate", summary.getSuccessRate());
        metrics.put("execution_time", summary.getExecutionTime());
        
        // Environment metrics
        metrics.put("environment", summary.getEnvironment());
        metrics.put("browser", summary.getBrowser());
        metrics.put("build_number", summary.getBuildNumber());
        metrics.put("git_branch", summary.getGitBranch());
        metrics.put("git_commit", summary.getGitCommit());
        
        // Performance metrics
        calculatePerformanceMetrics(summary);
        
        // Quality metrics
        calculateQualityMetrics(summary);
    }
    
    private void calculatePerformanceMetrics(TestExecutionSummary summary) {
        if (summary.getTotalTests() > 0) {
            // Calculate average test execution time (if available)
            String executionTime = summary.getExecutionTime();
            if (executionTime != null && !executionTime.equals("Unknown")) {
                try {
                    String[] parts = executionTime.split(":");
                    int totalSeconds = Integer.parseInt(parts[0]) * 3600 + 
                                     Integer.parseInt(parts[1]) * 60 + 
                                     Integer.parseInt(parts[2]);
                    double avgTestTime = (double) totalSeconds / summary.getTotalTests();
                    metrics.put("avg_test_execution_time_seconds", avgTestTime);
                } catch (Exception e) {
                    logger.warn("Failed to calculate average test execution time", e);
                }
            }
        }
    }
    
    private void calculateQualityMetrics(TestExecutionSummary summary) {
        // Test stability metric (lower is better)
        double instabilityRate = summary.getTotalTests() > 0 ? 
            (double) summary.getFailedTests() / summary.getTotalTests() * 100 : 0;
        metrics.put("instability_rate", instabilityRate);
        
        // Test coverage effectiveness
        double coverageEffectiveness = summary.getTotalTests() > 0 ? 
            (double) summary.getPassedTests() / summary.getTotalTests() * 100 : 0;
        metrics.put("coverage_effectiveness", coverageEffectiveness);
        
        // Quality score (composite metric)
        double qualityScore = calculateQualityScore(summary);
        metrics.put("quality_score", qualityScore);
    }
    
    private double calculateQualityScore(TestExecutionSummary summary) {
        if (summary.getTotalTests() == 0) return 0.0;
        
        // Quality score based on multiple factors
        double successWeight = 0.6;
        double stabilityWeight = 0.3;
        double coverageWeight = 0.1;
        
        double successScore = summary.getSuccessRate();
        double stabilityScore = 100 - ((double) summary.getFailedTests() / summary.getTotalTests() * 100);
        double coverageScore = summary.getTotalTests() >= 10 ? 100 : (summary.getTotalTests() * 10); // Assume minimum 10 tests for full coverage
        
        return (successScore * successWeight) + 
               (stabilityScore * stabilityWeight) + 
               (coverageScore * coverageWeight);
    }
    
    /**
     * Export metrics to CSV format
     */
    public void exportToCSV() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = metricsDirectory + "/test_metrics_" + timestamp + ".csv";
        
        try (FileWriter writer = new FileWriter(filename)) {
            // Write header
            writer.write("metric,value\n");
            
            // Write metrics
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                writer.write(String.format("%s,%s\n", entry.getKey(), entry.getValue()));
            }
            
            logger.info("Test metrics exported to CSV: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to export metrics to CSV", e);
        }
    }
    
    /**
     * Export metrics to JSON format
     */
    public void exportToJSON() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = metricsDirectory + "/test_metrics_" + timestamp + ".json";
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("{\n");
            
            int count = 0;
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                if (count > 0) writer.write(",\n");
                
                if (entry.getValue() instanceof String) {
                    writer.write(String.format("  \"%s\": \"%s\"", entry.getKey(), entry.getValue()));
                } else {
                    writer.write(String.format("  \"%s\": %s", entry.getKey(), entry.getValue()));
                }
                count++;
            }
            
            writer.write("\n}");
            logger.info("Test metrics exported to JSON: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to export metrics to JSON", e);
        }
    }
    
    /**
     * Get current metrics
     */
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }
    
    /**
     * Generate metrics dashboard HTML
     */
    public void generateDashboard() {
        String filename = metricsDirectory + "/metrics_dashboard.html";
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(getDashboardHTML());
            logger.info("Test metrics dashboard generated: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to generate metrics dashboard", e);
        }
    }
    
    private String getDashboardHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>Test Metrics Dashboard</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append(".container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append(".header { text-align: center; color: #3498db; margin-bottom: 30px; }\n");
        html.append(".metric-card { display: inline-block; width: 200px; margin: 10px; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 10px; text-align: center; }\n");
        html.append(".metric-value { font-size: 2em; font-weight: bold; }\n");
        html.append(".metric-label { font-size: 0.9em; margin-top: 10px; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<div class='container'>\n");
        html.append("<h1 class='header'>Test Metrics Dashboard</h1>\n");
        
        // Add metric cards
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            String label = entry.getKey().replace("_", " ").toUpperCase();
            html.append(String.format("<div class='metric-card'>\n<div class='metric-value'>%s</div>\n<div class='metric-label'>%s</div>\n</div>\n", 
                                    entry.getValue(), label));
        }
        
        html.append("</div>\n</body>\n</html>");
        return html.toString();
    }
}