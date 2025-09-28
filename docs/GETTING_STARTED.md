# Getting Started with Personal Expense Tracker

This guide will help you set up and run the Personal Expense Tracker application on your local machine.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

### Required Software

- **Java Development Kit (JDK) 21+**
  - Download from [OpenJDK](https://openjdk.java.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
  - Verify installation: `java -version`

- **Apache Maven 3.6+**
  - Download from [Maven Official Site](https://maven.apache.org/download.cgi)
  - Verify installation: `mvn -version`

- **Git**
  - Download from [Git Official Site](https://git-scm.com/downloads)
  - Verify installation: `git --version`

### Optional Tools

- **IDE**: IntelliJ IDEA, Eclipse, or Visual Studio Code
- **Postman**: For API testing
- **Web Browser**: Chrome, Firefox, or Safari for web interface

## Quick Setup (5 minutes)

### 1. Clone the Repository

```bash
git clone https://github.com/kpassoubady/Personal-Expense-Tracker.git
cd Personal-Expense-Tracker
```

### 2. Verify Java and Maven

```bash
java -version  # Should show Java 21+
mvn -version   # Should show Maven 3.6+
```

### 3. Build and Test

```bash
# Install dependencies and compile
mvn clean compile

# Run tests (optional but recommended)
mvn test

# Package the application
mvn clean package
```

### 4. Run the Application

```bash
# Start the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/expense-tracker-1.0.0.jar
```

### 5. Access the Application

Open your web browser and navigate to:

- **Main Application**: [http://localhost:8080](http://localhost:8080)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

## Detailed Setup Guide

### Environment Configuration

#### 1. Set JAVA_HOME (if not already set)

**Windows:**

```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
```

**macOS/Linux:**

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

#### 2. Set MAVEN_HOME (if using manual Maven installation)

**Windows:**

```cmd
set MAVEN_HOME=C:\apache-maven-3.9.4
set PATH=%MAVEN_HOME%\bin;%PATH%
```

**macOS/Linux:**

```bash
export MAVEN_HOME=/opt/apache-maven-3.9.4
export PATH=$MAVEN_HOME/bin:$PATH
```

### Database Configuration

The application uses H2 in-memory database by default. No additional setup required!

#### Database Access Details

- **JDBC URL**: `jdbc:h2:mem:expensetracker`
- **Username**: `sa`
- **Password**: `password`
- **Driver**: `org.h2.Driver`

#### H2 Console Access

1. Start the application
2. Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
3. Use the connection details above
4. Click "Connect"

### Application Configuration

Key configuration files:

#### `application.properties`

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:mem:expensetracker
spring.h2.console.enabled=true

# Development Tools
spring.devtools.restart.enabled=true
spring.thymeleaf.cache=false
```

#### Custom Configuration Options

You can override default settings using environment variables:

```bash
# Change server port
export SERVER_PORT=9090

# Enable/disable H2 console
export SPRING_H2_CONSOLE_ENABLED=true

# Change database URL (for persistent storage)
export SPRING_DATASOURCE_URL=jdbc:h2:file:./data/expenses
```

## Development Workflow

### 1. Making Changes

```bash
# Make your code changes
# The application will auto-restart due to Spring Boot DevTools
```

### 2. Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ExpenseRepositoryIntegrationTest

# Run tests with coverage
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### 3. Database Schema

The application automatically creates the database schema on startup. Sample data is loaded via `DataSeeder.java`.

#### Tables Created

- `categories` - Expense categories
- `expenses` - Individual expense records

#### Sample Data

- 3 default categories (Food, Transport, Entertainment)
- Sample expenses for demonstration

## API Testing

### Using curl

```bash
# Get all expenses
curl http://localhost:8080/api/expenses

# Create new expense
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Coffee",
    "amount": 4.50,
    "expenseDate": "2025-09-27",
    "category": {"id": 1}
  }'

# Get analytics
curl http://localhost:8080/api/expenses/analytics
```

### Using Postman

1. Import the following endpoints:
   - GET `http://localhost:8080/api/expenses`
   - POST `http://localhost:8080/api/expenses`
   - GET `http://localhost:8080/api/expenses/analytics`
   - GET `http://localhost:8080/api/categories`

## Troubleshooting

### Common Issues

#### Port Already in Use

```bash
# Error: Port 8080 was already in use
# Solution: Change port or kill process
export SERVER_PORT=8081
# Or kill process using port 8080
lsof -ti:8080 | xargs kill -9
```

#### Java Version Issues

```bash
# Error: Unsupported class file major version
# Solution: Ensure Java 21+ is installed and JAVA_HOME is set correctly
java -version
echo $JAVA_HOME
```

#### Maven Build Failures

```bash
# Clear Maven cache and rebuild
mvn clean
rm -rf ~/.m2/repository/com/expensetracker
mvn clean compile
```

#### Database Connection Issues

```bash
# Check if H2 console is enabled
# Verify URL: http://localhost:8080/h2-console
# Ensure connection details match application.properties
```

### Getting Help

1. **Check Logs**: Application logs are displayed in the console
2. **Verify Configuration**: Check `application.properties`
3. **Database State**: Use H2 console to inspect data
4. **Network Issues**: Ensure no firewall blocking port 8080

## IDE Setup

### IntelliJ IDEA

1. Open the project folder
2. IntelliJ should automatically detect it as a Maven project
3. Set Project SDK to Java 21
4. Enable annotation processing
5. Install Lombok plugin (if using Lombok)

### Visual Studio Code

1. Install "Extension Pack for Java"
2. Install "Spring Boot Extension Pack"
3. Open the project folder
4. VS Code should automatically configure the project

### Eclipse

1. Import as "Existing Maven Project"
2. Select the project root folder
3. Set Java Build Path to JDK 21
4. Install Spring Tools Suite plugin

## What's Next?

After successfully running the application:

1. **Explore the Web Interface**
   - Create your first expense category
   - Add some personal expenses
   - Check the dashboard analytics

2. **Test the API**
   - Use the provided curl commands
   - Import endpoints into Postman
   - Review the API response formats

3. **Examine the Code**
   - Browse the controller classes
   - Check the service layer logic
   - Review the comprehensive test suite

4. **Customize the Application**
   - Modify templates for your preferences
   - Add new expense categories
   - Experiment with the analytics

Happy coding! 🎉
