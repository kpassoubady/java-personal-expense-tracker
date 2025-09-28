# 🏗️ Day 1 - Session 2: Personal Expense Tracker - Services & Business Logic (30 mins)

## 🎯 Learning Objectives

By the end of this session, you will:

- Create service layer with comprehensive business logic
- Implement custom exceptions for proper error handling
- Add validation and data integrity checks
- Create unit tests for critical service methods
- Seed database with sample data for testing

**⏱️ Time Allocation: 30 minutes (with Q&A buffer)**

---

## 📋 Prerequisites Check (2 minutes)

- ✅ Session 1 completed successfully
- ✅ Entities and repositories working
- ✅ Application starts without errors
- ✅ H2 database accessible

**Quick Test**: Verify your foundation is solid:

```bash
# Ensure app still starts
mvn spring-boot:run

# Check in another terminal
curl http://localhost:8080/h2-console
```

---

## 🚀 Session Overview

In this session, we'll add the business logic layer that makes your expense tracker intelligent. We'll focus on essential services, error handling, and testing to ensure everything works reliably.

### 🎯 What You'll Build (30 minutes)

- **Custom Exceptions**: Professional error handling
- **Category Service**: CRUD operations with validation
- **Expense Service**: Business logic with category integration
- **Unit Tests**: Key service method testing
- **Data Seeding**: Sample data for development

---

## 📝 Step 1: Create Custom Exceptions (5 minutes)

### 🚨 Exception Classes

Create proper error handling foundation:

**Copilot Prompt:**

```text
/generate Create custom exception classes in com.expensetracker.app.exception package:

1. EntityNotFoundException extends RuntimeException:
   - Constructor with message
   - Constructor with message and cause
   - Static methods like notFound(String entityName, Long id)

2. ValidationException extends RuntimeException:
   - Constructor with message
   - Constructor with message and cause
   - Static method invalidData(String field, String reason)

Include proper documentation and examples
```

**Expected files:**

- `src/main/java/com/expensetracker/app/exception/EntityNotFoundException.java`
- `src/main/java/com/expensetracker/app/exception/ValidationException.java`

---

## 📝 Step 2: Create Category Service (10 minutes)

### 🏢 Category Service Implementation

Build the category business logic:

**Copilot Prompt:**

```text
/generate Create CategoryService in com.expensetracker.app.service package with:

Core CRUD operations:
- saveCategory(Category category) with duplicate name validation
- getAllCategories() returning ordered list
- getCategoryById(Long id) with EntityNotFoundException if not found
- updateCategory(Long id, Category updatedCategory) with validation
- deleteCategory(Long id) with expense relationship checking

Additional business methods:
- getCategoryCount() returning total count
- getCategoryStatistics() returning Map<String, Object> with usage stats
- findCategoriesByName(String searchName) for partial matching

Include:
- @Service annotation
- @Transactional annotations (read-only where appropriate)  
- Proper exception handling using custom exceptions
- Validation for duplicate names and business rules
- Logger for debugging
```

**Expected file**: `src/main/java/com/expensetracker/app/service/CategoryService.java`

### ✅ **Quick Test**

After creating CategoryService, test it works:

**Copilot Prompt:**

```text
/generate Create a simple test method in CategoryService that creates a test category and saves it, then logs the result. Add this as a @PostConstruct method for now.
```

---

## 📝 Step 3: Create Expense Service (10 minutes)

### 💼 Expense Service Implementation

Create the main business logic service:

**Copilot Prompt:**

```text
/generate Create ExpenseService in com.expensetracker.app.service package with:

Core CRUD operations:
- saveExpense(Expense expense) with category validation and amount checks
- getAllExpenses() returning latest expenses first
- getExpenseById(Long id) with proper exception handling
- updateExpense(Long id, Expense updatedExpense) with validation
- deleteExpense(Long id) with confirmation

Query and reporting methods:
- getExpensesByCategory(Long categoryId) filtering by category
- getExpensesByDateRange(LocalDate start, LocalDate end) date filtering
- getTotalExpenseAmount() returning BigDecimal sum
- getExpenseCount() returning total count
- searchExpenses(String keyword) searching description

Business validation:
- validateExpenseAmount(BigDecimal amount) ensuring positive amounts
- validateExpenseDate(LocalDate date) ensuring not future dates
- validateCategory(Category category) ensuring category exists

Include:
- @Service annotation and @Transactional management
- Integration with CategoryService for validation
- Custom exception handling
- Logger for business operations
- Proper null checking and edge cases
```

**Expected file**: `src/main/java/com/expensetracker/app/service/ExpenseService.java`

---

## 📝 Step 4: Create Sample Data Seeder (8 minutes)

### 🌱 Data Seeding Component

Create realistic test data:

**Copilot Prompt:**

```text
/generate Create DataSeeder component in com.expensetracker.app package with:

@Component class that runs on ApplicationReadyEvent:
- Create 5-6 sample categories (Food, Transport, Entertainment, Shopping, Bills, Health)
- Each category should have appropriate icon (FontAwesome icons) and color codes
- Generate 15-20 realistic expenses across different categories
- Use various amounts ($5-$500) and recent dates (last 30 days)
- Include realistic descriptions like "Grocery shopping", "Gas station", "Movie tickets"
- Check if data already exists before seeding (avoid duplicates on restart)
- Log all seeding activities for verification

Sample categories with details:
- Food: icon "fas fa-utensils", color "#FF6B6B"
- Transport: icon "fas fa-car", color "#4ECDC4"  
- Entertainment: icon "fas fa-film", color "#45B7D1"
- Shopping: icon "fas fa-shopping-bag", color "#96CEB4"
- Bills: icon "fas fa-file-invoice", color "#FECA57"
- Health: icon "fas fa-medkit", color "#FF9FF3"

Use the services you just created (CategoryService, ExpenseService) for data creation
```

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
