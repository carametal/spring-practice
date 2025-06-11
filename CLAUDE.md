# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.0 practice application using:
- Java 21
- Spring Boot with Web, Security, and JPA
- PostgreSQL database
- Testcontainers for integration testing
- Docker Compose for local development
- Lombok for boilerplate reduction

## Development Commands

**Build and run:**
```bash
./gradlew bootRun
```
*Note: Spring Boot's Docker Compose integration automatically starts the database when running bootRun*

**Run tests:**
```bash
./gradlew test
```

**Run specific test:**
```bash
./gradlew test --tests "ClassName.testMethodName"
```

**Build application:**
```bash
./gradlew build
```

**Start database with Docker Compose:**
```bash
docker compose up -d
```

## Database Setup

- PostgreSQL runs on port 5432 with credentials: `spring/password`
- Database name: `practice`
- SQL initialization scripts in `sql/` directory are automatically executed
- Uses pgcrypto extension for password hashing
- Pre-created admin user: admin@example.com/admin

## Testing Architecture

- Uses Testcontainers for integration tests with real PostgreSQL instances
- TestcontainersConfiguration provides PostgreSQL container setup
- Tests run with `@ServiceConnection` for automatic database configuration
- JUnit 5 platform with Spring Boot Test framework

## Code Structure

- Main application: `src/main/java/carametal/practice/PracticeApplication.java`
- Database entities and audit fields pattern: created_by, created_at, updated_by, updated_at
- Security configuration expected (Spring Security dependency included)
- JPA repositories pattern for data access