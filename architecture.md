# Personal Expense Tracker - Architecture Documentation

## System Overview

The Personal Expense Tracker is a full-stack Spring Boot application that follows a layered architecture pattern with comprehensive validation, dual-interface design, and enterprise-grade testing.

## High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Browser]
        API[API Client]
    end
    
    subgraph "Presentation Layer"
        WC[Web Controllers]
        RC[REST Controllers]
        TH[Thymeleaf Templates]
        TC[TemplateConfig]
    end
    
    subgraph "Business Logic Layer"
        ES[ExpenseService]
        CS[CategoryService]
        DS[DataSeeder]
    end
    
    subgraph "Data Access Layer"
        ER[ExpenseRepository]
        CR[CategoryRepository]
    end
    
    subgraph "Data Layer"
        H2[(H2 Database)]
    end
    
    subgraph "Cross-Cutting Concerns"
        VL[Custom Validation]
        EH[Exception Handling]
        TX[Transaction Management]
    end
    
    WEB --> WC
    API --> RC
    WC --> TH
    WC --> TC
    WC --> ES
    WC --> CS
    RC --> ES
    RC --> CS
    ES --> ER
    CS --> CR
    ER --> H2
    CR --> H2
    
    ES -.-> VL
    CS -.-> VL
    ES -.-> EH
    CS -.-> EH
    ES -.-> TX
    CS -.-> TX
```

## Network Relationship Diagram (NRD)

![NetworkDiagram](image.png)

## Detailed Component Architecture

```mermaid
graph TB
    subgraph "Controller Layer"
        subgraph "Web Controllers"
            HC[HomeController]
            EC[ExpenseController]
            CC[CategoryController]
        end
        
        subgraph "REST Controllers"
            ERC[ExpenseRestController]
            CRC[CategoryRestController]
            DRC[DashboardRestController]
        end
    end
    
    subgraph "Service Layer"
        ES[ExpenseService<br/>@Transactional]
        CS[CategoryService<br/>@Transactional]
    end
    
    subgraph "Repository Layer"
        ER[ExpenseRepository<br/>extends JpaRepository]
        CR[CategoryRepository<br/>extends JpaRepository]
    end
    
    subgraph "Entity Layer"
        EE[Expense Entity<br/>@PositiveAmount<br/>@ValidColor]
        CE[Category Entity<br/>@ValidColor]
    end
    
    subgraph "Validation Layer"
        PA[@PositiveAmount]
        VC[@ValidColor]
        PAV[PositiveAmountValidator]
        VCV[ValidColorValidator]
    end
    
    subgraph "Exception Layer"
        VE[ValidationException]
        ENE[EntityNotFoundException]
    end
    
    HC --> ES
    HC --> CS
    EC --> ES
    EC --> CS
    CC --> CS
    ERC --> ES
    CRC --> CS
    DRC --> ES
    DRC --> CS
    
    ES --> ER
    CS --> CR
    ER --> EE
    CR --> CE
    
    EE --> PA
    EE --> VC
    CE --> VC
    PA --> PAV
    VC --> VCV
    
    ES --> VE
    ES --> ENE
    CS --> VE
    CS --> ENE
```

## Entity Relationship Diagram

```mermaid
erDiagram
    CATEGORY {
        bigint id PK
        varchar(100) name UK "NOT NULL"
        varchar(7) color "DEFAULT '#007bff'"
        varchar(50) icon "DEFAULT 'fas fa-tag'"
        varchar(255) description
        timestamp created_at "NOT NULL"
        timestamp updated_at "NOT NULL"
    }
    
    EXPENSE {
        bigint id PK
        varchar(255) description "NOT NULL"
        decimal(12) amount "NOT NULL"
        date expense_date "NOT NULL"
        bigint category_id FK "NOT NULL"
        timestamp created_at "NOT NULL"
        timestamp updated_at "NOT NULL"
    }
    
    CATEGORY ||--o{ EXPENSE : "has many"
```

![ER Diagram ](image-1.png)
## Template Architecture (Dual-Layout System)

```mermaid
graph TB
    subgraph "Template Configuration"
        TC[TemplateConfig]
        AP[application.properties<br/>app.template.use-new-layout]
    end
    
    subgraph "Template Selection Logic"
        TS[getTemplateSuffix()]
        CL[Classic Layout: -classic]
        NL[New Layout: -new]
    end
    
    subgraph "Template Files"
        subgraph "Expenses"
            ELC[expenses/list-classic.html]
            ELN[expenses/list-new.html]
            EFC[expenses/form-classic.html]
            EFN[expenses/form-new.html]
        end
        
        subgraph "Categories"
            CLC[categories/list-classic.html]
            CLN[categories/list-new.html]
            CFC[categories/form-classic.html]
            CFN[categories/form-new.html]
        end
        
        subgraph "Dashboard"
            HDC[home/dashboard-classic.html]
            HDN[home/dashboard-new.html]
        end
    end
    
    AP --> TC
    TC --> TS
    TS --> CL
    TS --> NL
    CL --> ELC
    CL --> EFC
    CL --> CLC
    CL --> CFC
    CL --> HDC
    NL --> ELN
    NL --> EFN
    NL --> CLN
    NL --> CFN
    NL --> HDN
```

## Testing Architecture

```mermaid
graph TB
    subgraph "Unit Testing (JUnit 5)"
        ST[Service Tests<br/>@ExtendWith(MockitoExtension)]
        RT[Repository Tests<br/>@DataJpaTest]
        CT[Controller Tests<br/>@WebMvcTest]
    end
    
    subgraph "Integration Testing"
        IT[Integration Tests<br/>@SpringBootTest]
        TC[TestConfig]
    end
    
    subgraph "E2E Testing (TestNG)"
        subgraph "Page Object Model"
            BP[BasePage]
            HP[HomePage]
            EP[ExpensesPage]
            CP[CategoriesPage]
            EFP[ExpenseFormPage]
            CFP[CategoryFormPage]
        end
        
        subgraph "Test Infrastructure"
            BT[BaseTest]
            WDC[WebDriverConfig]
            SU[ScreenshotUtils]
            AU[AssertUtils]
            WU[WaitUtils]
        end
        
        subgraph "Test Execution"
            TNGConf[testng.xml]
            ERep[ExtentReports]
            Allure[Allure Reports]
        end
    end
    
    subgraph "Test Data"
        EB[ExpenseBuilder]
        CB[CategoryBuilder]
        MDH[MockDataHelper]
    end
    
    ST --> EB
    ST --> CB
    RT --> MDH
    BP --> HP
    BP --> EP
    BP --> CP
    HP --> EFP
    EP --> EFP
    CP --> CFP
    BT --> WDC
    BT --> SU
    BP --> AU
    BP --> WU
    TNGConf --> ERep
    TNGConf --> Allure
```

## Validation Flow

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant Validator
    participant Repository
    participant Entity
    
    Client->>Controller: Submit Form/API Request
    Controller->>Controller: @Valid Annotation Processing
    Controller->>Validator: Custom Annotation Validation
    Validator->>Validator: @PositiveAmount/@ValidColor
    
    alt Validation Passes
        Controller->>Service: Call Business Method
        Service->>Service: Business Rule Validation
        
        alt Business Rules Pass
            Service->>Repository: Save/Update Entity
            Repository->>Entity: JPA Validation
            Entity->>Repository: Persist to Database
            Repository->>Service: Return Result
            Service->>Controller: Return Success
            Controller->>Client: Success Response
        else Business Rules Fail
            Service->>Controller: Throw ValidationException
            Controller->>Client: Business Error Response
        end
    else Validation Fails
        Controller->>Client: Validation Error Response
    end
```

## Data Flow Architecture

```mermaid
graph LR
    subgraph "Request Flow"
        R[Request] --> WC[Web Controller]
        R --> RC[REST Controller]
    end
    
    subgraph "Processing Flow"
        WC --> V[Validation Layer]
        RC --> V
        V --> S[Service Layer]
        S --> BL[Business Logic]
        BL --> R[Repository Layer]
        R --> DB[(Database)]
    end
    
    subgraph "Response Flow"
        DB --> R
        R --> S
        S --> WC
        S --> RC
        WC --> T[Thymeleaf Template]
        RC --> J[JSON Response]
        T --> HTML[HTML Response]
    end
```

## Technology Stack

```mermaid
graph TB
    subgraph "Frontend"
        HTML[HTML5]
        CSS[Bootstrap 5]
        JS[JavaScript/jQuery]
        TH[Thymeleaf]
        FA[Font Awesome]
        CJ[Chart.js]
    end
    
    subgraph "Backend"
        SB[Spring Boot 3.2]
        J21[Java 21]
        JPA[Spring Data JPA]
        H[Hibernate]
        V[Bean Validation]
    end
    
    subgraph "Database"
        H2[H2 Database]
    end
    
    subgraph "Testing"
        J5[JUnit 5]
        M[Mockito]
        TNG[TestNG]
        S[Selenium WebDriver]
        WDM[WebDriverManager]
        ER[ExtentReports]
        A[Allure]
    end
    
    subgraph "Build & DevOps"
        MV[Maven]
        JK[Jenkins]
        GH[GitHub Actions]
    end
```

## Key Architectural Decisions

### 1. Dual-Layout Template System

- **Decision**: Implement configurable template system with `-classic` and `-new` suffixes
- **Rationale**: Allows safe UI migration without breaking existing functionality
- **Implementation**: `TemplateConfig` component with `getTemplateSuffix()` method

### 2. Custom Validation Framework

- **Decision**: Create domain-specific validation annotations (`@PositiveAmount`, `@ValidColor`)
- **Rationale**: Encapsulate business rules in reusable, declarative annotations
- **Implementation**: Custom validator classes implementing `ConstraintValidator`

### 3. Multi-Framework Testing Strategy

- **Decision**: Use JUnit 5 for unit tests, TestNG for E2E tests
- **Rationale**: Leverage JUnit's simplicity for unit tests, TestNG's advanced features for E2E
- **Implementation**: Separate test configurations and parallel execution strategies

### 4. Separation of Web and REST Controllers

- **Decision**: Maintain separate controller classes for web and API endpoints
- **Rationale**: Clear separation of concerns, different response formats
- **Implementation**: Web controllers return template names, REST controllers return `ResponseEntity`

### 5. Transaction Management Strategy

- **Decision**: Use `@Transactional` at service layer with read-only optimization
- **Rationale**: Ensure data consistency while optimizing read operations
- **Implementation**: `@Transactional(readOnly = true)` for query methods

## Security Considerations

```mermaid
graph TB
    subgraph "Input Validation"
        CV[Custom Validation]
        BV[Bean Validation]
        BR[Business Rules]
    end
    
    subgraph "Data Protection"
        JPA[JPA Constraints]
        TX[Transaction Isolation]
        AE[Audit Fields]
    end
    
    subgraph "Error Handling"
        CE[Custom Exceptions]
        GEH[Global Exception Handler]
        ER[Error Response Standardization]
    end
    
    CV --> BV
    BV --> BR
    BR --> JPA
    JPA --> TX
    TX --> AE
    CE --> GEH
    GEH --> ER
```

## Performance Considerations

- **Lazy Loading**: JPA entities use `FetchType.LAZY` for associations
- **Connection Pooling**: H2 database with built-in connection management
- **Transaction Optimization**: Read-only transactions for query operations
- **Caching**: Template caching disabled in development mode
- **Parallel Testing**: TestNG configured for parallel test execution

## Deployment Architecture

```mermaid
graph TB
    subgraph "Development"
        DEV[Local Development<br/>mvn spring-boot:run]
        H2C[H2 Console<br/>:8080/h2-console]
    end
    
    subgraph "Testing"
        UT[Unit Tests<br/>JUnit 5]
        E2E[E2E Tests<br/>TestNG + Selenium]
        REP[Test Reports<br/>ExtentReports + Allure]
    end
    
    subgraph "CI/CD"
        JEN[Jenkins Pipeline]
        GHA[GitHub Actions]
        BR[Multi-browser Testing]
    end
    
    DEV --> H2C
    DEV --> UT
    UT --> E2E
    E2E --> REP
    REP --> JEN
    JEN --> GHA
    GHA --> BR
```

This architecture documentation provides a comprehensive view of the Personal Expense Tracker system, highlighting its key architectural patterns, component relationships, and design decisions that make it a robust, maintainable, and testable application.
