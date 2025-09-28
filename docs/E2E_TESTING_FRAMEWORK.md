# Personal Expense Tracker - E2E Testing Framework

## Overview

This project includes a comprehensive Selenium-based End-to-End (E2E) testing framework for the Personal Expense Tracker application. The framework implements industry best practices including Page Object Model, cross-browser testing, parallel execution, and advanced reporting.

## 🚀 Framework Features

### ✅ Core Framework Components

- **TestNG Configuration**: Parallel execution with configurable thread count
- **WebDriverManager Integration**: Automatic driver management for Chrome and Firefox
- **Page Object Model**: Maintainable test structure with reusable page components
- **BaseTest Class**: WebDriver setup/teardown with comprehensive lifecycle management
- **Screenshot Utilities**: Automatic failure documentation with timestamp and browser info
- **Test Data Builders**: Fluent API for consistent test data creation
- **Custom Wait Utilities**: 15+ specialized WebDriver wait methods
- **Enhanced Assertions**: 20+ custom assertions with detailed error messages
- **Cross-browser Testing**: Chrome and Firefox support with headless mode
- **ExtentReports Integration**: Professional HTML test reports with screenshots
- **Retry Mechanism**: Configurable retry analyzer for flaky test handling

### 🏗️ Project Structure

```luna
src/test/java/com/expensetracker/app/e2e/
├── base/
│   └── BaseTest.java                    # Base class for all tests
├── config/
│   ├── TestConfig.java                  # Configuration management
│   └── WebDriverConfig.java            # WebDriver lifecycle management
├── listeners/
│   ├── ExtentReportListener.java        # ExtentReports integration
│   └── RetryAnalyzer.java              # Test retry mechanism
├── pages/
│   ├── BasePage.java                   # Base page object class
│   ├── HomePage.java                   # Dashboard page object
│   ├── ExpensesPage.java              # Expenses list page object
│   ├── ExpenseFormPage.java           # Expense form page object
│   ├── CategoriesPage.java            # Categories page object
│   └── CategoryFormPage.java          # Category form page object
├── tests/
│   └── smoke/
│       └── SmokeTests.java            # Critical smoke tests
└── utils/
    ├── AssertUtils.java               # Custom assertion utilities
    ├── ScreenshotUtils.java           # Screenshot capture utilities
    ├── TestDataBuilder.java           # Test data creation utilities
    └── WaitUtils.java                 # WebDriver wait utilities

src/test/resources/
├── testng/
│   └── testng.xml                     # TestNG suite configuration
└── test-config.properties             # Test configuration properties
```

## 🛠️ Setup Instructions

### Prerequisites

1. **Java 17+** - Required for running tests
2. **Maven 3.8+** - For dependency management
3. **Chrome Browser** - For Chrome WebDriver (latest stable)
4. **Firefox Browser** - For Firefox WebDriver (optional)

### Installation

1. **Clone the repository**:

   ```bash
   git clone <repository-url>
   cd Personal-Expense-Tracker
   ```

2. **Install dependencies**:

   ```bash
   mvn clean install -DskipTests
   ```

3. **Start the application** (in separate terminal):

   ```bash
   mvn spring-boot:run
   ```

4. **Verify application is running**:
   - Open browser and navigate to `http://localhost:8080`
   - Confirm the application loads successfully

## 🧪 Running Tests

### Command Line Execution

#### Run All Tests

```bash
mvn clean test
```

#### Run Specific Test Groups

```bash
# Run smoke tests only
mvn clean test -Dgroups=smoke

# Run regression tests
mvn clean test -Dgroups=regression

# Run cross-browser tests
mvn clean test -Dgroups=cross-browser
```

#### Browser Configuration

```bash
# Run with Chrome (default)
mvn clean test -Dbrowser=chrome

# Run with Firefox
mvn clean test -Dbrowser=firefox

# Run in headless mode
mvn clean test -Dheadless=true

# Run with both browser and headless
mvn clean test -Dbrowser=firefox -Dheadless=true
```

#### Parallel Execution

```bash
# Run with custom thread count
mvn clean test -DparallelCount=4

# Run with parallel and headless mode
mvn clean test -DparallelCount=2 -Dheadless=true
```

### TestNG XML Execution

```bash
# Run via TestNG XML configuration
mvn clean test -DsuiteXmlFile=src/test/resources/testng/testng.xml
```

### IDE Execution

#### IntelliJ IDEA

1. Right-click on `testng.xml` → Run
2. Or right-click on any test class → Run
3. Configure VM options: `-Dbrowser=chrome -Dheadless=false`

#### Eclipse

1. Right-click on test class → Run As → TestNG Test
2. Configure run configuration with system properties

## 📊 Test Reporting

### ExtentReports

- **Location**: `test-output/reports/`
- **Format**: Interactive HTML reports
- **Features**:
  - Test execution timeline
  - Screenshots for failed tests
  - Browser and environment information
  - Pass/fail statistics with charts
  - Test retry information

### Screenshots

- **Location**: `test-output/screenshots/`
- **Automatic capture**: On test failures
- **Naming convention**: `TestName_Browser_Timestamp.png`
- **Cleanup**: Configurable retention period (7 days default)

### TestNG Reports

- **Location**: `test-output/`
- **Files**: `index.html`, `emailable-report.html`

## ⚙️ Configuration

### Test Configuration Properties

Edit `src/test/resources/test-config.properties`:

```properties
# Application Configuration
app.base.url=http://localhost:8080
browser.default=chrome
browser.headless=false

# Test Execution
test.parallel.count=2
test.max.retry.count=1

# Timeouts (seconds)
wait.explicit.timeout=15
wait.page.load.timeout=30

# Directories
screenshots.dir=test-output/screenshots
reports.dir=test-output/reports
```

### TestNG Configuration

Edit `src/test/resources/testng/testng.xml`:

```xml
<suite name="ExpenseTrackerE2ESuite" parallel="methods" thread-count="2">
    <parameter name="browser" value="chrome"/>
    <parameter name="headless" value="false"/>
    <parameter name="environment" value="local"/>
    
    <listeners>
        <listener class-name="com.expensetracker.app.e2e.listeners.ExtentReportListener"/>
    </listeners>
    
    <test name="SmokeTests">
        <groups>
            <run>
                <include name="smoke"/>
            </run>
        </groups>
        <classes>
            <class name="com.expensetracker.app.e2e.tests.smoke.SmokeTests"/>
        </classes>
    </test>
</suite>
```

## 📝 Writing Tests

### Basic Test Structure

```java
public class ExampleTest extends BaseTest {
    
    @Test(groups = {"smoke"}, description = "Test description")
    public void testExample() {
        // Test implementation using page objects
        HomePage homePage = new HomePage();
        homePage.verifyDashboardLoaded();
        
        // Use custom assertions
        AssertUtils.assertElementVisible(locator, "Element should be visible");
        
        // Log test steps
        ExtentReportListener.logInfo("Test step completed");
    }
}
```

### Using Page Objects

```java
// Navigate and interact with pages
HomePage homePage = new HomePage();
ExpensesPage expensesPage = homePage.navigateToExpenses();
ExpenseFormPage formPage = expensesPage.clickAddExpense();

// Fill forms using fluent API
formPage.fillExpenseForm("Lunch", "25.50", "Food", "2024-01-15")
        .saveExpense();
```

### Test Data Creation

```java
// Create test data using builders
ExpenseData expense = TestDataBuilder.ExpenseBuilder
    .withDescription("Coffee")
    .withAmount("4.50")
    .withCategory("Food")
    .withDate("2024-01-15")
    .build();
```

## 🔧 Utilities and Helpers

### Custom Assertions

```java
AssertUtils.assertElementVisible(locator, "Message");
AssertUtils.assertPageTitleContains("Expected Title", "Message");
AssertUtils.assertElementCount(locator, 5, "Should have 5 elements");
```

### Wait Utilities

```java
WaitUtils.waitForElementVisible(locator);
WaitUtils.waitForElementClickable(locator);
WaitUtils.waitForTextPresent(locator, "Expected Text");
```

### Screenshot Utilities

```java
String path = ScreenshotUtils.captureScreenshot("test_name");
ScreenshotUtils.captureFailureScreenshot("failed_test", throwable);
```

## 🐛 Troubleshooting

### Common Issues

1. **WebDriver Issues**:
   - Ensure Chrome/Firefox browsers are installed
   - WebDriverManager handles driver binaries automatically
   - Check browser version compatibility

2. **Port Conflicts**:
   - Ensure application runs on port 8080
   - Update `app.base.url` if using different port

3. **Test Failures**:
   - Check screenshots in `test-output/screenshots/`
   - Review ExtentReports for detailed error information
   - Verify application state and test data

4. **Parallel Execution Issues**:
   - Reduce thread count if experiencing instability
   - Use headless mode for CI/CD environments

### Debug Mode

```bash
# Run with debug logging
mvn clean test -Dlog.level=DEBUG

# Run single test for debugging
mvn clean test -Dtest=SmokeTests#testApplicationLoad
```

## 🚀 CI/CD Integration

### GitHub Actions Example

```yaml
name: E2E Tests
on: [push, pull_request]

jobs:
  e2e-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Start Application
        run: |
          mvn spring-boot:run &
          sleep 30
      
      - name: Run E2E Tests
        run: mvn clean test -Dheadless=true -DparallelCount=2
      
      - name: Upload Test Reports
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-reports
          path: test-output/
```

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    stages {
        stage('Start Application') {
            steps {
                sh 'mvn spring-boot:run &'
                sleep 30
            }
        }
        stage('Run E2E Tests') {
            steps {
                sh 'mvn clean test -Dheadless=true'
            }
        }
        stage('Publish Reports') {
            steps {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'test-output/reports',
                    reportFiles: '*.html',
                    reportName: 'E2E Test Report'
                ])
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'test-output/**/*', fingerprint: true
        }
    }
}
```

## 🤝 Contributing

1. Follow the existing Page Object Model structure
2. Add appropriate test groups (`@Test(groups = {"smoke", "regression"})`)
3. Include meaningful test descriptions
4. Use custom assertions and utilities
5. Add logging for important test steps
6. Ensure tests are independent and can run in parallel

## 📚 Best Practices

1. **Page Objects**: Keep locators and actions in page classes
2. **Test Independence**: Each test should be able to run independently
3. **Data Management**: Use TestDataBuilder for consistent test data
4. **Assertions**: Use custom assertions for better error messages
5. **Waiting**: Always use explicit waits instead of Thread.sleep()
6. **Screenshots**: Automatic capture on failures, manual capture for key steps
7. **Reporting**: Use ExtentReportListener.logInfo() for test step documentation

## 📄 License

This testing framework is part of the Personal Expense Tracker project and follows the same license terms.
