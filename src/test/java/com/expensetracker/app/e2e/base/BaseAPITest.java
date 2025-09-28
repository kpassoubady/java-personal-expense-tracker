package com.expensetracker.app.e2e.base;

import org.testng.annotations.*;

public abstract class BaseAPITest extends BaseTest {
    @BeforeClass
    public void setUpApi() {
        // Configure base API settings
        String baseUrl = System.getProperty("app.base.url", "http://localhost:8080");
        logger.info("API Base URL configured: {}", baseUrl);
        // RestAssured configuration can be added when needed
    }
}
