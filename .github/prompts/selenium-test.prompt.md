# Selenium UI Test Creation Template

## Usage Pattern: `/selenium Create UI test for [user workflow] using Page Object Model`

### Template Structure

```text
/generate Create comprehensive UI test for [USER_WORKFLOW] using Page Object Model:

Test Setup:
- TestNG test class extending [BASE_TEST_CLASS]
- @Test methods with appropriate groups (smoke, regression, e2e)
- Test data preparation and cleanup strategies
- Browser configuration (headless mode, window size, timeouts)

Page Object Model:
- [PAGE_OBJECT_CLASSES] required for the workflow
- Element locators using @FindBy annotations or By locators
- Page methods for user actions (click, type, select, navigate)
- Verification methods for assertions and validations

User Workflow Steps:
- [STEP_1]: [ACTION_DESCRIPTION] with [VERIFICATION]
- [STEP_2]: [ACTION_DESCRIPTION] with [VERIFICATION]
- [STEP_N]: [ACTION_DESCRIPTION] with [VERIFICATION]

Test Scenarios:
- [POSITIVE_SCENARIOS]: Happy path user journeys
- [NEGATIVE_SCENARIOS]: Error conditions and edge cases
- [BOUNDARY_CONDITIONS]: Input validation limits and constraints

Assertions and Verifications:
- [UI_ELEMENT_ASSERTIONS] (visibility, text content, state)
- [DATA_ASSERTIONS] (form submissions, database updates)
- [NAVIGATION_ASSERTIONS] (URL changes, page transitions)

Include proper wait strategies (explicit waits, custom conditions)
Add screenshot capture for test failures and key steps
Implement retry logic for flaky test scenarios
Ensure tests can run in parallel execution mode
```

### Examples

- Complete user registration and login workflows
- E-commerce shopping cart and checkout processes
- Form submission with multi-step validation
- Data table operations (search, filter, sort, paginate)
- File upload/download user journeys

### Sample Usage

```text
/selenium Create UI test for expense creation workflow using Page Object Model:
- Navigate from dashboard to add expense form
- Fill form with category selection and amount validation
- Verify expense appears in expenses list after creation
```