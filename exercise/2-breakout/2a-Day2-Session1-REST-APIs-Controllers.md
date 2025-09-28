# 🎨 Day 2 - Session 1: Personal Expense Tracker - REST APIs & Controllers (30 mins)

## 🎯 Learning Objectives

By the end of this session, you will:

- Create REST API controllers for Category and Expense operations
- Implement proper HTTP status codes and JSON responses
- Add request validation and error handling
- Test APIs with realistic data
- Understand REST API best practices

**⏱️ Time Allocation: 30 minutes (with Q&A buffer)**

---

## 📋 Prerequisites Check (2 minutes)

- ✅ Day 1 Sessions completed (entities, repositories, services)
- ✅ Application starts successfully with sample data
- ✅ Services work correctly (tested in previous session)
- ✅ H2 database contains categories and expenses

**Quick Test**: Verify your backend is ready:

```bash
# Start application
mvn spring-boot:run

# Check sample data exists
curl http://localhost:8080/h2-console
```

---

## 🚀 Session Overview

In this session, you'll expose your business logic through REST APIs. We'll create controllers that return JSON data, handle HTTP requests properly, and provide a foundation for frontend integration.

### 🎯 What You'll Build (30 minutes)

- **Category REST Controller**: Full CRUD API with JSON responses
- **Expense REST Controller**: Advanced querying and filtering APIs  
- **Error Handling**: Proper HTTP status codes and error messages
- **API Testing**: Verify all endpoints work correctly

---

## 📝 Step 1: Create Category REST Controller (12 minutes)

### 🏷️ Category API Implementation

Create comprehensive REST API for categories:

**Copilot Prompt:**

```text
/generate Create CategoryRestController in com.expensetracker.app.controller package with:

@RestController and @RequestMapping("/api/categories"):

Basic CRUD operations:
- GET /api/categories - return all categories with expense counts
- GET /api/categories/{id} - return single category or 404 if not found
- POST /api/categories - create new category with @Valid validation
- PUT /api/categories/{id} - update category with validation
- DELETE /api/categories/{id} - delete with safety check for existing expenses

Additional endpoints:
- GET /api/categories/search?name={name} - search categories by name
- GET /api/categories/stats - return category statistics

Response format:
- Success responses: {"success": true, "data": {...}, "message": "..."}
- Error responses: {"success": false, "error": "...", "message": "..."}

Include:
- @CrossOrigin for frontend integration
- Proper HTTP status codes (200, 201, 400, 404, 409)
- Exception handling with try-catch blocks
- Validation error handling
- Integration with CategoryService
```

**Expected file**: `src/main/java/com/expensetracker/app/controller/CategoryRestController.java`

### ✅ **Quick API Test**

Test your Category API:

```bash
# Get all categories
curl -X GET http://localhost:8080/api/categories

# Get specific category
curl -X GET http://localhost:8080/api/categories/1

# Create new category (test with JSON)
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Category","description":"API Test","icon":"fas fa-test","color":"#123456"}'
```

---

## 📝 Step 2: Create Expense REST Controller (15 minutes)

### 💰 Expense API Implementation

Create the main expense API with advanced features:

**Copilot Prompt:**

```text
/generate Create ExpenseRestController in com.expensetracker.app.controller package with:

@RestController and @RequestMapping("/api/expenses"):

Core CRUD operations:
- GET /api/expenses - return all expenses with pagination support (?page=0&size=10)
- GET /api/expenses/{id} - return single expense with category details
- POST /api/expenses - create expense with category validation
- PUT /api/expenses/{id} - update expense with business validation  
- DELETE /api/expenses/{id} - delete expense with confirmation

Advanced query endpoints:
- GET /api/expenses/category/{categoryId} - expenses by category
- GET /api/expenses/search?keyword={text}&startDate={date}&endDate={date} - advanced search
- GET /api/expenses/summary - expense totals and statistics
- GET /api/expenses/recent?limit={n} - recent expenses

Analytics endpoints:
- GET /api/expenses/analytics/category - spending by category
- GET /api/expenses/analytics/monthly - monthly spending trends

Include:
- Proper pagination with Page/Pageable from Spring Data
- Date range filtering with @DateTimeFormat
- Request/response DTOs for clean API contracts
- Comprehensive error handling and validation
- @CrossOrigin for AJAX support
- Proper HTTP status codes for all operations

Response should include:
- Total count for pagination
- Category names, not just IDs
- Formatted dates and amounts
```

**Expected file**: `src/main/java/com/expensetracker/app/controller/ExpenseRestController.java`

### 🔧 Create DTOs (If Needed)

If your controller needs DTOs, ask Copilot:

**Copilot Prompt:**

```text
/generate Create ExpenseDTO in com.expensetracker.app.dto package for API responses:
- Include all Expense fields plus categoryName
- Add convenience constructors 
- Include validation annotations for API requests
- Add methods to convert between Expense entity and DTO
```

---

## 📝 Step 3: Add Global Error Handling (3 minutes)

### 🚨 Exception Handler

Create centralized error handling:

**Copilot Prompt:**

```text
/generate Create GlobalExceptionHandler in com.expensetracker.app.controller package with:

@ControllerAdvice class handling:
- @ExceptionHandler(EntityNotFoundException.class) - return 404 with error message
- @ExceptionHandler(ValidationException.class) - return 400 with validation details
- @ExceptionHandler(MethodArgumentNotValidException.class) - handle @Valid failures
- @ExceptionHandler(Exception.class) - generic 500 error handler

Each handler should return ResponseEntity with consistent error format:
{"success": false, "error": "Error Type", "message": "Detailed message", "timestamp": "..."}

Include proper logging for debugging
```

**Expected file**: `src/main/java/com/expensetracker/app/controller/GlobalExceptionHandler.java`

---

## 📝 Step 4: API Testing & Verification (8 minutes)

### 🧪 Test Category APIs

Test all category endpoints:

```bash
# 1. Get all categories
curl -X GET http://localhost:8080/api/categories

# 2. Get category by ID
curl -X GET http://localhost:8080/api/categories/1

# 3. Search categories
curl -X GET "http://localhost:8080/api/categories/search?name=Food"

# 4. Get category statistics
curl -X GET http://localhost:8080/api/categories/stats

# 5. Create new category
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Travel","description":"Travel expenses","icon":"fas fa-plane","color":"#8E44AD"}'
```

### 🧪 Test Expense APIs

Test expense endpoints:

```bash
# 1. Get all expenses
curl -X GET http://localhost:8080/api/expenses

# 2. Get paginated expenses
curl -X GET "http://localhost:8080/api/expenses?page=0&size=5"

# 3. Get expenses by category
curl -X GET http://localhost:8080/api/expenses/category/1

# 4. Get recent expenses
curl -X GET "http://localhost:8080/api/expenses/recent?limit=5"

# 5. Get expense analytics
curl -X GET http://localhost:8080/api/expenses/analytics/category

# 6. Search expenses
curl -X GET "http://localhost:8080/api/expenses/search?keyword=grocery"

# 7. Create new expense
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"description":"API Test Expense","amount":25.99,"expenseDate":"2024-01-15","categoryId":1}'
```

### ✅ **Verify Error Handling**

Test error scenarios:

```bash
# Test 404 error
curl -X GET http://localhost:8080/api/categories/999

# Test validation error
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"","description":"Empty name test"}'
```

---

## 📝 Step 5: Quick Frontend Integration Setup (2 minutes)

### 🌐 Enable CORS and Static Resources

Update your main application class or create a config:

**Copilot Prompt:**

```text
/generate Create WebConfig in com.expensetracker.app.config package with:
- @Configuration and implements WebMvcConfigurer
- addCorsMappings() allowing all origins for /api/** endpoints
- addResourceHandlers() for static content serving
- Enable proper CORS for development (localhost:3000, localhost:8080)
```

---

## 🎉 Session 1 Deliverables

### ✅ What You've Accomplished

By the end of this session, you should have:

- **✅ CategoryRestController** with 6+ REST endpoints
- **✅ ExpenseRestController** with 8+ REST endpoints including analytics
- **✅ GlobalExceptionHandler** for consistent error handling
- **✅ CORS Configuration** ready for frontend integration
- **✅ Comprehensive API Testing** - all endpoints verified working
- **✅ Professional JSON Responses** with proper HTTP status codes

### 🔍 Quality Checklist

- [ ] All API endpoints respond correctly
- [ ] Error handling returns proper HTTP status codes
- [ ] JSON responses follow consistent format
- [ ] Pagination works for expense listings
- [ ] Search and filtering endpoints function correctly
- [ ] CORS allows frontend access
- [ ] Validation errors are handled gracefully

---

## 🎯 What's Next?

**Coming in Session 2**: Web Interface & Templates
- Create Thymeleaf templates for category and expense management
- Build responsive web forms with Bootstrap
- Add dashboard with charts and analytics
- Integrate AJAX for dynamic updates

---

## 💡 GitHub Copilot Tips for This Session

### 🎯 Effective Prompts Used

```text
/generate Create REST controller with [specific endpoints]
/api Generate API endpoint for [specific functionality]
/test Create test for [specific API endpoint]
```

### 🔧 Best Practices Learned

- **REST Design**: Use proper HTTP verbs and status codes
- **Error Handling**: Consistent error response format across all APIs
- **Validation**: Validate at controller level with @Valid
- **CORS**: Enable cross-origin requests for frontend development

### 🚀 API Testing Tips

- Use curl for quick testing during development
- Test both success and error scenarios
- Verify JSON response structure consistency
- Check HTTP status codes match expectations

---

## ❓ Troubleshooting & Q&A Time

**Common Student Questions:**

1. **Q**: "What's the difference between @Controller and @RestController?"
   **A**: Ask Copilot: `/explain difference between @Controller and @RestController annotations`

2. **Q**: "When should I return ResponseEntity vs just the object?"
   **A**: Always use ResponseEntity for REST APIs to control HTTP status codes

3. **Q**: "How do I handle file uploads in REST APIs?"
   **A**: We'll cover this in advanced sessions, but Copilot can show MultipartFile examples

**Additional Help**: API development is about contracts and consistency. Your APIs should be predictable and well-documented.

**🎯 Great Progress!** You now have a complete REST API backend that any frontend can consume. Ready for the web interface in the next session!

---

## 🎊 Mid-Course Celebration

**What You've Built So Far:**

- ✅ **Complete Backend**: Entities, repositories, services, REST APIs
- ✅ **Professional APIs**: 14+ REST endpoints with proper HTTP semantics
- ✅ **Error Handling**: Graceful error responses and validation
- ✅ **Sample Data**: Ready for frontend development
- ✅ **Production Ready**: CORS, pagination, search capabilities

**You're now ready to build any frontend - web, mobile, or desktop!** 🚀