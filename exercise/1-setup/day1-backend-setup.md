# 🏗️ Day 1 Setup: Backend & Service Layer Prerequisites

## 🎯 Setup Overview

This setup ensures you have everything needed to successfully complete Day 1 of the Personal Expense Tracker project. You'll verify Java, Maven, IDE setup, and create the initial project structure.

**⏱️ Estimated Setup Time: 5 minutes**

---

## ✅ Prerequisites Checklist

### 📋 Required Software

**Java Development Kit (JDK)**

```bash
# Check Java version (should be 17 or higher, preferably 21)
java -version
javac -version

# Expected output should show version 21+ or 17+
# If not installed, download from: https://adoptium.net/
```

**Maven Build Tool**

```bash
# Check Maven version (should be 3.6 or higher)
mvn -version

# Expected output should show Maven 3.6+ and Java 21+
# If not installed: https://maven.apache.org/install.html
```

**IDE and Extensions**

- VS Code or IntelliJ IDEA installed
- GitHub Copilot extension enabled and authenticated
- Java Extension Pack (for VS Code)
- Spring Boot Extension Pack (for VS Code)

---

## 🚀 Project Structure Setup

### 📁 Create Project Directory

```bash
# Navigate to your workspace
cd /path/to/your/workspace

# Create project directory
mkdir Personal-Expense-Tracker
cd Personal-Expense-Tracker
```

### 🔧 Verify GitHub Copilot

Test Copilot functionality:

1. Open VS Code in the project directory: `code .`
2. Create a new file: `Test.java`
3. Type: `// Create a simple Hello World class`
4. Press `Tab` to accept Copilot suggestion
5. Verify Copilot generates appropriate Java code

---

## 📦 Maven Project Initialization

### 🎯 Create Spring Boot Project Structure

Use this Maven archetype command or follow the exercise prompts:

```bash
# Create Maven project structure
mvn archetype:generate \
  -DgroupId=com.expensetracker.app \
  -DartifactId=Personal-Expense-Tracker \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

# Navigate into project
cd Personal-Expense-Tracker
```

### 📋 Required Dependencies

Your `pom.xml` should include these dependencies (or let Copilot help you create them):

- Spring Boot Starter Web
- Spring Boot Starter Data JPA  
- Spring Boot Starter Validation
- Spring Boot Starter Test
- H2 Database
- Spring Boot DevTools (optional, for development)

---

## 🗄️ Database Setup

### 💾 H2 Database Configuration

Create `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:h2:mem:expensetracker
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (for development)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Application Configuration
spring.application.name=Personal-Expense-Tracker
server.port=8080
```

---

## ✅ Setup Verification

### 🧪 Test Basic Setup

Create a simple test to verify everything works:

```java
// src/test/java/com/expensetracker/app/SetupVerificationTest.java
package com.expensetracker.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SetupVerificationTest {

    @Test
    void contextLoads() {
        // This test will pass if Spring Boot can start successfully
    }
}
```

### 🚀 Run Verification Tests

```bash
# Compile and test the basic setup
mvn clean compile test

# Expected result: BUILD SUCCESS
# If successful, you're ready for Day 1!
```

---

## 🔧 IDE Configuration

### 📝 VS Code Settings

Create `.vscode/settings.json` for optimal Java development:

```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "spring-boot.ls.problem.application-properties.unknown-property": "ignore",
    "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml"
}
```

### 🎨 GitHub Copilot Settings

Recommended Copilot settings for Java development:

```json
{
    "github.copilot.enable": {
        "*": true,
        "yaml": false,
        "plaintext": false
    },
    "github.copilot.advanced": {
        "length": 500,
        "temperature": 0.1
    }
}
```

---

## 📚 Development Tools

### 🔍 Useful Commands

```bash
# Run Spring Boot application
mvn spring-boot:run

# Run tests with coverage
mvn clean test jacoco:report

# Package application
mvn clean package

# Run specific test
mvn test -Dtest=ExpenseServiceTest
```

### 🌐 Access Points (after app starts)

- **Application**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **Actuator Health**: http://localhost:8080/actuator/health

---

## 🚨 Troubleshooting

### Common Issues and Solutions

**Maven Dependencies Not Downloading**

```bash
# Clear Maven repository and reinstall
rm -rf ~/.m2/repository
mvn clean install
```

**Java Version Issues**

```bash
# Set JAVA_HOME environment variable
export JAVA_HOME=/path/to/java21
# Add to your ~/.zshrc or ~/.bash_profile
```

**IDE Not Recognizing Java**

- Restart VS Code
- Run: `Java: Reload Projects` from Command Palette (Cmd+Shift+P)
- Verify Java Extension Pack is installed and enabled

**GitHub Copilot Not Working**

- Check authentication: `Copilot: Sign In` in Command Palette
- Verify subscription status
- Restart VS Code

---

## ✅ Ready for Day 1!

### 🎯 Success Checklist

- [ ] Java 21+ installed and verified
- [ ] Maven 3.6+ installed and working
- [ ] IDE configured with Java and Copilot extensions
- [ ] Project structure created
- [ ] Basic Spring Boot application compiles and tests pass
- [ ] H2 database accessible
- [ ] GitHub Copilot generating suggestions

### 🚀 Next Steps

You're now ready to start **Day 1: Backend & Service Layer Implementation**!

The setup process should have taken about 5 minutes. If you encountered any issues, please resolve them before starting the main exercise.

**Time to build something amazing with GitHub Copilot! 🎉**