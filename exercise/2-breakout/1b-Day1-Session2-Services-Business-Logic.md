# 🏗️ Day 1 - Session 2: Personal Expense Tracker - Services & Business Logic (45 mins)

## 🎯 Learning Objectives

By the end of this session, you will:

- Create service layer with comprehensive business logic using **GitHub Copilot assistance**
- Implement custom exceptions with **Copilot-generated error handling patterns**
- Add validation using **AI-assisted code completion**
- Create unit tests with **Copilot's testing suggestions**
- Practice **intermediate GitHub Copilot techniques** from the [Copilot Mastery Guide](../0-copilot-mastery-guide.md)
- Seed database with **Copilot-generated sample data**

**⏱️ Time Allocation: 45 minutes (Extended for comprehensive Copilot integration)**

---

## 🤖 GitHub Copilot Integration - Session 2 Focus

This session emphasizes **Intermediate Copilot Techniques**:

### 🎯 Skills You'll Practice

- **Pattern-Based Generation**: Using Copilot for service layer patterns
- **Context-Aware Suggestions**: Leveraging existing code for better completions
- **Test-Driven Development**: AI-assisted unit test creation
- **Error Handling Patterns**: Generating robust exception handling
- **Data Seeding**: Creating realistic sample data with AI

### 🔧 Key Copilot Features for This Session

- **Multi-line completions** for service method implementations
- **#selection context** for enhancing specific code blocks  
- **Method signature completion** from JavaDoc comments
- **Test case generation** from existing service methods
- **Validation logic patterns** for business rules

> 📖 **Reference**: See [Section 2: Intermediate Techniques](../0-copilot-mastery-guide.md#section-2-intermediate-techniques) for detailed guidance

---

## 📋 Prerequisites Check (3 minutes)

- ✅ Session 1 completed successfully
- ✅ Entities and repositories working  
- ✅ Application starts without errors
- ✅ H2 database accessible
- ✅ **GitHub Copilot activated** and showing suggestions

**🤖 Copilot Warm-up Exercise**:

1. Open any Java file from Session 1
2. Type `// TODO: Add validation for` and observe Copilot suggestions
3. Verify you can see multiple completion options with `Ctrl/Cmd + Enter`

**Quick Test**: Verify your foundation is solid:

```bash
# Ensure app still starts
mvn spring-boot:run

# Check in another terminal  
curl http://localhost:8080/h2-console
```

---

## 🚀 Session Overview

## 🚀 Session Overview

In this session, we'll add the business logic layer that makes your expense tracker intelligent using **GitHub Copilot's pattern recognition** capabilities. We'll focus on essential services, error handling, and testing to ensure everything works reliably.

### 🎯 What You'll Build (45 minutes)

- **Custom Exceptions**: Professional error handling with **Copilot-generated patterns**
- **Category Service**: CRUD operations with **AI-assisted validation**
- **Expense Service**: Business logic with **Copilot's relationship suggestions**
- **Unit Tests**: **Copilot-generated test cases** for service methods
- **Data Seeding**: **AI-created sample data** for development

### 🤖 Copilot Integration Milestones

Each step includes specific Copilot techniques:

1. **Pattern Generation**: Exception class templates
2. **Service Layer Completion**: Business logic suggestions
3. **Test Creation**: Automated test case generation
4. **Data Generation**: Realistic sample data creation

---

## 📝 Step 1: Create Custom Exceptions with Copilot (8 minutes)

### 🚨 Exception Classes - AI-Assisted Creation

Create proper error handling foundation using Copilot's pattern recognition:

**🤖 Copilot Exercise 1A: Exception Pattern Generation**

1. **Create** `src/main/java/com/expensetracker/app/exception/EntityNotFoundException.java`

2. **Start with intentional prompt** (Type this exact comment):

   ```java
   package com.expensetracker.app.exception;
   
   /**
    * Custom exception for entity not found scenarios
    * Provides static factory methods for common cases
    */
   // Exception class that extends RuntimeException with constructors and static factory methods
   ```

3. **Let Copilot generate** the complete class structure
4. **Press `Ctrl/Cmd + Enter`** to see multiple suggestions
5. **Choose the most comprehensive option**

**🤖 Copilot Exercise 1B: Validation Exception**

1. **Create** `src/main/java/com/expensetracker/app/exception/ValidationException.java`

2. **Use descriptive comment** to guide Copilot:

   ```java
   package com.expensetracker.app.exception;
   
   /**
    * Exception for validation failures in business logic
    * Used when data doesn't meet business rules
    */
   // RuntimeException subclass for validation errors with message and cause constructors
   ```

**🎯 Expected Copilot Behavior**: Copilot should generate:

- Proper inheritance from RuntimeException
- Multiple constructor overloads
- Static factory methods for common scenarios
- Professional JavaDoc comments

**💡 Pro Tip**: If Copilot's first suggestion isn't complete, try adding more descriptive comments like `// Add static factory method for not found with ID`
**🎯 Expected Copilot Behavior**:

Copilot should generate:

- Proper inheritance from RuntimeException
- Multiple constructor overloads
- Static factory methods for common scenarios  
- Professional JavaDoc comments

**💡 Pro Tip**: If Copilot's first suggestion isn't complete, try adding more descriptive comments like `// Add static factory method for not found with ID`

---

## 📝 Step 2: Create Category Service with AI Assistance (12 minutes)

### 🏢 Category Service Implementation - Pattern-Based Generation

Build comprehensive category business logic using Copilot's service pattern recognition:

**🤖 Copilot Exercise 2A: Service Class Structure**

1. **Create** `src/main/java/com/expensetracker/app/service/CategoryService.java`

2. **Start with class-level context** (Type this template):

   ```java
   package com.expensetracker.app.service;
   
   import com.expensetracker.app.entity.Category;
   import com.expensetracker.app.repository.CategoryRepository;
   import com.expensetracker.app.exception.EntityNotFoundException;
   import com.expensetracker.app.exception.ValidationException;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Service;
   import org.springframework.transaction.annotation.Transactional;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   
   import java.util.List;
   import java.util.Map;
   
   /**
    * Service layer for Category management with comprehensive business logic
    * Includes CRUD operations, validation, and analytics
    */
   @Service
   @Transactional
   public class CategoryService {
       
       private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
       
       @Autowired
       private CategoryRepository categoryRepository;
       
       // Generate CRUD methods with validation and exception handling
   ```

3. **Let Copilot complete each method** by adding descriptive comments:

   ```java
   /**
    * Saves a new category with duplicate name validation
    * @param category the category to save
    * @return saved category
    * @throws ValidationException if name already exists
    */
   // Method to save category with duplicate name checking
   ```

**🤖 Copilot Exercise 2B: Business Logic Methods**

Continue adding business methods with Copilot assistance:

```java
// Method to get all categories ordered by name

// Method to get category by ID with exception if not found  

// Method to update category with validation

// Method to delete category checking for expense relationships

// Method to get total category count

// Method to find categories by partial name match

// Method to get category statistics with expense counts
```

**🎯 Copilot Pattern Recognition**: Copilot should suggest:

- `@Transactional(readOnly = true)` for query methods
- Proper exception throwing with custom messages
- Validation logic for business rules
- Repository method calls with error handling
- Logging statements for debugging

**🤖 Copilot Exercise 2C: Quick Test Method**

Add a test method to verify service works:

```java
/**
 * Quick test method to verify category service functionality
 * Remove this after comprehensive tests are added
 */
@PostConstruct
// Method to create and save a test category, then log result
```

---
---

## 📝 Step 3: Create Expense Service with Advanced AI Patterns (12 minutes)

### 💼 Expense Service Implementation - Complex Business Logic

Create comprehensive expense management using Copilot's advanced pattern recognition:

**🤖 Copilot Exercise 3A: Service Class Foundation**

1. **Create** `src/main/java/com/expensetracker/app/service/ExpenseService.java`

2. **Start with comprehensive service template**:

   ```java
   package com.expensetracker.app.service;
   
   import com.expensetracker.app.entity.Expense;
   import com.expensetracker.app.entity.Category;
   import com.expensetracker.app.repository.ExpenseRepository;
   import com.expensetracker.app.exception.EntityNotFoundException;
   import com.expensetracker.app.exception.ValidationException;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Service;
   import org.springframework.transaction.annotation.Transactional;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   
   import java.math.BigDecimal;
   import java.time.LocalDate;
   import java.util.List;
   
   /**
    * Service layer for Expense management with comprehensive business logic
    * Includes CRUD operations, validation, reporting, and analytics
    */
   @Service
   @Transactional  
   public class ExpenseService {
       
       private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);
       
       @Autowired
       private ExpenseRepository expenseRepository;
       
       @Autowired
       private CategoryService categoryService;
       
       // Generate comprehensive CRUD operations with validation
   ```

**🤖 Copilot Exercise 3B: CRUD Methods with Validation**

Add business methods using descriptive comments for Copilot:

```java
/**
 * Saves a new expense with comprehensive validation
 * @param expense the expense to save
 * @return saved expense
 * @throws ValidationException if validation fails
 */
// Method to save expense with amount validation and category checking

/**
 * Retrieves all expenses ordered by date (latest first)
 * @return list of all expenses
 */  
// Method to get all expenses ordered by date descending

// Method to get expense by ID with EntityNotFoundException if not found

// Method to update expense with full validation

// Method to delete expense with logging
```

**🤖 Copilot Exercise 3C: Query and Reporting Methods**

Continue with advanced query methods:

```java
// Method to get expenses by category ID with category validation

// Method to get expenses by date range with validation

// Method to get total expense amount using repository sum query

// Method to search expenses by keyword in description (case insensitive)

// Method to get expense statistics grouped by category
```

**🤖 Copilot Exercise 3D: Business Validation Methods**

Add private validation methods:

```java
/**
 * Validates expense amount is positive and not null
 * @param amount the amount to validate
 * @throws ValidationException if invalid
 */  
// Private method to validate expense amount is positive

// Private method to validate expense date is not in future

// Private method to validate category exists and is active
```

**🎯 Copilot Advanced Pattern Recognition**:

Copilot should demonstrate:

- **Service dependency injection** patterns
- **Complex validation logic** with multiple checks
- **Query method patterns** with proper parameter handling
- **Exception handling** with meaningful messages
- **Transaction management** with read-only annotations

---

## 📝 Step 4: Create Sample Data Seeder with AI-Generated Content (10 minutes)

### 🌱 Data Seeding Component - Realistic Test Data Generation

Create comprehensive sample data using Copilot's content generation capabilities:

**🤖 Copilot Exercise 4A: Data Seeder Structure**

1. **Create** `src/main/java/com/expensetracker/app/DataSeeder.java`

2. **Start with event listener template**:

   ```java
   package com.expensetracker.app;
   
   import com.expensetracker.app.entity.Category;
   import com.expensetracker.app.entity.Expense;
   import com.expensetracker.app.service.CategoryService;
   import com.expensetracker.app.service.ExpenseService;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.boot.context.event.ApplicationReadyEvent;
   import org.springframework.context.event.EventListener;
   import org.springframework.stereotype.Component;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   
   import java.math.BigDecimal;
   import java.time.LocalDate;
   
   /**
    * Component to seed database with sample data for development and testing
    * Runs automatically when application starts and database is empty
    */
   @Component
   public class DataSeeder {
       
       private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
       
       @Autowired
       private CategoryService categoryService;
       
       @Autowired  
       private ExpenseService expenseService;
       
       @EventListener(ApplicationReadyEvent.class)
       // Method to seed data on application startup if database is empty
   ```

**🤖 Copilot Exercise 4B: Category Generation**

Add category creation with Copilot-generated realistic data:

```java
/**
 * Creates sample categories with icons and colors if none exist
 */
// Private method to create sample categories with FontAwesome icons and hex colors

/**
 * Creates realistic sample expenses across different categories and date ranges  
 */
// Private method to create 15-20 sample expenses with realistic amounts and descriptions
```

**🤖 Copilot Exercise 4C: Realistic Content Generation**

Let Copilot generate varied, realistic content:

```java
// Array of realistic expense descriptions for different categories:
// Food: "Grocery shopping at Whole Foods", "Lunch at Italian restaurant", etc.
// Transport: "Gas station fill-up", "Uber ride downtown", "Bus monthly pass", etc.
// Entertainment: "Movie tickets", "Concert at Madison Square", "Netflix subscription", etc.
// Shopping: "New running shoes", "Online book purchase", "Clothing at Target", etc.
// Bills: "Electric bill", "Internet service", "Mobile phone plan", etc.
// Health: "Pharmacy prescription", "Dentist appointment", "Gym membership", etc.
```

**🎯 Advanced Copilot Techniques**:

Copilot should generate:

- **Realistic expense descriptions** with variety and context
- **Appropriate amount ranges** for different categories
- **Recent date distributions** across the last 30 days
- **Color codes and icons** matching category types
- **Duplicate checking logic** to prevent re-seeding

---

## 📝 Step 5: Create Unit Tests with AI-Generated Test Cases (8 minutes)

### 🧪 Unit Testing with Copilot

Generate comprehensive tests using Copilot's testing patterns:

**🤖 Copilot Exercise 5A: CategoryService Tests**

1. **Create** `src/test/java/com/expensetracker/app/service/CategoryServiceTest.java`

2. **Start with test class structure**:

   ```java
   package com.expensetracker.app.service;
   
   import com.expensetracker.app.entity.Category;
   import com.expensetracker.app.repository.CategoryRepository;
   import com.expensetracker.app.exception.EntityNotFoundException;
   import com.expensetracker.app.exception.ValidationException;
   import org.junit.jupiter.api.Test;
   import org.junit.jupiter.api.BeforeEach;
   import org.mockito.InjectMocks;
   import org.mockito.Mock;
   import org.mockito.MockitoAnnotations;
   import org.springframework.boot.test.context.SpringBootTest;
   
   import java.util.Optional;
   
   import static org.junit.jupiter.api.Assertions.*;
   import static org.mockito.Mockito.*;
   
   /**
    * Unit tests for CategoryService with comprehensive test coverage
    */  
   @SpringBootTest
   class CategoryServiceTest {
       
       @Mock
       private CategoryRepository categoryRepository;
       
       @InjectMocks
       private CategoryService categoryService;
       
       // Generate test setup method and comprehensive test methods
   ```

**🤖 Copilot Exercise 5B: Test Method Generation**

Let Copilot generate comprehensive test methods:

```java
// Test method for successful category save
// Test method for duplicate category name validation
// Test method for category not found exception
// Test method for successful category update
// Test method for category deletion with expense check
```

**🎯 Expected Test Coverage**: Copilot should generate tests for:

- **Happy path scenarios** with valid data
- **Exception scenarios** with appropriate assertions
- **Edge cases** like null values and empty strings
- **Mock verification** for repository interactions

---

## 🎉 Session Summary & Validation (5 minutes)

### ✅ What You've Accomplished with GitHub Copilot

**✅ Exception Handling**: Custom exceptions with AI-generated patterns  
**✅ Service Layer**: Comprehensive business logic using Copilot completions  
**✅ Data Validation**: AI-assisted validation rules and error handling  
**✅ Sample Data**: Realistic test data generated by Copilot  
**✅ Unit Tests**: AI-generated test cases with proper assertions  
**✅ Copilot Skills**: Intermediate pattern recognition and code completion

### 🤖 Copilot Techniques Mastered

- **Pattern-based code generation** for service layers
- **Context-aware completions** using existing code structure  
- **AI-generated realistic content** for test data
- **Test case automation** with proper mocking
- **Validation logic patterns** for business rules

### 🚀 Quick Validation Test

Run your enhanced application:

```bash
mvn clean compile
mvn spring-boot:run
```

**Expected outcomes**:

- Application starts without errors
- Sample data appears in H2 database
- Services are properly wired and functional
- Unit tests pass with `mvn test`

### 📚 Next Session Preview

**Day 2 - Session 1** will focus on **REST APIs and Controllers** with advanced Copilot chat interface techniques including:

- Agent usage (`@workspace`, `@terminal`)
- Interactive API development
- Advanced model selection for different tasks

> 🎯 **Copilot Progress**: You've completed **Intermediate Techniques** - ready for **Day 2: Chat Interface & Agent Usage**!

**Expected file**: `src/main/java/com/expensetracker/app/DataSeeder.java`

---

## 📝 Step 5: Create Essential Tests (5 minutes)

### 🧪 Basic Service Tests

Create tests for critical functionality:

**Copilot Prompt:**

```text
/generate Create CategoryServiceTest in src/test/java/com/expensetracker/app/service with:

@SpringBootTest and @Transactional test class with:
- testSaveCategory() - verify category creation works
- testSaveDuplicateCategory() - verify duplicate name validation
- testGetCategoryById() - verify retrieval works  
- testDeleteCategoryWithExpenses() - verify business rule enforcement

Use @Autowired for CategoryService and basic assertions
Keep tests simple but verify key business logic
Include proper test data setup and cleanup
```

**Copilot Prompt:**

```text
/generate Create ExpenseServiceTest in src/test/java/com/expensetracker/app/service with:

@SpringBootTest and @Transactional test class with:
- testSaveExpense() - verify expense creation with category
- testSaveExpenseWithInvalidAmount() - verify amount validation
- testGetExpensesByCategory() - verify category filtering
- testTotalExpenseAmount() - verify calculation accuracy

Use @Autowired for ExpenseService and CategoryService
Include test data creation helpers
Keep focused on business logic validation
```

---

## 📝 Step 6: Testing & Verification (2 minutes)

### 🧪 Run Everything

Test your complete backend:

```bash
# Run tests first
mvn test

# Start application and check seeding
mvn spring-boot:run
```

### ✅ Verify Sample Data

1. Check H2 console: `http://localhost:8080/h2-console`
2. Run queries to verify data:

```sql
SELECT COUNT(*) FROM CATEGORY;
SELECT COUNT(*) FROM EXPENSE;
SELECT c.name, COUNT(e.id) as expense_count 
FROM CATEGORY c LEFT JOIN EXPENSE e ON c.id = e.category_id 
GROUP BY c.id, c.name;
```

### 🔍 Check Application Logs

Look for seeding confirmation messages in the console output.

---

## 🎉 Session 2 Deliverables

### ✅ What You've Accomplished

By the end of Session 2, you should have:

- **✅ 2 Custom Exception Classes** for professional error handling
- **✅ CategoryService** with full CRUD and business logic
- **✅ ExpenseService** with validation and reporting methods
- **✅ DataSeeder Component** with realistic sample data
- **✅ 6+ Unit Tests** covering critical business logic
- **✅ Working Backend** with 6 categories and 15+ expenses

### 🔍 Quality Checklist

- [ ] All tests pass successfully
- [ ] Application starts and seeds data automatically
- [ ] Services handle validation and exceptions properly
- [ ] H2 console shows populated tables with sample data
- [ ] Logs confirm seeding operations completed
- [ ] No compilation errors or warnings

---

## 🎯 What's Next?

**Coming in Day 2 Session 1**: REST API Controllers

- Create CategoryRestController and ExpenseRestController
- Build JSON APIs with proper HTTP status codes
- Add request/response validation
- Test APIs with Postman or curl

---

## 💡 GitHub Copilot Tips for This Session

### 🎯 Effective Prompts Used

```text
/generate Create [service class] with [specific business methods]
/test Generate tests for [specific functionality]
/fix Fix the [specific error or issue]
```

### 🔧 Best Practices Learned

- **Service Layer**: Focus on business logic, not data access
- **Exception Handling**: Use custom exceptions for domain-specific errors  
- **Validation**: Validate at service layer, not just entity level
- **Testing**: Test business rules, not just basic CRUD operations

### 🚀 Productivity Boosters

- Use Copilot to generate realistic test data
- Ask for business validation suggestions
- Let Copilot suggest exception handling patterns
- Use Copilot for comprehensive service method documentation

---

## ❓ Troubleshooting & Q&A Time

**Common Student Questions:**

1. **Q**: "When should I use @Transactional?"
   **A**: Ask Copilot: `/explain @Transactional annotation usage and best practices`

2. **Q**: "Why create custom exceptions instead of using standard ones?"  
   **A**: Custom exceptions provide domain-specific error information and better debugging

3. **Q**: "Should business logic be in controllers or services?"
   **A**: Always in services! Controllers should be thin and focus on HTTP concerns

**Additional Help**: Remember, the service layer is the heart of your application. Take time to understand the business logic patterns we're implementing.

**🎯 Great Progress!** You now have a complete, tested backend. Tomorrow we'll expose this through REST APIs and build a beautiful web interface!

---

## 🎊 Celebration Checkpoint

**What You've Built So Far:**

- ✅ **Complete Data Layer**: Entities, repositories, relationships
- ✅ **Business Logic Layer**: Services with validation and error handling  
- ✅ **Sample Data**: 6 categories, 15+ realistic expenses
- ✅ **Quality Assurance**: Unit tests covering critical paths
- ✅ **Professional Structure**: Proper exception handling and logging

**This is real, production-quality backend code!** 🚀
