# 🧪 Day 3 Setup: Testing & UI Automation Prerequisites

## 🎯 Setup Overview

This setup ensures your complete web application from Days 1-2 is working correctly and prepares your environment for comprehensive testing including Selenium WebDriver UI automation with TestNG.

**⏱️ Estimated Setup Time: 7 minutes**

---

## ✅ Prerequisites Verification

### 🌐 Complete Web Application Must Be Working

**Verify Full Application Stack**

```bash
# Navigate to your project directory
cd Personal-Expense-Tracker

# Start the application
mvn spring-boot:run

# Test in browser - all these should work:
# http://localhost:8080 (Dashboard)
# http://localhost:8080/expenses (Expense list)
# http://localhost:8080/categories (Category list)
# Add new expense, edit expense, delete expense
# Add new category, edit category

# Stop with Ctrl+C when verified
```

**Required Components from Previous Days**

- [ ] Backend services (Day 1) fully functional
- [ ] REST APIs responding correctly
- [ ] Web interface (Day 2) working properly
- [ ] Dashboard displaying data and charts
- [ ] Forms submitting and validating correctly
- [ ] AJAX functionality working

---

## 📦 Testing Dependencies Setup

### 🧪 Add Testing Dependencies to Maven

Update your `pom.xml` with comprehensive testing dependencies:

```xml
<dependencies>
    <!-- Existing dependencies... -->
    
    <!-- Enhanced Testing Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- TestNG Framework -->
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.8.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Selenium WebDriver -->
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.15.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- WebDriverManager for automatic driver management -->
    <dependency>
        <groupId>io.github.bonigarcia</groupId>
        <artifactId>webdrivermanager</artifactId>
        <version>5.5.3</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito for advanced mocking -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- TestContainers for integration testing (optional) -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.1</version>
        <scope>test</scope>
    </dependency>
    
    <!-- ExtentReports for beautiful test reporting -->
    <dependency>
        <groupId>com.aventstack</groupId>
        <artifactId>extentreports</artifactId>
        <version>5.0.9</version>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ for fluent assertions -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Install Dependencies**

```bash
# Download new testing dependencies
mvn clean install -DskipTests
```

---

## 🌐 Browser Setup for Selenium

### 🔧 Install Required Browsers

**Chrome (Recommended)**

```bash
# macOS
brew install --cask google-chrome

# Windows (using Chocolatey)
choco install googlechrome

# Windows (using winget)
winget install Google.Chrome

# Manual Installation (all platforms)
# Download from: https://www.google.com/chrome/

# Verify installation
google-chrome --version          # macOS/Linux
"C:\Program Files\Google\Chrome\Application\chrome.exe" --version  # Windows
```

**Firefox (Optional for cross-browser testing)**

```bash
# macOS  
brew install --cask firefox

# Windows (using Chocolatey)
choco install firefox

# Windows (using winget)
winget install Mozilla.Firefox

# Manual Installation (all platforms)
# Download from: https://www.mozilla.org/firefox/

# Verify installation
firefox --version               # macOS/Linux
"C:\Program Files\Mozilla Firefox\firefox.exe" --version  # Windows
```

### 🚀 WebDriver Verification

WebDriverManager will automatically download drivers, but verify setup:

```java
// Quick test to verify WebDriver setup
package com.expensetracker.app;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class WebDriverSetupTest {
    
    @Test
    public void verifyWebDriverSetup() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.google.com");
        System.out.println("Page title: " + driver.getTitle());
        driver.quit();
    }
}
```

---

## 🗂️ Test Directory Structure

### 📁 Create Comprehensive Test Structure

```bash
# Create test directories
mkdir -p src/test/java/com/expensetracker/app/service
mkdir -p src/test/java/com/expensetracker/app/repository  
mkdir -p src/test/java/com/expensetracker/app/controller
mkdir -p src/test/java/com/expensetracker/app/integration
mkdir -p src/test/java/com/expensetracker/app/ui
mkdir -p src/test/java/com/expensetracker/app/ui/pages
mkdir -p src/test/java/com/expensetracker/app/ui/tests
mkdir -p src/test/java/com/expensetracker/app/utils

# Create test resources
mkdir -p src/test/resources/testdata
mkdir -p src/test/resources/config
mkdir -p src/test/resources/reports
```

### 🎯 Expected Test Structure After Day 3

```text
src/test/java/com/expensetracker/app/
├── service/
│   ├── ExpenseServiceTest.java
│   ├── CategoryServiceTest.java
│   └── ...
├── repository/
│   ├── ExpenseRepositoryTest.java
│   ├── CategoryRepositoryTest.java
│   └── ...
├── controller/
│   ├── ExpenseControllerTest.java
│   ├── CategoryControllerTest.java
│   └── ...
├── integration/
│   ├── ExpenseIntegrationTest.java
│   ├── WebLayerIntegrationTest.java
│   └── ...
├── ui/
│   ├── pages/
│   │   ├── DashboardPage.java
│   │   ├── ExpenseFormPage.java
│   │   └── ...
│   ├── tests/
│   │   ├── ExpenseManagementTest.java
│   │   ├── CategoryManagementTest.java
│   │   └── ...
│   └── BaseUITest.java
└── utils/
    ├── TestDataBuilder.java
    ├── ScreenshotUtils.java
    └── ...
```

---

## ⚙️ TestNG Configuration

### 📋 Create TestNG Suite Configuration

Create `src/test/resources/testng.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<suite name="ExpenseTrackerTestSuite" parallel="methods" thread-count="3">
    
    <test name="UnitTests">
        <groups>
            <run>
                <include name="unit"/>
            </run>
        </groups>
        <packages>
            <package name="com.expensetracker.app.service"/>
            <package name="com.expensetracker.app.repository"/>
        </packages>
    </test>
    
    <test name="IntegrationTests">
        <groups>
            <run>
                <include name="integration"/>
            </run>
        </groups>
        <packages>
            <package name="com.expensetracker.app.integration"/>
            <package name="com.expensetracker.app.controller"/>
        </packages>
    </test>
    
    <test name="UITests">
        <groups>
            <run>
                <include name="ui"/>
            </run>
        </groups>
        <packages>
            <package name="com.expensetracker.app.ui.tests"/>
        </packages>
    </test>
    
</suite>
```

### 🎨 Create Test Configuration Properties

Create `src/test/resources/application-test.properties`:

```properties
# Test Database Configuration
spring.datasource.url=jdbc:h2:mem:testexpensetracker
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Test JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.h2.console.enabled=false

# Test Server Configuration
server.port=0
spring.test.database.replace=none

# Selenium Configuration
selenium.browser=chrome
selenium.headless=false
selenium.timeout=10
selenium.base.url=http://localhost:8080
```

---

## 🔧 Maven Surefire Plugin Configuration

### 📊 Update Maven for TestNG

Add to your `pom.xml`:

```xml
<build>
    <plugins>
        <!-- Existing plugins... -->
        
        <!-- Surefire Plugin for TestNG -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>
                <suiteXmlFiles>
                    <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
                </suiteXmlFiles>
                <groups>unit,integration</groups>
                <excludedGroups>ui</excludedGroups>
            </configuration>
        </plugin>
        
        <!-- Failsafe Plugin for Integration Tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>
                <suiteXmlFiles>
                    <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
                </suiteXmlFiles>
                <groups>ui</groups>
                <includes>
                    <include>**/*UITest.java</include>
                </includes>
            </configuration>
        </plugin>
        
        <!-- JaCoCo for Code Coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.10</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
    </plugins>
</build>
```

---

## ✅ Setup Verification

### 🧪 Create Basic Test Verification

Create `src/test/java/com/expensetracker/app/SetupVerificationTest.java`:

```java
package com.expensetracker.app;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@SpringBootTest
public class SetupVerificationTest extends AbstractTestNGSpringContextTests {

    @Test(groups = "unit")
    public void contextLoads() {
        // Verifies Spring Boot test context loads correctly
    }
    
    @Test(groups = "ui")
    public void seleniumSetupTest() {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode for setup test
        
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get("https://www.google.com");
            assert driver.getTitle().contains("Google");
            System.out.println("Selenium setup successful!");
        } finally {
            driver.quit();
        }
    }
}
```

### 🚀 Run Setup Verification

```bash
# Run unit tests
mvn test -Dgroups=unit

# Run Selenium setup test (will download ChromeDriver automatically)
mvn test -Dgroups=ui -Dtest=SetupVerificationTest

# Expected output:
# - All tests pass
# - "Selenium setup successful!" message appears
# - ChromeDriver downloaded automatically
```

---

## 🎯 Performance Testing Setup (Optional)

### ⚡ Add JMeter-style Load Testing

```xml
<!-- Add to pom.xml for performance testing -->
<dependency>
    <groupId>org.apache.jmeter</groupId>
    <artifactId>ApacheJMeter_java</artifactId>
    <version>5.5</version>
    <scope>test</scope>
</dependency>
```

---

## 🚨 Troubleshooting

### Common Testing Issues

**Chrome Driver Not Found**

```bash
# WebDriverManager should handle this, but if issues persist:
# Download manually from: https://chromedriver.chromium.org/
# Or update Chrome browser to latest version
```

**TestNG Not Found**

```bash
# Verify TestNG dependency
mvn dependency:tree | grep testng

# If missing, add to pom.xml and run:
mvn clean install
```

**Port Conflicts During Testing**

```text
# In application-test.properties, use random port:
server.port=0

# Or specify a test-specific port:
server.port=8081
```

**Selenium Tests Timing Out**

```java
// Increase timeouts in test configuration
selenium.timeout=30

// Or in code:
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
```

---

## ✅ Ready for Day 3

### 🎯 Success Checklist

- [ ] Complete web application (Days 1-2) fully functional
- [ ] All testing dependencies installed and verified
- [ ] Chrome browser installed and WebDriver working
- [ ] TestNG configuration created and working
- [ ] Test directory structure created
- [ ] Maven plugins configured for testing
- [ ] Setup verification tests pass
- [ ] Selenium can launch browser and navigate
- [ ] Application can run on test port

### 🚀 Next Steps

You're now ready to start **Day 3: Comprehensive Testing & UI Automation**!

### 📋 What You'll Build Today

- 40+ comprehensive unit tests with mocking
- 20+ integration tests for all layers
- 15+ Selenium UI automation tests
- TestNG framework with organized test execution
- Performance testing and load verification
- Beautiful test reports with coverage metrics
- CI/CD ready testing pipeline

**Let's ensure your application is production-ready! 🎯**