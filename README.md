# HMCTS Task Management API

A production-ready RESTful API for managing caseworker tasks with comprehensive audit trail capabilities. Built with Spring Boot 3, featuring full CRUD operations, task assignment workflows, and complete change history tracking.

## Features

- **Task Management**: Complete CRUD operations for caseworker tasks
- **Assignment System**: Assign tasks to caseworkers with full relationship management
- **Audit Trail**: Automatic tracking of all task changes (status, assignments) with timeline API
- **Data Persistence**: H2 file-based database with JPA/Hibernate
- **Validation**: Jakarta Bean Validation with custom error handling
- **API Documentation**: Interactive Swagger/OpenAPI documentation
- **Comprehensive Testing**: Unit, integration, functional, and smoke tests
- **Code Quality**: Checkstyle, OWASP dependency checks, JaCoCo coverage reports

## Technology Stack

- **Java 21** - Modern LTS Java version
- **Spring Boot 3.5.5** - Application framework
- **Spring Data JPA** - Data persistence layer
- **H2 Database** - Embedded file-based database
- **Hibernate 6** - ORM with relationship mapping
- **Lombok** - Boilerplate code reduction
- **Gradle 8.14.3** - Build automation
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework for unit tests
- **MockMvc** - Spring MVC integration testing
- **REST-assured** - API functional testing
- **Springdoc OpenAPI** - API documentation
- **JaCoCo** - Code coverage reporting

## Prerequisites

- Java 21 or higher
- Gradle 8.x (or use included wrapper `./gradlew`)

## Quick Start

### Build the Project
```bash
./gradlew build
```

### Run the Application
```bash
./gradlew bootRun
```

The API will be available at `http://localhost:4000`

### Access API Documentation
Once running, visit:
- **Swagger UI**: http://localhost:4000/swagger-ui.html
- **OpenAPI JSON**: http://localhost:4000/v3/api-docs
- **H2 Console**: http://localhost:4000/h2-console (JDBC URL: `jdbc:h2:file:./data/tasksdb`)

## API Endpoints

### Task Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tasks` | Create a new task |
| GET | `/api/tasks` | Retrieve all tasks |
| GET | `/api/tasks/{id}` | Retrieve task by ID |
| PUT | `/api/tasks/{id}` | Update task |
| PATCH | `/api/tasks/{id}/status` | Update task status only |
| DELETE | `/api/tasks/{id}` | Delete task |
| GET | `/api/tasks/{id}/timeline` | Get task change history |

### Caseworker Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/caseworkers` | Retrieve all caseworkers |
| GET | `/api/caseworkers/{id}` | Retrieve caseworker by ID |

## Testing

The project includes four test suites:

### Run All Tests
```bash
./gradlew test integration functional smoke
```

### Individual Test Suites

**Unit Tests** (Service layer logic)
```bash
./gradlew test
```
- `TaskServiceTest` - Task business logic
- `CaseworkerServiceTest` - Caseworker operations
- `TaskHistoryServiceTest` - Audit trail recording

**Integration Tests** (Controller + Repository)
```bash
./gradlew integration
```
- `TaskControllerIntegrationTest` - Task API endpoints
- `CaseworkerControllerIntegrationTest` - Caseworker API endpoints
- `TaskHistoryIntegrationTest` - Timeline API
- `TaskRepositoryIntegrationTest` - Database operations

**Functional Tests** (End-to-end)
```bash
./gradlew functional
```
- `TaskManagementFunctionalTest` - Complete workflow testing

**Smoke Tests** (Health checks)
```bash
./gradlew smoke
```
- `TaskManagementSmokeTest` - Service availability verification

### Code Coverage Report
```bash
./gradlew jacocoTestReport
```
Report location: `build/reports/jacoco/test/html/index.html`

## Code Quality

### Run Checkstyle
```bash
./gradlew checkstyleMain checkstyleTest
```

### Run OWASP Dependency Check
```bash
./gradlew dependencyCheckAnalyze
```

### Run All Quality Checks
```bash
./gradlew check
```

## Database

### Schema
The application uses H2 database with three main entities:

- **Tasks**: Core task information (title, description, status, due date, assignee)
- **Caseworkers**: User information (name, email, role)
- **TaskHistory**: Audit trail (change type, old/new values, timestamp)

### Sample Data
5 caseworkers are preloaded on startup for testing purposes.

### Database Access
- **File Location**: `./data/tasksdb.mv.db`
- **Console URL**: http://localhost:4000/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/tasksdb`
- **Username**: `sa`
- **Password**: *(empty)*

## Project Structure

```
src/
├── main/
│   ├── java/uk/gov/hmcts/reform/dev/
│   │   ├── config/          # Configuration classes
│   │   ├── controllers/     # REST API endpoints
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entities/        # JPA entities
│   │   ├── exception/       # Custom exceptions & handlers
│   │   ├── repository/      # Data access layer
│   │   └── service/         # Business logic layer
│   └── resources/
│       └── application.yaml # Application configuration
├── test/                    # Unit tests
├── integrationTest/         # Integration tests
├── functionalTest/          # Functional tests
└── smokeTest/              # Smoke tests
```

## Key Implementation Highlights

1. **Clean Architecture**: Clear separation of concerns (Controller → Service → Repository)
2. **Validation**: Jakarta Bean Validation with custom error responses
3. **Exception Handling**: Global exception handler with structured error responses
4. **Audit Trail**: Automatic change tracking for compliance and timeline rendering
5. **Relationship Management**: Proper JPA relationships with cascade operations
6. **Test Coverage**: Comprehensive test suite covering all layers
7. **API Documentation**: Complete OpenAPI/Swagger documentation with examples
8. **Code Standards**: Follows HMCTS Java coding standards with checkstyle enforcement

## Example Usage

### Create a Task
```bash
curl -X POST http://localhost:4000/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Review case documents",
    "description": "Review and approve case documentation",
    "status": "PENDING",
    "dueDateTime": "2025-12-31T17:00:00",
    "assignedToId": 1
  }'
```

### Get Task Timeline
```bash
curl http://localhost:4000/api/tasks/1/timeline
```

## Development Guidelines

- Follow existing code patterns and structure
- Write tests for all new features (unit + integration)
- Update API documentation with Swagger annotations
- Run `./gradlew check` before committing
- Ensure all tests pass: `./gradlew test integration`
- Additional tests: `./gradlew functional smoke`

## License

This project is part of the HMCTS development assessment.

## Author

Developed as a technical assessment for HMCTS demonstrating:
- RESTful API design and implementation
- Spring Boot ecosystem proficiency
- Clean code and architecture principles
- Comprehensive testing strategies
- Production-ready code quality standards
