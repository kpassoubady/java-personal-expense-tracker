# 🤖 GitHub Copilot Mastery Guide - Personal Expense Tracker

## 🎯 **Overview**

This guide provides advanced GitHub Copilot techniques specifically designed for the Personal Expense Tracker project. Use these techniques throughout your development sessions to maximize productivity and learning.

**⏱️ Reference Time: Use as needed during exercises**

---

## 🚀 **Available AI Models & When to Use Them**

### 🧠 **Model Selection Guide**

Your organization may provide different models. Here's a comprehensive guide with examples:

| Model | Capability | Description | Best Use Cases |
|-------|------------|-------------|----------------|
| **GPT-4.1** | 0x | Incremental update to OpenAI's powerful GPT-4 model | Complex reasoning, architecture decisions |
| **GPT-4o** | 0x | OpenAI's "omni" model, optimized for fast, multimodal interactions | Quick responses, multimodal tasks |
| **GPT-5 mini** | 0x | Smaller, more efficient version of the unreleased GPT-5 | Fast code completion, simple queries |
| **Grok Code Fast 1** | 0x | xAI's specialized coding model, designed for speed | Rapid code generation, debugging |
| **Claude Sonnet 3.5** | 1x | Anthropic's fast and cost-effective model, excelling at enterprise tasks | Code review, documentation, refactoring |
| **Claude Sonnet 3.7** | 1x | Future iteration of Anthropic's Sonnet model series | Advanced analysis, complex problem solving |
| **Claude Sonnet 4** | 1x | Next-generation model in the Sonnet series | Sophisticated reasoning, code architecture |
| **Gemini 2.5 Pro** | 1x | Future version of Google's advanced Gemini Pro model | Multi-language support, complex analysis |
| **GPT-5** | 1x | Next-generation flagship large language model from OpenAI | Most complex tasks, advanced reasoning |
| **GPT-5-Codex** | 1x | GPT-5 specialized for code generation | Advanced code generation, complex algorithms |
| **o3-mini** | 0.33x | Smaller, efficient model from emerging developers | Basic tasks, quick responses |
| **o4-mini (Preview)** | 0.33x | Preview version of next-gen small model | Testing new features, lightweight tasks |

### 📝 **How to Switch Models**

```bash
# In Copilot Chat
/model gpt-4o          # Fast multimodal interactions
/model claude-3.5-sonnet   # Code review and enterprise tasks
/model gpt-5-codex     # Advanced code generation
/model grok-code-fast-1    # Rapid development

# Choose what's available in your organization
```

**💡 Pro Tip**: Start with Claude Sonnet 3.5 for code review and GPT-4o for quick development tasks. Use more powerful models (GPT-5, Claude Sonnet 4) for complex architecture decisions.

---

## 💬 **Advanced Chat Interface Techniques**

### 🎯 **Context Selection Commands**

Use these to provide precise context to Copilot:

#### **File & Code Selection**
```
# Reference specific code
#selection - Use currently selected text/code
#editor - Reference the entire active file
#file:src/main/java/com/expensetracker/app/entity/Category.java - Reference specific file

Example:
"Analyze this #selection and suggest improvements for JPA entity design"
```

#### **Terminal Integration**
```
#terminal_selection - Use selected terminal output
#terminal_last_command - Reference the last command run
#terminal - Reference entire terminal context

Example:
"Fix this compilation error: #terminal_selection"
"Explain what happened with: #terminal_last_command"
```

#### **Workspace Context**
```
#workspace - Reference entire project context
#git - Reference git history and changes
#problems - Reference current IDE problems/errors

Example:
"How can I improve the overall architecture of #workspace?"
```

### 🤖 **Agent Specialization**

Switch between specialized agents for different tasks:

```
@workspace - Project structure, architecture questions
@terminal - Command-line operations, build issues  
@git - Version control, branching, merging
@vscode - Editor configuration, extensions

Example:
"@workspace How should I organize my service layer packages?"
"@terminal Why did my Maven build fail?"
"@git Help me create a feature branch for expense categories"
```

---

## 📝 **Prompt Engineering for Java Spring Boot**

### 🏗️ **Entity Generation Prompts**

```
/generate Create a JPA entity for [EntityName] with:
- [specific fields and types]
- [validation requirements]  
- [relationship specifications]
- [additional constraints]

Example:
/generate Create a JPA Expense entity with:
- BigDecimal amount (required, positive, 2 decimal places)
- LocalDate expenseDate (required, not future)
- ManyToOne Category relationship (cascade persist)
- Audit fields (createdAt, updatedAt)
```

### 🔧 **Service Layer Prompts**

```
/generate Create [ServiceName] with:
- CRUD operations for [Entity]
- Business validation: [specific rules]
- Exception handling: [specific exceptions]
- Transaction boundaries: [read-only/write specifications]

Example:
/generate Create ExpenseService with:
- CRUD operations for Expense entity
- Business validation: amount > 0, date not in future
- Exception handling: EntityNotFoundException, ValidationException
- Transaction boundaries: read-only for queries, transactional for modifications
```

### 🌐 **Controller Layer Prompts**

```
/generate Create REST controller for [Entity] with:
- Standard CRUD endpoints
- HTTP status codes: [specific codes]
- Request/Response format: [JSON specifications]
- Error handling: [specific error responses]

Example:
/generate Create REST controller for Category with:
- GET /api/categories (return all with expense counts)
- POST /api/categories (create with validation)
- HTTP status codes: 200, 201, 400, 404, 409
- JSON response format: {"success": boolean, "data": object, "message": string}
```

---

## 📋 **Project-Specific Copilot Instructions**

### 🎨 **Create .copilot-instructions.md**

Create this file in your project root for consistent behavior:

```markdown
# Personal Expense Tracker - Copilot Instructions

## Project Context
This is a Spring Boot 3.2+ application for personal expense tracking with:
- JPA entities: Category, Expense
- H2 database for development
- Thymeleaf templates with Bootstrap
- Comprehensive testing strategy

## Code Standards
- Use Java 21 features when appropriate
- Follow Spring Boot best practices
- Implement proper validation and error handling
- Write meaningful test cases
- Use descriptive variable and method names

## Architectural Patterns
- Service layer for business logic
- Repository layer for data access
- Controller layer for web/REST endpoints
- DTO pattern for API responses
- Page Object Model for UI tests

## Preferred Libraries
- Spring Boot Starters (Web, Data JPA, Test, Validation)
- JUnit 5 for unit testing
- TestNG for integration testing
- Selenium WebDriver for UI testing
- H2 for development database

## Code Generation Preferences
- Include comprehensive JavaDoc comments
- Add validation annotations on entities
- Use @Transactional appropriately
- Include proper exception handling
- Generate both positive and negative test cases
```

### 📁 **Prompt Files for Common Tasks**

Create `prompts/` directory with reusable prompts:

#### `prompts/entity-generation.md`
```
Generate a JPA entity with the following template:
- Package: com.expensetracker.app.entity
- Proper JPA annotations (@Entity, @Table, @Id, @GeneratedValue)
- Validation annotations (@NotNull, @NotBlank, etc.)
- Audit fields (createdAt, updatedAt) with @PrePersist/@PreUpdate
- Standard constructors, getters, setters
- equals() and hashCode() based on id
- toString() method for debugging
```

#### `prompts/test-generation.md`
```
Generate comprehensive tests with:
- @SpringBootTest for integration tests
- @WebMvcTest for controller tests
- @DataJpaTest for repository tests
- Test data builders for consistent objects
- Both positive and negative test scenarios
- Proper assertions and verification
- Mock setup where appropriate
```

---

## 🛠️ **Practical Examples for Each Session**

### **Day 1: Backend Development**

#### **Entity Creation with Context**
```
# Select existing entity structure in editor, then:
"Using #selection as a template, create a similar Expense entity with amount, date, description, and category relationship"
```

#### **Service Logic with Terminal Context**
```
# After seeing compilation errors in terminal:
"Fix these compilation errors in my service class: #terminal_selection"
```

### **Day 2: Web Development**

#### **Controller Generation**
```
# With service layer selected:
"Create a REST controller that uses #selection to provide CRUD endpoints with proper error handling"
```

#### **Template Creation**
```
# With existing template open:
"Create a similar Thymeleaf template for expense management using #editor as reference"
```

### **Day 3: Testing**

#### **Test Generation with Context**
```
# With service method selected:
"Generate comprehensive tests for #selection including edge cases and error scenarios"
```

#### **UI Test Creation**
```
# With web page open in browser:
"Create Selenium Page Object for the current expense form, testing all input fields and validation"
```

---

## 🎯 **Interactive Exercise: Copilot Command Practice**

### **Exercise: Create a Custom Category Validation**

**Time: 5 minutes**

1. **Open** `CategoryService.java`
2. **Select** the `saveCategory` method
3. **Use Chat**: "Add business rule validation to #selection that prevents duplicate category names ignoring case"
4. **Try different agents**:
   - `@workspace What's the best pattern for validation in this project?`
   - `@terminal How do I run tests for this specific class?`
5. **Reference terminal output**: Run tests, then ask "Explain these test results: #terminal_last_command"

**Expected Outcome**: Enhanced validation with proper error handling and understanding of different Copilot interfaces.

---

## 📚 **Best Practices Summary**

### ✅ **Do This**
- Use specific context selectors (`#selection`, `#file:path`)
- Provide clear, structured prompts with requirements
- Switch agents based on task type (`@workspace`, `@terminal`)
- Create project-specific instructions file
- Use terminal context for debugging

### ❌ **Avoid This**
- Vague prompts without context
- Accepting first suggestion without review
- Ignoring error messages or warnings
- Not using appropriate agents for tasks
- Over-relying on code generation without understanding

---

## 🚀 **Quick Reference Card**

```
CONTEXT SELECTORS:
#selection - Selected code
#editor - Active file
#file:path - Specific file
#terminal_selection - Selected terminal output
#terminal_last_command - Last command
#workspace - Project context

AGENTS:
@workspace - Architecture & structure
@terminal - Commands & builds
@git - Version control
@vscode - Editor settings

COMMON PATTERNS:
/generate - Create new code
/explain - Understand existing code
/fix - Resolve issues
/refactor - Improve code structure
/test - Generate tests
```

---

**💡 Remember**: GitHub Copilot is most effective when you provide clear context and specific requirements. Practice these techniques throughout your development sessions!