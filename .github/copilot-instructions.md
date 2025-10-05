# Personal Expense Tracker - AI Coding Assistant Instructions

## Project Architecture

This is a **Spring Boot 3.2** application with **Java 21** using a layered architecture with comprehensive validation and dual-interface design:

- **Entities**: `Expense` and `Category` with custom validation annotations (`@PositiveAmount`, `@ValidColor`)
- **Services**: Transaction-managed business logic with custom exception handling (`ValidationException`, `EntityNotFoundException`)
- **Controllers**: Dual approach - Thymeleaf web controllers + separate REST API controllers (`/api/**`)
- **Templates**: Configurable dual-layout system (classic/new) via `TemplateConfig`
- **Testing**: Multi-framework approach - JUnit 5 (unit), TestNG (E2E), Selenium with Page Object Model

### Key Package Structure
```
com.expensetracker.app/
├── entity/           # JPA entities with custom validation
├── service/          # @Transactional business logic
├── controller/       # Separate web + REST controllers  
├── validation/       # Custom annotations (@PositiveAmount, @ValidColor)
├── exception/        # Domain-specific exceptions
├── config/           # TemplateConfig for dual-layout system
└── e2e/             # TestNG + Selenium Page Object Model tests
```

## Critical Development Patterns

### Custom Validation System
- **@PositiveAmount**: Custom BigDecimal validation for expense amounts
- **@ValidColor**: Hex color validation for categories (#RRGGBB or #RGB)
- **Business rule validation**: Additional validation in service layer and controllers
```java
@PositiveAmount(message = "Amount must be greater than zero")
@DecimalMax(value = "999999.99", message = "Amount cannot exceed 999,999.99")
private BigDecimal amount;
```

### Service Layer Transaction Management
- All service methods use `@Transactional` (read-only for queries)
- Custom exceptions propagate through transaction boundaries
- Business logic validation separate from entity validation
```java
@Transactional(readOnly = true)
public List<Expense> getExpensesByCategory(Long categoryId) { ... }
```

### Dual-Layout Template System
- Templates have `-classic` and `-new` suffixes
- Controlled by `app.template.use-new-layout` in `application.properties`
- `TemplateConfig` provides suffix logic: `templateConfig.getTemplateSuffix()`

### Testing Architecture
- **Unit Tests**: JUnit 5 with Mockito for service/repository testing
- **E2E Tests**: TestNG + Selenium with comprehensive Page Object Model
- **Test Builders**: Fluent builders for consistent test data creation
- **Parallel Execution**: TestNG configured for parallel test execution

## Critical Workflows

### Essential Commands
```bash
mvn spring-boot:run                  # Start app (port 8080)
mvn test                            # Run all tests
mvn test -Dtest=*SmokeTest*         # Run E2E smoke tests
mvn test -DsuiteXmlFile=src/test/resources/testng/testng.xml  # TestNG suite
```

### Development URLs
- **Web UI**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console (sa/password, jdbc:h2:mem:expensetracker)
- **REST API**: http://localhost:8080/api/expenses, /api/categories

### Testing Strategy
- **Jenkins Pipeline**: Parameterized browser testing (Chrome/Firefox)
- **Screenshot Capture**: Automatic on test failures
- **Reporting**: ExtentReports + Allure with timeline views

## Code Conventions

### Controller Patterns
- **Web Controllers**: Return template names with suffix (`"expenses/list" + templateConfig.getTemplateSuffix()`)
- **REST Controllers**: Return `ResponseEntity<Map<String, Object>>` with consistent JSON structure
- **Validation**: Use `@Valid` + `BindingResult` for form validation, custom business rules in separate methods

### Exception Handling
- **ValidationException**: For business rule violations with field-specific details
- **EntityNotFoundException**: For missing entities with entity type and ID
- **Custom validation methods**: Additional validation beyond annotations

### E2E Test Structure
- **BasePage**: Abstract base with common functionality and navigation
- **Page Objects**: Each page extends BasePage with specific locators and actions
- **Test Configuration**: TestNG XML with parallel execution and test groups (smoke, regression, e2e)

When working on this codebase:
1. **Validation**: Use custom annotations for entity validation, add business rules in services
2. **Templates**: Check `templateConfig.isUseNewLayout()` for conditional template logic
3. **Testing**: Use builders for test data, follow Page Object Model for E2E tests
4. **Transactions**: Apply `@Transactional(readOnly = true)` for query methods
5. **Controllers**: Maintain separation between web and REST controllers