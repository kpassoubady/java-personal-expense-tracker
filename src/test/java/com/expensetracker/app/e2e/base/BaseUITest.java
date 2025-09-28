package com.expensetracker.app.e2e.base;

import org.testng.annotations.*;
import org.testng.ITestResult;
import com.expensetracker.app.e2e.config.WebDriverConfig;
import com.expensetracker.app.e2e.utils.ScreenshotUtils;

public abstract class BaseUITest extends BaseTest {
    @BeforeClass
    public void setUpWebDriver() {
        driver = WebDriverConfig.getDriver();
    }

    @AfterMethod
    public void captureScreenshotOnFailure(ITestResult result) {
        if (!result.isSuccess()) {
            ScreenshotUtils.captureFailureScreenshot(result.getName(), result.getThrowable());
        }
    }

    @AfterClass
    public void tearDownWebDriver() {
        if (driver != null) driver.quit();
    }
}
