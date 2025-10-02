# GitHub Copilot Instructions for Personal Expense Tracker

## Project Context
This is a **Personal Expense Tracker** built with **Spring Boot 3.2.3** and **Java 21**. It follows a layered architecture with JPA entities, service layer business logic, and dual controller patterns (web + REST API).

## Technology Stack
- **Backend**: Spring Boot 3.2.3, Spring Data JPA, Spring MVC
- **Frontend**: Thymeleaf templates, Bootstrap 5.3.2, jQuery 3.7.1
- **Database**: H2 in-memory (development), JPA/Hibernate
- **Testing**: JUnit 5 (unit tests), TestNG + Selenium (E2E tests)
- **Build**: Maven 3.6+, Java 21

## Architecture Patterns

### Layered Architecture
```
Controllers (Web + REST API) → Services (Business Logic) → Repositories (Data Access) → Entities (Domain Model)
```

### Dual Controller Pattern
- **Web Controllers** (`*Controller.java`): Render Thymeleaf templates, handle HTML responses
- **REST Controllers** (`*RestController.java`): Return JSON, handle `/api/**` endpoints
- Example: `ExpenseController` (web) vs `ExpenseRestController` (API)

### Dual-Layout Template System
A unique configuration-driven template system allowing safe migration between UI layouts:

```java
// TemplateConfig.java - Controls template selection
public String getTemplateSuffix() {
    return useNewLayout ? "" : "-classic";  // Toggle via app.template.use-new-layout
}
```

**Template Pattern**:
- `expenses/list-classic.html` - Original templates
- `expenses/list-new.html` - Modern layout templates
- Configuration: `app.template.use-new-layout=true/false` in `application.properties`

**Usage in Controllers**:
```java
return "expenses/list" + templateConfig.getTemplateSuffix();
```

## Custom Validation Framework

### Custom Validation Annotations
The project uses domain-specific validation annotations:

**`@PositiveAmount`** - Validates positive monetary amounts
```java
@PositiveAmount(message = "Amount must be greater than zero")
@Column(nullable = false, precision = 12, scale = 2)
private BigDecimal amount;
```

**`@ValidColor`** - Validates hex color codes (#RRGGBB or #RGB)
```java
@ValidColor(message = "Please provide a valid hex color code (e.g., #FF0000)")
@Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
private String color = "#007bff";
```

**`@UniqueCategoryName`** - Ensures category name uniqueness
```java
@UniqueCategoryName(message = "Category name already exists")
@NotBlank(message = "Category name is required")
private String name;
```

### Validation Patterns
Location: `src/main/java/com/expensetracker/app/validation/`
- Annotation: `@ValidColor`, `@PositiveAmount`, `@UniqueCategoryName`
- Validator: `ValidColorValidator`, `PositiveAmountValidator`, `UniqueCategoryNameValidator`

## Entity Relationships

### Core Domain Model
```
Category (1) ←──── (Many) Expense
```

**Category Entity** (`categories` table):
- `id` (PK), `name` (unique), `color`, `icon`, `description`
- `createdAt`, `updatedAt` (audit fields)
- Relationship: `@OneToMany(mappedBy = "category")` with Expense

**Expense Entity** (`expenses` table):
- `id` (PK), `description`, `amount` (BigDecimal), `expenseDate`
- `createdAt`, `updatedAt` (audit fields)
- Relationship: `@ManyToOne` with Category (`category_id` FK)

### Audit Fields Pattern
All entities use `@PrePersist` and `@PreUpdate` lifecycle callbacks:
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}
```

## Service Layer Patterns

### Transaction Management
- Use `@Transactional` for write operations
- Use `@Transactional(readOnly = true)` for read operations
- Business logic and validation belong in services, not controllers

**Example**:
```java
@Transactional(readOnly = true)
public List<Expense> getAllExpenses() { ... }

@Transactional
public Expense saveExpense(Expense expense) { ... }
```

### Custom Exceptions
Location: `src/main/java/com/expensetracker/app/exception/`
- `EntityNotFoundException` - Entity not found by ID
- `ValidationException` - Business rule validation failures
- `DuplicateResourceException` - Unique constraint violations

**Pattern**:
```java
throw new EntityNotFoundException("Category", id);
throw new ValidationException("Category", "name", "Category name must be unique");
```

## Multi-Framework Testing Strategy

### JUnit 5 (Unit & Integration Tests)
Location: `src/test/java/com/expensetracker/app/`
- **Repository Tests**: `@DataJpaTest`, test data access layer
- **Service Tests**: `@SpringBootTest` or `@ExtendWith(MockitoExtension.class)`
- **Controller Tests**: `@WebMvcTest`, MockMvc for endpoint testing

**Example**:
```java
@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
}
```

### TestNG + Selenium (E2E Tests)
Location: `src/test/java/com/expensetracker/app/e2e/`
- **Page Object Model**: Separate page objects from test logic
- **Parallel Execution**: TestNG parallel test execution
- **Test Reporting**: Allure + ExtentReports integration

**Page Object Pattern**:
```java
public class ExpenseListPage extends BasePage {
    @FindBy(id = "expense-table")
    private WebElement expenseTable;
    
    public int getExpenseCount() { ... }
}
```

### Test Data Builders
Pattern: Fluent builder pattern for test data creation
```java
Expense expense = ExpenseBuilder.builder()
    .description("Test Expense")
    .amount(new BigDecimal("100.00"))
    .expenseDate(LocalDate.now())
    .category(category)
    .build();
```

## REST API Conventions

### Response Patterns
- Success: `ResponseEntity.ok(data)` with status 200
- Created: `ResponseEntity.status(HttpStatus.CREATED).body(resource)` with status 201
- Error: `ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap)`

### Error Response Structure
```java
Map<String, Object> errorResponse = new HashMap<>();
errorResponse.put("status", "error");
errorResponse.put("message", "User-friendly message");
errorResponse.put("error", e.getMessage());
errorResponse.put("timestamp", LocalDateTime.now());
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
```

### API Endpoints
- Base path: `/api/**`
- Example: `/api/expenses`, `/api/categories`, `/api/dashboard`

## Build & Development Commands

### Maven Commands
```bash
mvn clean compile              # Compile source code
mvn test                       # Run JUnit tests
mvn test -Dtest=ClassName      # Run specific test
mvn spring-boot:run            # Start application
mvn clean package              # Build JAR
```

### Testing Commands
```bash
mvn test                                    # Run all JUnit tests
mvn test -Dgroups=unit                      # Run unit tests only
mvn test -Dgroups=integration               # Run integration tests
mvn test -Dtest=ExpenseServiceTest          # Run specific test class
```

### Application URLs
- Web Interface: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:expensetracker`
  - Username: `sa`
  - Password: `password`
- REST API: `http://localhost:8080/api`

## Configuration

### Application Properties
Location: `src/main/resources/application.properties`

**Key Configuration**:
```properties
# Template Layout System
app.template.use-new-layout=false     # Toggle template layout
app.template.layout-variant=main      # Layout variant name

# Database
spring.datasource.url=jdbc:h2:mem:expensetracker
spring.jpa.hibernate.ddl-auto=update

# Server
server.port=8080
```

## Code Conventions

### Financial Calculations
- **Always use `BigDecimal`** for monetary amounts, never `double` or `float`
- Use precision and scale: `@Column(precision = 12, scale = 2)`
- Example: `new BigDecimal("100.00")` (String constructor for precision)

### Naming Conventions
- **Entities**: Singular names (`Category`, `Expense`)
- **Tables**: Plural names (`categories`, `expenses`)
- **Service methods**: `saveExpense()`, `getAllCategories()`, `deleteById()`
- **Repository methods**: `findByExpenseDateBetween()`, `getTotalExpenses()`

### Date Handling
- Use `LocalDate` for dates without time
- Use `LocalDateTime` for timestamps
- Use `@PastOrPresent` for expense dates validation

## Project-Specific Patterns to Follow

### Controller Method Patterns
```java
// Web Controller - Returns view name
@GetMapping("/expenses")
public String listExpenses(Model model) {
    model.addAttribute("expenses", expenseService.getAllExpenses());
    return "expenses/list" + templateConfig.getTemplateSuffix();
}

// REST Controller - Returns ResponseEntity
@GetMapping("/api/expenses")
public ResponseEntity<List<Expense>> getAllExpenses() {
    return ResponseEntity.ok(expenseService.getAllExpenses());
}
```

### Service Layer Business Rules
- Validate entity relationships (Category must exist before creating Expense)
- Enforce business constraints (expense date cannot be in future)
- Handle cascading operations carefully (check dependencies before delete)

### Template Fragment Pattern
Location: `src/main/resources/templates/fragments/`
- Reusable components: `layout.html`, `header.html`, `footer.html`
- Include with: `th:replace="~{fragments/layout :: header}"`

## Jenkins CI/CD Integration
Location: `Jenkinsfile`
- Parameterized browser testing (Chrome, Firefox, Edge)
- Parallel test execution
- Allure report generation

## Example Prompts for GitHub Copilot

### Entity Generation
```
Create a JPA entity for [EntityName] with fields: [list fields]
Add validation annotations and audit fields (createdAt, updatedAt)
Include proper JPA relationships
```

### Service Method Generation
```
Create a service method to [describe business operation]
Include @Transactional annotation
Add proper exception handling with custom exceptions
Add validation for business rules
```

### Test Case Generation
```
Generate a JUnit 5 test for [ClassName].[methodName]
Use @DataJpaTest for repository tests
Use MockMvc for controller tests
Include edge cases and validation scenarios
```

### REST Endpoint Generation
```
Create a REST endpoint for [operation]
Return ResponseEntity with proper HTTP status codes
Include error handling with consistent response structure
Add logging for important operations
```

## Key Files for Reference

- **Configuration**: `src/main/java/com/expensetracker/app/config/TemplateConfig.java`
- **Main Entity**: `src/main/java/com/expensetracker/app/entity/Expense.java`
- **Service Pattern**: `src/main/java/com/expensetracker/app/service/ExpenseService.java`
- **Web Controller**: `src/main/java/com/expensetracker/app/controller/ExpenseController.java`
- **REST Controller**: `src/main/java/com/expensetracker/app/controller/ExpenseRestController.java`
- **Repository**: `src/main/java/com/expensetracker/app/repository/ExpenseRepository.java`
- **Custom Validation**: `src/main/java/com/expensetracker/app/validation/PositiveAmount.java`
- **Documentation**: `docs/DUAL_LAYOUT_SYSTEM.md`, `docs/E2E_TESTING_FRAMEWORK.md`

## Quick Reference

### When to Use What
- **`@Transactional`**: Service layer write operations
- **`@Transactional(readOnly = true)`**: Service layer read operations
- **BigDecimal**: All monetary calculations
- **Custom Exceptions**: Domain-specific error handling
- **Builder Pattern**: Test data creation
- **Page Object Model**: E2E test page interactions
- **Template Suffix**: Dynamic template selection based on configuration
