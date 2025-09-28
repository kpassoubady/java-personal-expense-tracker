# REST API Endpoint Creation Template

## Usage Pattern: `/api Generate REST endpoint for [operation] with [validation requirements]`

### Template Structure

```text
/generate Create REST endpoint for [OPERATION_NAME] with comprehensive implementation:

Endpoint Configuration:
- HTTP Method: [GET/POST/PUT/DELETE]
- URL Pattern: /api/[RESOURCE_PATH]/[PATH_VARIABLES]
- Request/Response Content-Type: application/json

Request Handling:
- [REQUEST_PARAMETERS] (e.g., @PathVariable, @RequestParam, @RequestBody)
- [VALIDATION_RULES] (e.g., @Valid, @NotNull, custom validators)
- [AUTHENTICATION_REQUIREMENTS] (e.g., JWT, basic auth, role-based)

Business Logic:
- [SERVICE_LAYER_CALLS] (e.g., service methods, transaction management)
- [DATA_PROCESSING] (e.g., mapping, filtering, aggregation)
- [ERROR_SCENARIOS] (e.g., not found, validation failures, business rule violations)

Response Handling:
- [SUCCESS_RESPONSES] (e.g., 200 OK, 201 Created, 204 No Content)
- [ERROR_RESPONSES] (e.g., 400 Bad Request, 404 Not Found, 409 Conflict)
- [RESPONSE_BODY_FORMAT] (e.g., DTO structure, pagination, metadata)

Include comprehensive exception handling with proper HTTP status codes
Add OpenAPI/Swagger documentation annotations
Implement request/response logging for debugging
```

### Examples

- CRUD operations for business entities
- Search endpoints with filtering and pagination
- File upload/download endpoints
- Batch processing endpoints
- Integration with external APIs

### Sample Usage

```text
/api Generate REST endpoint for expense search with filtering:
- GET /api/expenses/search with category and date range filters
- Pagination support with page size validation
- Comprehensive error handling for invalid parameters
```