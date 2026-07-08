# Personal Expense Tracker - Java Spring Boot

## Project Overview

This is a comprehensive, full-stack personal expense tracking application built with **Spring Boot 3**, **Thymeleaf**, and **Bootstrap 5**. It features a modern responsive UI, robust REST APIs, comprehensive analytics, and enterprise-grade testing coverage. This project serves as a capstone application for the GitHub Copilot Java training track.

**Author:** Kangeyan Passoubady (Kavin School LLC)
**Framework:** Spring Boot 3.2.3, Java 21
**Database:** H2 In-Memory Database (Development), PostgreSQL (Production)
**License:** MIT

## Repository Structure

```
java-personal-expense-tracker/
├── src/
│   ├── main/
│   │   ├── java/com/expensetracker/app/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # Web & REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── repository/       # Data repositories
│   │   │   ├── service/          # Business logic layer
│   │   │   └── ExpenseTrackerApplication.java
│   │   └── resources/
│   │       ├── templates/        # Thymeleaf templates
│   │       │   ├── categories/   # Category management pages
│   │       │   ├── expenses/     # Expense management pages
│   │       │   ├── fragments/    # Reusable template fragments
│   │       │   ├── home/         # Dashboard pages
│   │       │   └── layout/       # Layout templates
│   │       ├── static/           # CSS, JS, images
│   │       └── application.properties
│   └── test/
│       └── java/com/expensetracker/app/
│           ├── controller/       # Integration tests
│           ├── repository/       # Repository tests
│           ├── service/          # Service layer tests
│           └── ExpenseTrackerApplicationTests.java
├── docs/                         # Additional documentation
├── images/                       # Screenshots and images
├── pom.xml                       # Maven configuration
└── README.md                     # Project documentation
```

## Technology Stack

```
Frontend:    Thymeleaf + Bootstrap 5 + JavaScript (jQuery)
Backend:     Spring Boot 3.2.3 + Spring MVC + Spring Data JPA
Database:    H2 In-Memory Database (Development)
Testing:     JUnit 5 + MockMvc + AssertJ + Testcontainers Ready
Build:       Maven 3 + Java 21
```

## Key Features

### Dashboard & Analytics
- Interactive dashboard with expense summaries and trends
- Monthly spending analytics and category breakdowns
- Visual expense distribution charts
- Quick action buttons for common tasks

### Expense Management
- Create, read, update, and delete expenses
- Advanced search and filtering capabilities
- Category-based expense organization
- Date range filtering and sorting

### Category Management
- Custom expense categories with colors and icons
- Category-wise spending analytics
- Bulk category operations

### Modern Web Interface
- Responsive design optimized for all devices
- Dark/Light theme support with auto-detection
- Accessibility-first design (ARIA labels, semantic HTML)
- Progressive Web App (PWA) features

### REST API
- Complete RESTful API for all operations
- JSON-based data exchange
- Advanced analytics endpoints
- API documentation and testing support

## Database Schema

```sql
Categories
├── id (PRIMARY KEY)
├── name (UNIQUE, NOT NULL)
├── description
├── color (HEX code)
├── icon (emoji/symbol)
├── created_at
└── updated_at

Expenses  
├── id (PRIMARY KEY)
├── description (NOT NULL)
├── amount (NOT NULL, > 0, ≤ 10,000)
├── expense_date (NOT NULL)
├── category_id (FOREIGN KEY → Categories)
├── created_at
└── updated_at
```

## Development Workflow

### Prerequisites
- **Java 21** or higher
- **Maven 3.6+** or higher
- **Git** (for cloning the repository)

### Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/kpassoubady/Personal-Expense-Tracker.git
   cd Personal-Expense-Tracker
   ```

2. **Build the Application**
   ```bash
   mvn clean compile
   ```

3. **Run Tests**
   ```bash
   mvn test
   ```

4. **Start the Application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the Application**
   - **Web Interface**: http://localhost:8080
   - **H2 Database Console**: http://localhost:8080/h2-console
   - **REST API Base**: http://localhost:8080/api

### Database Access
- **URL**: `jdbc:h2:mem:expensetracker`
- **Username**: `sa`
- **Password**: `password`

## Test Coverage

The application features **enterprise-grade test coverage** with 110+ integration tests across all layers:

| Test Suite                 | Purpose                       | Tests    | Status         |
| -------------------------- | ----------------------------- | -------- | -------------- |
| **Repository Integration** | Data layer & custom queries   | 36       | ✅ All Passing |
| **Analytics Repository**   | Complex analytics & reporting | 26       | ✅ All Passing |
| **REST API Integration**   | Complete API functionality    | 27       | ✅ All Passing |
| **Web Page Integration**   | UI, Templates, Navigation     | 21       | ✅ All Passing |
| **Service Layer**          | Business logic validation     | Multiple | ✅ Passing     |

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test suite
mvn test -Dtest=ExpenseRepositoryIntegrationTest
mvn test -Dtest=ExpenseAnalyticsRepositoryTest
mvn test -Dtest=ExpenseControllerIntegrationTest
mvn test -Dtest=WebPageIntegrationTest

# Generate test coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## API Documentation

### Expense Endpoints

```http
GET    /api/expenses                 # List all expenses
POST   /api/expenses                 # Create new expense
GET    /api/expenses/{id}            # Get specific expense
PUT    /api/expenses/{id}            # Update expense
DELETE /api/expenses/{id}            # Delete expense

# Analytics
GET    /api/expenses/analytics       # Complete analytics data
GET    /api/expenses/by-category     # Group by category
GET    /api/expenses/monthly-summary # Monthly totals
```

### Category Endpoints

```http
GET    /api/categories               # List all categories
POST   /api/categories               # Create new category
GET    /api/categories/{id}          # Get specific category
PUT    /api/categories/{id}          # Update category
DELETE /api/categories/{id}          # Delete category
GET    /api/categories/{id}/expenses # Category expenses
```

## Key Components

### Controllers
- **`HomeController`**: Dashboard and analytics presentation
- **`ExpenseController`**: Web UI for expense management + REST API endpoints
- **`CategoryController`**: Category management (Web + REST)

### Services
- **`ExpenseService`**: Business logic, validation, analytics calculations
- **`CategoryService`**: Category operations and relationship management

### Repositories
- **`ExpenseRepository`**: Custom queries, pagination, analytics
- **`ExpenseAnalyticsRepository`**: Specialized analytics and reporting queries
- **`CategoryRepository`**: Category data access and statistics

## Common Tasks

### For Developers
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests (`mvn test`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Standards
- **Java Style**: Follow Google Java Style Guide
- **Testing**: Maintain 90%+ test coverage
- **Documentation**: Update README for new features
- **Commits**: Use conventional commit messages

## Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration  
spring.datasource.url=jdbc:h2:mem:expensetracker
spring.h2.console.enabled=true

# Template Configuration
app.template.use-new-layout=true
spring.thymeleaf.cache=false

# Development Tools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
```

### Environment Variables

You can override properties using environment variables:

```bash
export SERVER_PORT=9090
export SPRING_DATASOURCE_URL=jdbc:h2:file:./data/expenses
export SPRING_H2_CONSOLE_ENABLED=false
```

## Deployment Considerations

1. **Database**: Replace H2 with PostgreSQL/MySQL for production
2. **Security**: Enable Spring Security for authentication
3. **Monitoring**: Add actuator endpoints for health checks
4. **Logging**: Configure appropriate log levels
5. **SSL**: Enable HTTPS for production deployment
