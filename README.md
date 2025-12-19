# User Profile Service

A microservice for managing user profile information built with Spring Boot 3.5.4.

## Overview

User Profile Service is a REST-based microservice that provides endpoints for creating, retrieving, updating, and managing user profile information. It features caching support via Redis, database persistence with MySQL, circuit breaker resilience patterns, and comprehensive error handling.

## Technology Stack

- **Java**: 17
- **Framework**: Spring Boot 3.5.4
- **ORM**: Spring Data JPA / Hibernate
- **Database**: MySQL
- **Caching**: Redis with Spring Cache abstraction
- **Build Tool**: Maven
- **Resilience**: Resilience4j (Circuit Breaker, Retry)
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Additional Libraries**: 
  - Lombok (for reducing boilerplate code)
  - Spring Cloud (2025.0.0)
  - Spring WebFlux (for reactive operations)
  - Spring Actuator (for monitoring)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/ksb/micro/user_profile/
│   │       ├── UserProfileApplication.java (Main application entry point)
│   │       ├── config/
│   │       │   ├── RedisCacheConfig.java (Redis cache configuration)
│   │       │   └── WebClientConfig.java (WebClient configuration for HTTP calls)
│   │       ├── controller/
│   │       │   └── UserProfileController.java (REST endpoints)
│   │       ├── service/
│   │       │   └── impl/ (Business logic implementation)
│   │       ├── repository/
│   │       │   └── UserProfileRepository.java (Data access layer)
│   │       ├── model/
│   │       │   └── UserProfile.java (Domain entity)
│   │       └── exception/
│   │           ├── GlobalExceptionHandler.java (Centralized exception handling)
│   │           ├── InvalidRequestException.java
│   │           ├── PhotoServiceException.java
│   │           └── ResourceNotFoundException.java
│   └── resources/
│       ├── application.properties (Configuration properties)
│       └── import.sql (Initial data)
└── test/
    └── java/ (Unit and integration tests)
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### Installation & Running

1. **Clone and navigate to project:**
   ```bash
   cd users-profile-service
   ```

2. **Configure database and Redis:**
   Update `src/main/resources/application.properties` with your database and Redis credentials.

3. **Build the project:**
   ```bash
   mvn clean install
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

5. **Access Swagger UI:**
   Navigate to `http://localhost:8081/swagger-ui.html`

## Key Features

- **REST API**: Full CRUD operations for user profiles
- **Caching**: Redis-based caching for improved performance
- **Resilience**: Circuit breaker pattern for fault tolerance
- **Monitoring**: Spring Actuator endpoints for health and metrics
- **API Documentation**: Auto-generated Swagger/OpenAPI documentation
- **Exception Handling**: Centralized error handling with custom exceptions
- **Reactive Support**: WebFlux integration for non-blocking operations

## Build Information

- **Maven Coordinates**: `com.ksb.micro:user-profile-service:0.0.1-SNAPSHOT`
- **Spring Boot Version**: 3.5.4
- **Spring Cloud Version**: 2025.0.0

## Key Features

- **REST API**: Full CRUD operations for user profiles
- **Caching**: Redis-based caching for improved performance
- **Error Handling**: Comprehensive exception handling and validation
- **Database Integration**: MySQL database for persistent storage
- **API Documentation**: Swagger/OpenAPI configuration for API docs
- **Monitoring**: Spring Boot Actuator endpoints for health checks and metrics

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL Server running
- Redis Server running (optional, for caching features)

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd users-profile-service
   ```

2. **Configure Database**
   Update the database connection settings in `application.properties`:
   - Database URL
   - Username and password

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   
   Or run the JAR file:
   ```bash
   java -jar target/user-profile-service-0.0.1-SNAPSHOT.jar
   ```

## Configuration

The application runs on **port 8081** by default.

### Key Configuration Properties

- `spring.application.name`: user-profile
- `server.port`: 8081
- `spring.jpa.hibernate.ddl-auto`: update
- Redis caching enabled with TTL: 10 minutes

## API Endpoints

The service provides REST endpoints for user profile management through the UserProfileController. Refer to the Swagger/OpenAPI documentation (usually available at `http://localhost:8081/swagger-ui.html`) for detailed endpoint information.

## Testing

Run the test suite using Maven:
```bash
mvn test
```

## Monitoring & Health Checks

Spring Boot Actuator endpoints are exposed at `/actuator` for monitoring:
- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Other diagnostic endpoints available

## Build & Deployment

The project is built using Maven and produces a JAR artifact:
- Location: `target/user-profile-service-0.0.1-SNAPSHOT.jar`

## Notes

- The application implements caching to optimize database queries
- Database schema is automatically updated on startup
- Global exception handling provides consistent error responses across all endpoints