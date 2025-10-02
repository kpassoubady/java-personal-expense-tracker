# Personal Expense Tracker - Architecture Documentation

This document provides a comprehensive overview of the Personal Expense Tracker application architecture, including system design, component relationships, data flows, and technology stack.

## Table of Contents
1. [High-Level Architecture](#high-level-architecture)
2. [Entity Relationship Diagram](#entity-relationship-diagram)
3. [Detailed Component Architecture](#detailed-component-architecture)
4. [Template Architecture (Dual-Layout System)](#template-architecture-dual-layout-system)
5. [Testing Architecture](#testing-architecture)
6. [Data Flow Diagram](#data-flow-diagram)
7. [Validation Flow](#validation-flow)
8. [Technology Stack](#technology-stack)
9. [Deployment Architecture](#deployment-architecture)
10. [Architectural Decisions](#architectural-decisions)

---

## High-Level Architecture

The application follows a classic **layered architecture** pattern with clear separation of concerns:

```mermaid
graph TB
    subgraph "Client Layer"
        Browser[Web Browser]
        Mobile[Mobile Browser]
    end
    
    subgraph "Presentation Layer"
        WebUI[Thymeleaf Templates<br/>Bootstrap 5 UI]
        RestAPI[REST API<br/>JSON Responses]
    end
    
    subgraph "Controller Layer"
        WebCtrl[Web Controllers<br/>HTML Responses]
        RestCtrl[REST Controllers<br/>/api/** endpoints]
    end
    
    subgraph "Business Logic Layer"
        Services[Service Layer<br/>@Transactional]
        Validation[Custom Validators<br/>@PositiveAmount, @ValidColor]
        Exceptions[Custom Exceptions<br/>EntityNotFoundException]
    end
    
    subgraph "Data Access Layer"
        Repos[JPA Repositories<br/>Spring Data JPA]
        Entities[JPA Entities<br/>Category, Expense]
    end
    
    subgraph "Data Layer"
        H2[(H2 Database<br/>In-Memory)]
    end
    
    Browser --> WebUI
    Mobile --> WebUI
    Browser --> RestAPI
    Mobile --> RestAPI
    
    WebUI --> WebCtrl
    RestAPI --> RestCtrl
    
    WebCtrl --> Services
    RestCtrl --> Services
    
    Services --> Validation
    Services --> Exceptions
    Services --> Repos
    
    Repos --> Entities
    Entities --> H2
    
    style Services fill:#90EE90
    style WebCtrl fill:#87CEEB
    style RestCtrl fill:#87CEEB
    style H2 fill:#FFD700
```

---

## Entity Relationship Diagram

The domain model consists of two core entities with a one-to-many relationship:

```mermaid
erDiagram
    CATEGORY ||--o{ EXPENSE : contains
    
    CATEGORY {
        bigint id PK "Auto-generated"
        varchar name UK "Unique, max 100 chars"
        varchar color "Hex color code, default #007bff"
        varchar icon "Font Awesome icon, default fas fa-tag"
        varchar description "Max 255 chars"
        timestamp created_at "Auto-set on creation"
        timestamp updated_at "Auto-updated"
    }
    
    EXPENSE {
        bigint id PK "Auto-generated"
        varchar description "Required, max 255 chars"
        decimal amount "Precision 12, scale 2, positive"
        date expense_date "Required, past or present"
        bigint category_id FK "Required, references Category"
        timestamp created_at "Auto-set on creation"
        timestamp updated_at "Auto-updated"
    }
```

### Entity Details

**Category Entity**:
- **Purpose**: Organize expenses into logical groups (Food, Transportation, etc.)
- **Validations**: 
  - `@NotBlank` name, unique constraint
  - `@ValidColor` custom annotation for hex colors
  - `@Size` constraints on description and icon
- **Relationships**: `@OneToMany` with Expense (cascade all, orphan removal)
- **Audit**: Automatic `createdAt` and `updatedAt` timestamps

**Expense Entity**:
- **Purpose**: Represent individual expense transactions
- **Validations**: 
  - `@NotNull` amount, description, date, category
  - `@PositiveAmount` custom annotation for positive values
  - `@PastOrPresent` ensures date is not in future
  - `@DecimalMin`, `@DecimalMax`, `@Digits` for amount constraints
- **Relationships**: `@ManyToOne` with Category (lazy fetch)
- **Audit**: Automatic `createdAt` and `updatedAt` timestamps

---

## Detailed Component Architecture

```mermaid
graph TB
    subgraph "Controller Layer"
        HomeCtrl[HomeController<br/>Dashboard views]
        ExpCtrl[ExpenseController<br/>Expense web pages]
        CatCtrl[CategoryController<br/>Category web pages]
        ExpAPI[ExpenseRestController<br/>/api/expenses]
        CatAPI[CategoryRestController<br/>/api/categories]
        DashAPI[DashboardRestController<br/>/api/dashboard]
    end
    
    subgraph "Service Layer"
        ExpSvc[ExpenseService<br/>Business logic]
        CatSvc[CategoryService<br/>Business logic]
    end
    
    subgraph "Repository Layer"
        ExpRepo[ExpenseRepository<br/>extends JpaRepository]
        CatRepo[CategoryRepository<br/>extends JpaRepository]
    end
    
    subgraph "Configuration"
        TmplConfig[TemplateConfig<br/>Dual-layout control]
        AppConfig[Application Properties]
    end
    
    subgraph "Validation"
        PosAmt[PositiveAmountValidator]
        ValidCol[ValidColorValidator]
        UniqName[UniqueCategoryNameValidator]
    end
    
    HomeCtrl --> ExpSvc
    HomeCtrl --> CatSvc
    HomeCtrl --> TmplConfig
    
    ExpCtrl --> ExpSvc
    ExpCtrl --> TmplConfig
    
    CatCtrl --> CatSvc
    CatCtrl --> TmplConfig
    
    ExpAPI --> ExpSvc
    CatAPI --> CatSvc
    DashAPI --> ExpSvc
    DashAPI --> CatSvc
    
    ExpSvc --> ExpRepo
    ExpSvc --> CatRepo
    CatSvc --> CatRepo
    
    ExpSvc --> PosAmt
    CatSvc --> ValidCol
    CatSvc --> UniqName
    
    TmplConfig --> AppConfig
    
    style ExpSvc fill:#90EE90
    style CatSvc fill:#90EE90
    style TmplConfig fill:#FFB6C1
```

### Component Responsibilities

**Controllers**:
- **Web Controllers**: Render Thymeleaf templates, handle form submissions
- **REST Controllers**: Return JSON responses, handle API requests
- **Separation**: `/expenses` (web) vs `/api/expenses` (API)

**Services**:
- Business logic and validation
- Transaction management (`@Transactional`)
- Exception handling (custom exceptions)
- Cross-entity operations

**Repositories**:
- Data access using Spring Data JPA
- Custom query methods (derived and `@Query`)
- Example: `findByExpenseDateBetween()`, `getTotalExpenses()`

**Validators**:
- Custom validation annotations
- Validator classes implementing `ConstraintValidator`
- Domain-specific validation rules

---

## Template Architecture (Dual-Layout System)

A unique feature allowing safe migration between template systems:

```mermaid
graph LR
    subgraph "Configuration"
        AppProps[application.properties<br/>use-new-layout=true/false]
        TmplConfig[TemplateConfig<br/>getTemplateSuffix]
    end
    
    subgraph "Controller Logic"
        Ctrl[Controller Method]
        Decision{use-new-layout?}
    end
    
    subgraph "Template Files"
        Classic[expenses/list-classic.html<br/>Original layout]
        New[expenses/list-new.html<br/>Modern layout]
    end
    
    AppProps --> TmplConfig
    Ctrl --> TmplConfig
    TmplConfig --> Decision
    
    Decision -->|false| Classic
    Decision -->|true| New
    
    Classic --> Render[Rendered HTML]
    New --> Render
    
    style TmplConfig fill:#FFB6C1
    style Classic fill:#87CEEB
    style New fill:#90EE90
```

### Template System Features

**Configuration-Driven**:
```properties
app.template.use-new-layout=false  # Use classic templates
app.template.use-new-layout=true   # Use new layout system
```

**Controller Pattern**:
```java
return "expenses/list" + templateConfig.getTemplateSuffix();
// Returns: "expenses/list-classic" or "expenses/list-new"
```

**Benefits**:
- Zero-downtime migration capability
- Immediate rollback if issues arise
- Progressive enhancement approach
- A/B testing capabilities

**File Structure**:
```
templates/
├── categories/
│   ├── list-classic.html
│   ├── list-new.html
│   ├── form-classic.html
│   └── form-new.html
├── expenses/
│   ├── list-classic.html
│   ├── list-new.html
│   ├── form-classic.html
│   └── form-new.html
└── layout/
    └── main.html (new layout template)
```

---

## Testing Architecture

Multi-framework testing strategy for comprehensive coverage:

```mermaid
graph TB
    subgraph "Unit Tests - JUnit 5"
        ServiceTests[Service Tests<br/>@SpringBootTest or Mock]
        RepoTests[Repository Tests<br/>@DataJpaTest]
        ValidTests[Validator Tests<br/>@ExtendWith MockitoExtension]
    end
    
    subgraph "Integration Tests - JUnit 5"
        CtrlTests[Controller Tests<br/>@WebMvcTest + MockMvc]
        IntTests[Integration Tests<br/>@SpringBootTest]
    end
    
    subgraph "E2E Tests - TestNG + Selenium"
        PageObjects[Page Object Model<br/>BasePage, ExpenseListPage]
        E2ETests[TestNG Tests<br/>Parallel execution]
        Browsers[Multi-browser Testing<br/>Chrome, Firefox, Edge]
    end
    
    subgraph "Test Reporting"
        Allure[Allure Reports<br/>Test results]
        Extent[ExtentReports<br/>HTML reports]
    end
    
    ServiceTests --> IntTests
    RepoTests --> IntTests
    ValidTests --> ServiceTests
    
    CtrlTests --> E2ETests
    IntTests --> E2ETests
    
    E2ETests --> PageObjects
    E2ETests --> Browsers
    
    E2ETests --> Allure
    E2ETests --> Extent
    
    style ServiceTests fill:#90EE90
    style E2ETests fill:#FFB6C1
    style Allure fill:#FFD700
```

### Testing Strategy

**Unit Tests (JUnit 5)**:
- **Repository**: `@DataJpaTest` with `TestEntityManager`
- **Service**: Mock repositories, test business logic
- **Validator**: Test validation rules in isolation

**Integration Tests (JUnit 5)**:
- **Controller**: `@WebMvcTest` with `MockMvc`
- **Full Stack**: `@SpringBootTest` with embedded H2

**E2E Tests (TestNG + Selenium)**:
- **Page Object Model**: Separate page structure from test logic
- **Parallel Execution**: Multiple tests run concurrently
- **Multi-Browser**: Chrome, Firefox, Edge support

**Test Data Builders**:
```java
// Fluent builder pattern for test data
Expense expense = ExpenseBuilder.builder()
    .description("Test Expense")
    .amount(new BigDecimal("100.00"))
    .build();
```

---

## Data Flow Diagram

Request/response flow through the application:

```mermaid
sequenceDiagram
    participant Browser
    participant Controller
    participant Service
    participant Validator
    participant Repository
    participant Database
    
    Browser->>Controller: HTTP Request<br/>(GET/POST/PUT/DELETE)
    
    Controller->>Service: Call service method
    
    Service->>Validator: Validate input
    Validator-->>Service: Validation result
    
    alt Validation Failed
        Service-->>Controller: Throw ValidationException
        Controller-->>Browser: Error response (400)
    end
    
    Service->>Repository: Query/save entity
    Repository->>Database: SQL query
    Database-->>Repository: Result set
    Repository-->>Service: Entity/entities
    
    Service-->>Controller: Business result
    
    alt Web Request
        Controller-->>Browser: Thymeleaf template (HTML)
    else API Request
        Controller-->>Browser: JSON response
    end
```

### Request Flow Details

1. **Web Request**: Browser → Controller → Service → Repository → Database
2. **Validation**: Happens at multiple layers (annotation, service, controller)
3. **Transaction**: Service layer manages transactions
4. **Response**: HTML (web) or JSON (API)

---

## Validation Flow

Multi-layer validation strategy:

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant AnnotationVal as Annotation Validators<br/>@PositiveAmount, @ValidColor
    participant Service
    participant BusinessVal as Business Validators<br/>Service Layer
    participant Repository
    
    Client->>Controller: Submit form/API request
    
    Controller->>AnnotationVal: Validate @Valid entity
    
    alt Annotation Validation Failed
        AnnotationVal-->>Controller: BindingResult errors
        Controller-->>Client: 400 Bad Request
    end
    
    Controller->>Service: Call service method
    
    Service->>BusinessVal: Validate business rules<br/>- Unique category name<br/>- Category exists<br/>- Date not in future
    
    alt Business Validation Failed
        BusinessVal-->>Service: Throw ValidationException
        Service-->>Controller: Exception
        Controller-->>Client: 400/422 Error
    end
    
    Service->>Repository: Save entity
    Repository-->>Service: Saved entity
    Service-->>Controller: Success result
    Controller-->>Client: 200/201 Success
```

### Validation Layers

**Layer 1: Annotation Validation**
- `@NotNull`, `@NotBlank`, `@Size`, `@Pattern`
- Custom: `@PositiveAmount`, `@ValidColor`, `@UniqueCategoryName`

**Layer 2: Service Layer Business Rules**
- Entity relationships (Category must exist)
- Business constraints (no future dates)
- Complex validations (duplicate checks)

**Layer 3: Database Constraints**
- Unique constraints (category name)
- Foreign key constraints (category_id)
- Not null constraints

---

## Technology Stack

Complete technology overview:

```mermaid
graph TB
    subgraph "Frontend Technologies"
        HTML[HTML5 + Semantic Markup]
        Thymeleaf[Thymeleaf 3.x<br/>Server-side templating]
        Bootstrap[Bootstrap 5.3.2<br/>Responsive UI]
        jQuery[jQuery 3.7.1<br/>AJAX & DOM]
        FontAwesome[Font Awesome<br/>Icons]
    end
    
    subgraph "Backend Framework"
        SpringBoot[Spring Boot 3.2.3]
        SpringMVC[Spring MVC<br/>Web framework]
        SpringData[Spring Data JPA<br/>Repository layer]
        Hibernate[Hibernate<br/>ORM]
    end
    
    subgraph "Database"
        H2[H2 Database<br/>In-memory dev DB]
        JPA[JPA 3.x<br/>Persistence API]
    end
    
    subgraph "Testing"
        JUnit5[JUnit 5<br/>Unit tests]
        TestNG[TestNG<br/>E2E tests]
        Selenium[Selenium WebDriver<br/>Browser automation]
        Mockito[Mockito<br/>Mocking framework]
        MockMvc[MockMvc<br/>Controller tests]
    end
    
    subgraph "Build & CI/CD"
        Maven[Maven 3.6+<br/>Build tool]
        Java21[Java 21<br/>Runtime]
        Jenkins[Jenkins<br/>CI/CD pipeline]
        Allure[Allure<br/>Test reporting]
    end
    
    HTML --> Thymeleaf
    Thymeleaf --> Bootstrap
    Thymeleaf --> jQuery
    Bootstrap --> FontAwesome
    
    Thymeleaf --> SpringMVC
    SpringMVC --> SpringBoot
    SpringData --> SpringBoot
    SpringData --> Hibernate
    
    Hibernate --> JPA
    JPA --> H2
    
    JUnit5 --> Mockito
    JUnit5 --> MockMvc
    TestNG --> Selenium
    
    Maven --> SpringBoot
    Maven --> JUnit5
    Maven --> TestNG
    Java21 --> SpringBoot
    
    Jenkins --> Maven
    Jenkins --> Allure
    
    style SpringBoot fill:#90EE90
    style H2 fill:#FFD700
    style JUnit5 fill:#87CEEB
```

### Version Details

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Primary language |
| Spring Boot | 3.2.3 | Application framework |
| Spring Data JPA | Included in Boot | Data access |
| Hibernate | Included in Boot | ORM provider |
| H2 Database | Runtime | Development database |
| Thymeleaf | 3.x | Template engine |
| Bootstrap | 5.3.2 | CSS framework |
| jQuery | 3.7.1 | JavaScript library |
| JUnit | 5.x | Unit testing |
| TestNG | Latest | E2E testing |
| Selenium | 4.x | Browser automation |
| Maven | 3.6+ | Build tool |

---

## Deployment Architecture

Application deployment and runtime environment:

```mermaid
graph TB
    subgraph "Development Environment"
        IDE[IDE - IntelliJ/Eclipse/VSCode]
        LocalMaven[Maven Build]
        LocalDB[H2 In-Memory DB]
    end
    
    subgraph "CI/CD Pipeline"
        Git[Git Repository<br/>GitHub]
        Jenkins[Jenkins Server]
        MavenCI[Maven Build<br/>Unit + Integration Tests]
        E2ETests[E2E Test Suite<br/>Selenium Grid]
        Reports[Test Reports<br/>Allure + ExtentReports]
    end
    
    subgraph "Runtime Environment"
        AppServer[Embedded Tomcat<br/>Port 8080]
        SpringApp[Spring Boot Application]
        H2Runtime[H2 Database<br/>In-Memory]
        H2Console[H2 Console<br/>Port 8080/h2-console]
    end
    
    subgraph "Access Points"
        WebUI[Web Interface<br/>localhost:8080]
        RestAPI[REST API<br/>localhost:8080/api]
        DBConsole[Database Console<br/>localhost:8080/h2-console]
    end
    
    IDE --> LocalMaven
    LocalMaven --> LocalDB
    
    IDE --> Git
    Git --> Jenkins
    Jenkins --> MavenCI
    MavenCI --> E2ETests
    E2ETests --> Reports
    
    MavenCI --> SpringApp
    SpringApp --> AppServer
    SpringApp --> H2Runtime
    H2Runtime --> H2Console
    
    AppServer --> WebUI
    AppServer --> RestAPI
    H2Runtime --> DBConsole
    
    style SpringApp fill:#90EE90
    style Jenkins fill:#FFB6C1
    style H2Runtime fill:#FFD700
```

### Deployment Details

**Development Setup**:
1. Clone repository
2. Run `mvn clean compile`
3. Run `mvn spring-boot:run`
4. Access at `http://localhost:8080`

**CI/CD Pipeline**:
1. Code pushed to GitHub
2. Jenkins triggers build
3. Maven compiles and runs tests
4. E2E tests execute in parallel
5. Allure reports generated
6. Artifacts archived

**Production Considerations**:
- Replace H2 with production database (PostgreSQL, MySQL)
- Configure external application properties
- Enable production profiles
- Set up proper logging and monitoring

---

## Architectural Decisions

### Why Dual-Layout Template System?

**Problem**: Need to modernize UI without disrupting existing functionality

**Solution**: Configuration-driven template switching
- Original templates preserved with `-classic` suffix
- New templates available with `-new` suffix
- Single property toggle: `app.template.use-new-layout`
- Zero-downtime migration capability

**Benefits**:
- Safe migration path
- Immediate rollback if needed
- Progressive enhancement approach
- A/B testing capabilities

### Why Dual Controller Pattern?

**Problem**: Different clients need different response formats (HTML vs JSON)

**Solution**: Separate web and REST controllers
- **Web Controllers**: Return view names, render Thymeleaf templates
- **REST Controllers**: Return JSON, handle API requests

**Benefits**:
- Clear separation of concerns
- Different error handling strategies
- Easier to maintain and test
- Supports multiple client types

### Why Multi-Framework Testing?

**Problem**: Need comprehensive testing coverage at different levels

**Solution**: JUnit 5 for unit/integration, TestNG for E2E
- **JUnit 5**: Fast unit tests, repository tests, controller tests
- **TestNG**: Parallel E2E tests, better reporting, data providers

**Benefits**:
- Each framework used for its strengths
- Comprehensive test coverage
- Parallel execution for faster feedback
- Multiple reporting options

### Why Custom Validation Annotations?

**Problem**: Domain-specific validation rules not covered by standard annotations

**Solution**: Custom validation annotations with validator classes
- `@PositiveAmount` - Financial validation
- `@ValidColor` - Hex color validation
- `@UniqueCategoryName` - Database-level uniqueness

**Benefits**:
- Reusable validation logic
- Consistent error messages
- Domain-specific rules
- Declarative validation

### Why BigDecimal for Amounts?

**Problem**: Floating-point arithmetic is imprecise for financial calculations

**Solution**: Always use `BigDecimal` for monetary amounts
- Precision: 12 digits
- Scale: 2 decimal places
- String constructor for exact values

**Benefits**:
- Accurate financial calculations
- No rounding errors
- Industry best practice
- Compliance with financial standards

---

## Security Considerations

**Input Validation**:
- Multi-layer validation (annotation + service + database)
- Custom validators for domain-specific rules
- Protection against malicious input

**Error Handling**:
- Custom exceptions with appropriate error messages
- Consistent error response structure
- No sensitive information in error responses

**Database**:
- Prepared statements via JPA (SQL injection protection)
- Unique constraints on sensitive fields
- Audit fields for tracking changes

---

## Performance Considerations

**Database**:
- Lazy loading for relationships (`FetchType.LAZY`)
- Connection pooling via HikariCP (Spring Boot default)
- In-memory H2 for fast development

**Caching**:
- Second-level cache potential (not currently enabled)
- Browser caching for static resources
- Template caching in production

**Query Optimization**:
- Custom repository queries for complex operations
- Pagination support for large result sets
- Efficient SQL generation via Hibernate

---

## Future Enhancements

**Planned Improvements**:
- [ ] Move to production database (PostgreSQL/MySQL)
- [ ] Add Spring Security for authentication/authorization
- [ ] Implement caching layer (Redis/Ehcache)
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Implement file upload for receipts
- [ ] Add export functionality (PDF/Excel)
- [ ] Implement budget tracking and alerts
- [ ] Add multi-user support with user management

---

## Conclusion

The Personal Expense Tracker demonstrates a well-architected Spring Boot application with:

✅ **Clean Architecture**: Clear separation of concerns across layers  
✅ **Flexible Design**: Dual-layout system for safe UI migration  
✅ **Comprehensive Testing**: Multi-framework approach for all test levels  
✅ **Custom Validation**: Domain-specific validation framework  
✅ **Best Practices**: Transaction management, error handling, audit trails  
✅ **Modern Stack**: Spring Boot 3, Java 21, Bootstrap 5

The architecture supports maintainability, testability, and scalability while providing a solid foundation for future enhancements.
