# 🏗️ Day 1 - Session 1: Personal Expense Tracker - Entities & Repositories (45 mins)

## 🎯 Learning Objectives

By the end of this session, you will:

- Create Spring Boot project structure with JPA entities
- Implement repository layer with basic queries
- Set up H2 database configuration
- Master GitHub Copilot interface and context selection
- Verify entities and repositories work correctly

**⏱️ Time Allocation: 45 minutes (includes Copilot mastery)**

## 🤖 GitHub Copilot Focus: Basic Interface & Code Generation

**Today's Copilot Skills:**

- Interface basics (inline completions, chat, command palette)
- Context selection with `#selection`, `#editor`
- Model selection and basic prompting
- Code generation for entities and repositories

📚 **Reference**: [Copilot Mastery Guide](../0-copilot-mastery-guide.md) for advanced techniques

---

## 📋 Prerequisites Check (2 minutes)

- ✅ Java 21 installed
- ✅ Maven 3.6+ installed
- ✅ IDE setup (VS Code or IntelliJ)
- ✅ GitHub Copilot enabled and authenticated

**Quick Test**: Verify setup by running:

```bash
java --version
mvn --version
```

---

## 🚀 Session Overview

In this first session, you'll build the foundation data layer of the expense tracker. We'll focus on creating solid entities and repositories without rushing into complex business logic.

### 🎯 What You'll Build (30 minutes)

- **JPA Entities**: Category and Expense with relationships
- **Repository Layer**: Basic CRUD operations with a few custom queries
- **Database Config**: H2 setup for development
- **Basic Validation**: Essential entity validation

---

## 📝 Step 1: Project Setup & Configuration (8 minutes)

### 🔧 Check Current Project Structure

First, let's examine what we already have:

```bash
# Navigate to project directory
cd /path/to/Personal-Expense-Tracker

# Check current structure
ls -la src/main/java/com/expensetracker/app/
```

### 📄 Verify Application Properties

Check if `src/main/resources/application.properties` exists and contains:

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:expensedb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (for development)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.org.springframework.web=DEBUG
logging.level.com.expensetracker=DEBUG
```

**GitHub Copilot Prompt:**

```
**GitHub Copilot Prompt:**

```bash
# Try different approaches - experiment with your available models
# Examples: gpt-4-turbo, claude-3.5-sonnet, gpt-3.5-turbo
# Check what's available in your organization

/workspace Check application.properties configuration for H2 database and suggest any missing JPA settings for development
```

**🤖 Copilot Practice**: Try switching models and compare responses:

```bash
# In Copilot Chat, try:
/model gpt-4-turbo
"What H2 configuration is missing for development?"

/model claude-3.5-sonnet  
"Review this database setup and suggest improvements"
```

---

## 📝 Step 1.5: Create Specialized Development Assistants (8 minutes)

### 🤖 GitHub Copilot Chat-Modes Setup

Now that you understand model switching, let's create specialized AI assistants for different development contexts. These chat-modes will provide focused expertise throughout the course.

#### 🔧 Create SDLC Development Assistant

Create `.github/chatmodes/sdlc-modes.chatmode.md`:

**Copilot Prompt:**

```text
/generate Create GitHub Copilot chatmode file for Software Development Life Cycle (SDLC) assistance:

Filepath: .github/chatmodes/sdlc-modes.chatmode.md

Include YAML frontmatter with:
- description: 'SDLC specialized modes for different development phases'
- tools: []

Create modes for:
1. Development Mode 🔧 - balanced speed/accuracy, practical patterns, error handling
2. Code Review Mode 🔍 - security focus, best practices, performance analysis 
3. Debugging Mode 🐛 - systematic troubleshooting, logging, root cause analysis
4. Refactoring Mode ♻️ - code improvement, design patterns, maintainability
5. Documentation Mode 📚 - comprehensive docs, examples, API documentation

Each mode should specify:
- Usage pattern (/mode [name] or "specify mode in request")
- Specialized behavior and focus areas
- Example prompts for that mode
- When to use this mode

Make it practical for Spring Boot development
```

#### 🍃 Create Spring Boot Expert Assistant

Create `.github/chatmodes/spring-boot-expert.chatmode.md`:

**Copilot Prompt:**

```text
/generate Create specialized GitHub Copilot chatmode for Spring Boot development:

Filepath: .github/chatmodes/spring-boot-expert.chatmode.md

Include YAML frontmatter with:
- description: 'Spring Boot development specialist for this expense tracker project'
- tools: []

Specialized behavior for:
- Project Context: Spring Boot 3.2.3 + Java 21, Maven build, H2 database
- Package structure: com.expensetracker.app
- Thymeleaf templates, JPA/Hibernate, RESTful APIs

Expertise areas:
- Entity design with proper JPA annotations
- Repository layer with custom queries
- Service layer with transaction management
- REST controller best practices
- Exception handling patterns
- Validation and security considerations

Include:
- Coding standards and conventions
- Common Spring Boot patterns
- Performance optimization tips
- Testing recommendations
- Example prompts for typical tasks

Make responses project-specific and actionable
```

#### 🚀 Immediate Application

Now let's use our new Spring Boot expert for the next entity creation:

**Test Your Spring Boot Expert:**

```text
# In Copilot Chat, try:
@workspace /chatmode spring-boot-expert

"Help me design the Category entity with proper JPA annotations, validation, and relationships for an expense tracker."
```

**🎯 Learning Tip**: Notice how the specialized assistant provides more contextual, project-specific guidance compared to general prompts!

---

## 📝 Step 2: Create Category Entity (10 minutes)

### 🏷️ Category Entity Implementation

Use GitHub Copilot to create the Category entity:

**Copilot Prompt:**

```text
/generate Create a JPA Category entity in com.expensetracker.app.entity package with:
- Long id (auto-generated primary key)
- String name (required, unique, max 100 characters)
- String description (optional, max 255 characters)
- String icon (max 50 characters for UI icons like "fas fa-utensils")
- String color (7 characters for hex color codes like "#FF5733")
- LocalDateTime createdAt, updatedAt (automatic audit fields)
- One-to-many relationship with Expense entity
- Include proper JPA annotations, validation constraints, constructors, getters, setters
- Add toString() method for debugging
```

**🤖 Advanced Copilot Practice**: After creating the entity, try these:

1. **Context Selection**: Select the entire entity class, then ask:

```text
"Analyze #selection and suggest improvements for JPA best practices"
```

2. **Model Comparison**: Try the same prompt with different models:

```text
/model gpt-4-turbo
"Review #selection for performance optimizations"

/model claude-3.5-sonnet
"Check #selection for validation completeness"
```

3. **Agent Usage**: Get architectural advice:

```text
@workspace "How should this Category entity fit into the overall project structure?"
```

```

**Expected file location**: `src/main/java/com/expensetracker/app/entity/Category.java`

### ✅ Verification Point

- Ensure the entity compiles without errors
- Check that JPA annotations are correct
- Verify validation annotations are in place

---

## 📝 Step 3: Create Expense Entity (10 minutes)

### 💰 Expense Entity Implementation

Create the main Expense entity:

**Copilot Prompt:**

```text
/generate Create a JPA Expense entity in com.expensetracker.app.entity package with:
- Long id (auto-generated primary key)
- BigDecimal amount (required, precision 12, scale 2, positive values only)
- String description (required, max 255 characters)
- LocalDate expenseDate (required, defaults to today)
- Many-to-one relationship with Category entity
- LocalDateTime createdAt, updatedAt (automatic audit fields)
- Include proper JPA annotations (@Entity, @Table, @Column)
- Add validation annotations (@NotNull, @NotBlank, @DecimalMin, @Digits)
- Include constructors (default and with parameters), getters, setters
- Add toString() method and equals/hashCode based on id
```

**Expected file location**: `src/main/java/com/expensetracker/app/entity/Expense.java`

### 🔧 Add Helpful Methods

Ask Copilot to add convenience methods:

**Copilot Prompt:**

```text
/generate Add to Expense entity:
- A convenience constructor Expense(String description, BigDecimal amount, LocalDate expenseDate, Category category)
- A method getCategoryId() that returns category?.getId()
- A method setCategoryId(Long categoryId) for form binding
- Proper @PrePersist and @PreUpdate methods for audit fields
```

---

## 📝 Step 4: Create Repository Layer (8 minutes)

### 🗄️ Category Repository

Create repository with essential queries:

**Copilot Prompt:**

```text
/generate Create CategoryRepository interface extending JpaRepository<Category, Long> with:
- findByNameIgnoreCase(String name) - case-insensitive name search
- findAllByOrderByName() - alphabetically sorted categories
- existsByNameIgnoreCase(String name) - check for duplicate names
- @Query to count expenses per category: "SELECT c, COUNT(e) FROM Category c LEFT JOIN c.expenses e GROUP BY c"
- Place in com.expensetracker.app.repository package
- Include proper @Repository annotation and imports
```

**Expected file location**: `src/main/java/com/expensetracker/app/repository/CategoryRepository.java`

### 📊 Expense Repository

Create expense repository with basic queries:

**Copilot Prompt:**

```text
/generate Create ExpenseRepository interface extending JpaRepository<Expense, Long> with:
- findByCategory(Category category) - expenses by category
- findByExpenseDateBetween(LocalDate start, LocalDate end) - date range filter
- findByAmountGreaterThan(BigDecimal amount) - expenses above threshold
- findByDescriptionContainingIgnoreCase(String keyword) - search by description
- @Query "SELECT e FROM Expense e ORDER BY e.expenseDate DESC, e.createdAt DESC" named findAllOrderByDateDesc()
- @Query to sum expenses by category: "SELECT c.name, COALESCE(SUM(e.amount), 0) FROM Category c LEFT JOIN c.expenses e GROUP BY c.id, c.name"
- Place in com.expensetracker.app.repository package
```

**Expected file location**: `src/main/java/com/expensetracker/app/repository/ExpenseRepository.java`

---

## 📝 Step 5: Testing & Verification (5 minutes)

### 🧪 Quick Compilation Test

Test that everything compiles:

```bash
# Clean and compile
mvn clean compile

# If there are errors, ask Copilot to help fix them
```

**🤖 Terminal Integration Practice**: If you see compilation errors:

1. **Select the error text in terminal**, then use:

```text
"Fix this compilation error: #terminal_selection"
```

2. **Reference the last command**:

```text
"Explain what happened with: #terminal_last_command"
```

3. **Get terminal help**:

```text
@terminal "Why did my Maven compilation fail?"
```

**💡 Pro Tip**: Always copy error messages to Copilot - it's excellent at interpreting build errors and suggesting fixes!

### 🚀 Start the Application

```bash
# Start Spring Boot application
mvn spring-boot:run
```

### ✅ Verify H2 Database Setup

1. Open browser to: `http://localhost:8080/h2-console`
2. Use connection settings:
   - JDBC URL: `jdbc:h2:mem:expensedb`
   - Username: `sa`
   - Password: `password` (check your application.properties file)

3. Check that tables were created:

```sql
SHOW TABLES;
SHOW COLUMNS FROM CATEGORIES;
SHOW COLUMNS FROM EXPENSES;
```

---

## 🎉 Session 1 Deliverables

### ✅ What You've Accomplished

By the end of Session 1, you should have:

- **✅ 2 JPA Entities** (Category, Expense) with proper relationships
- **✅ 2 Repository Classes** with 8+ query methods
- **✅ Working H2 Database** with auto-generated tables
- **✅ Proper JPA Configuration** with audit fields
- **✅ Compilation Success** - no build errors

### 🔍 Quality Checklist

- [ ] Application starts without errors
- [ ] H2 console accessible and shows tables
- [ ] Entities have proper JPA annotations
- [ ] Repositories extend JpaRepository correctly
- [ ] Validation constraints are in place
- [ ] Relationships between entities work

---

## 🎯 What's Next?

**Coming in Session 2**: Service Layer & Business Logic

- Create CategoryService and ExpenseService
- Implement validation and business rules  
- Add exception handling
- Create sample data and testing

---

## 🎯 **Bonus: Create Project Copilot Instructions** (5 minutes)

### 📝 **Interactive Exercise: Set Up Custom Instructions**

Let's create a `.copilot-instructions.md` file to ensure consistent Copilot behavior throughout the project:

1. **Use Copilot to create and populate it**:

```text
/generate Create a .copilot-instructions.md file for a Spring Boot expense tracking application with:
- Project context: Personal expense tracker with JPA entities
- Code standards: Java 21, Spring Boot 3.2+, proper validation
- Architecture patterns: Service/Repository/Controller layers
- Testing approach: JUnit 5, comprehensive test coverage
- Preferred libraries: Spring Boot starters, H2 database
```

2. **Verify it works** by asking:

```text
"Generate a JPA entity following the project standards"
```

**Expected Result**: Copilot should now provide more consistent suggestions that match your project's patterns!

---

## 💡 GitHub Copilot Tips for This Session

### 🎯 Advanced Techniques Learned

- **Context Selectors**: `#selection`, `#editor`, `#terminal_selection`
- **Agent Usage**: `@workspace`, `@terminal` for specialized help
- **Model Switching**: Compare responses from different available models
- **Terminal Integration**: Use error messages and command output effectively
- **Custom Instructions**: Project-specific guidance for consistent code generation

### 🔧 Progressive Mastery Path

**Today (Session 1)**: Basic interface, context selection, model switching
**Tomorrow (Session 3)**: Chat interface, complex prompts, agent specialization  
**Day 3 (Session 5)**: Advanced prompt engineering, custom workflows

### 🚀 Quick Reference Card

```text
CONTEXT SELECTORS:
#selection - Selected code/text
#editor - Current file contents  
#terminal_selection - Selected terminal output
#terminal_last_command - Last terminal command

AGENTS FOR DAY 1:
@workspace - Project structure questions
@terminal - Build and compilation help

MODELS TO TRY:
/model gpt-4-turbo - Complex reasoning
/model claude-3.5-sonnet - Code review
/model gpt-3.5-turbo - Quick completions
```

### 🤖 **What's Next in Copilot Learning**

**Session 2**: Service layer generation with complex business logic prompts
**Day 2**: Web development with template generation and AJAX integration  
**Day 3**: Advanced testing strategies and custom prompt files

---

## ❓ Troubleshooting & Q&A Time

**Common Student Questions:**

1. **Q**: "Why use BigDecimal for money amounts?"
   **A**: Ask Copilot: `/explain why BigDecimal is better than double for financial calculations`

2. **Q**: "What's the difference between @OneToMany and @ManyToOne?"
   **A**: Ask Copilot: `/explain JPA relationship annotations with examples`

3. **Q**: "Why are we using H2 database?"
   **A**: H2 is perfect for learning - no setup required, visible console, automatic schema generation

**Additional Help**: If you're stuck, ask your neighbor or raise your hand! Remember, we're building this progressively, so don't worry if something isn't 100% clear yet.

**🎯 Ready for Session 2?** Take a 5-minute break, then we'll add the service layer to make this backend fully functional!
