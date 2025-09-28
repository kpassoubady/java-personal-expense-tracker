package com.expensetracker.app.e2e.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for test environment settings
 * Loads properties from files and system properties
 */
public class TestConfig {
    
    private static final Properties properties = new Properties();
    private static TestConfig instance;
    
    // Default values
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_ENVIRONMENT = "local";
    private static final boolean DEFAULT_HEADLESS = false;
    private static final int DEFAULT_TIMEOUT = 30;
    
    static {
        loadProperties();
    }
    
    private TestConfig() {}
    
    public static TestConfig getInstance() {
        if (instance == null) {
            instance = new TestConfig();
        }
        return instance;
    }
    
    /**
     * Load properties from configuration files
     */
    private static void loadProperties() {
        // Load default properties
        loadPropertiesFromFile("/config/test-config.properties");
        
        // Load environment-specific properties
        String environment = System.getProperty("environment", DEFAULT_ENVIRONMENT);
        loadPropertiesFromFile("/config/" + environment + "-config.properties");
        
        // Override with system properties
        properties.putAll(System.getProperties());
    }
    
    /**
     * Load properties from a specific file
     */
    private static void loadPropertiesFromFile(String fileName) {
        try (InputStream inputStream = TestConfig.class.getResourceAsStream(fileName)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            // File not found is acceptable, use defaults
            System.out.println("Could not load properties file: " + fileName);
        }
    }
    
    /**
     * Get base URL for the application
     */
    public String getBaseUrl() {
        return getProperty("base.url", DEFAULT_BASE_URL);
    }
    
    /**
     * Get browser type
     */
    public String getBrowser() {
        return getProperty("browser", DEFAULT_BROWSER);
    }
    
    /**
     * Get environment name
     */
    public String getEnvironment() {
        return getProperty("environment", DEFAULT_ENVIRONMENT);
    }
    
    /**
     * Check if headless mode is enabled
     */
    public boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless", String.valueOf(DEFAULT_HEADLESS)));
    }
    
    /**
     * Get default timeout in seconds
     */
    public int getTimeout() {
        return Integer.parseInt(getProperty("timeout", String.valueOf(DEFAULT_TIMEOUT)));
    }
    
    /**
     * Get database URL
     */
    public String getDatabaseUrl() {
        return getProperty("database.url", "jdbc:h2:mem:expensetracker");
    }
    
    /**
     * Get API base URL
     */
    public String getApiBaseUrl() {
        return getBaseUrl() + "/api";
    }
    
    /**
     * Get H2 console URL
     */
    public String getH2ConsoleUrl() {
        return getBaseUrl() + "/h2-console";
    }
    
    /**
     * Get screenshots directory
     */
    public String getScreenshotsDir() {
        return getProperty("screenshots.dir", "target/screenshots");
    }
    
    /**
     * Get reports directory
     */
    public String getReportsDir() {
        return getProperty("reports.dir", "target/extent-reports");
    }
    
    /**
     * Get downloads directory
     */
    public String getDownloadsDir() {
        return getProperty("downloads.dir", "target/downloads");
    }
    
    /**
     * Check if retry is enabled for failed tests
     */
    public boolean isRetryEnabled() {
        return Boolean.parseBoolean(getProperty("retry.enabled", "true"));
    }
    
    /**
     * Get maximum retry count
     */
    public int getMaxRetryCount() {
        return Integer.parseInt(getProperty("retry.max.count", "1"));
    }
    
    /**
     * Check if parallel execution is enabled
     */
    public boolean isParallelExecutionEnabled() {
        return Boolean.parseBoolean(getProperty("parallel.execution", "true"));
    }
    
    /**
     * Get thread count for parallel execution
     */
    public int getThreadCount() {
        return Integer.parseInt(getProperty("thread.count", "2"));
    }
    
    /**
     * Check if video recording is enabled
     */
    public boolean isVideoRecordingEnabled() {
        return Boolean.parseBoolean(getProperty("video.recording", "false"));
    }
    
    /**
     * Get test data directory
     */
    public String getTestDataDir() {
        return getProperty("testdata.dir", "src/test/resources/testdata");
    }
    
    /**
     * Generic method to get property with default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Generic method to get property
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Set property (for dynamic configuration)
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Get all properties as a formatted string
     */
    public String getAllProperties() {
        StringBuilder sb = new StringBuilder("Test Configuration:\n");
        sb.append("Base URL: ").append(getBaseUrl()).append("\n");
        sb.append("Browser: ").append(getBrowser()).append("\n");
        sb.append("Environment: ").append(getEnvironment()).append("\n");
        sb.append("Headless: ").append(isHeadless()).append("\n");
        sb.append("Timeout: ").append(getTimeout()).append("\n");
        sb.append("Parallel Execution: ").append(isParallelExecutionEnabled()).append("\n");
        sb.append("Thread Count: ").append(getThreadCount()).append("\n");
        sb.append("Retry Enabled: ").append(isRetryEnabled()).append("\n");
        sb.append("Max Retry Count: ").append(getMaxRetryCount()).append("\n");
        sb.append("Screenshots Dir: ").append(getScreenshotsDir()).append("\n");
        sb.append("Reports Dir: ").append(getReportsDir()).append("\n");
        return sb.toString();
    }
}