# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-module Spring Boot project written in Kotlin, managed with Gradle. The project is structured to support multiple modules including API and batch processing.

**Tech Stack:**
- Spring Boot 3.5.3
- Kotlin 1.9.25
- Java 21
- Gradle multi-module build system
- Spring Boot Actuator for monitoring

## Project Structure

```
tuk-server/
├── tuk-api/                    # API module (web application)
│   ├── src/main/kotlin/
│   │   └── nexters/tuk/
│   │       └── TukServerApplication.kt
│   ├── src/main/resources/
│   │   └── application.yml
│   └── build.gradle.kts
├── build.gradle.kts           # Root build configuration
└── settings.gradle.kts        # Module configuration
```

## Common Commands

### Build and Run
```bash
# Build all modules
./gradlew build

# Run the API application
./gradlew :tuk-api:bootRun

# Run tests for all modules
./gradlew test

# Run tests for specific module
./gradlew :tuk-api:test

# Run a single test class
./gradlew :tuk-api:test --tests "nexters.tuk.TukServerApplicationTests"

# Clean build
./gradlew clean build
```

### Development
```bash
# Check for dependency updates
./gradlew dependencyUpdates

# Generate build scan
./gradlew build --scan

# Build specific module
./gradlew :tuk-api:build
```

## Architecture

This is a multi-module Spring Boot project with the following structure:

### Modules
- **tuk-api**: Web API module containing REST controllers and web-related functionality
- **tuk-batch**: (Future) Batch processing module for background jobs

### Current Implementation
- **Main Application**: `nexters.tuk.TukServerApplication` in tuk-api module
- **Package Structure**: Uses `nexters.tuk` package naming
- **Configuration**: YAML-based configuration in `application.yml`
- **Monitoring**: Spring Boot Actuator enabled with health endpoint exposed

### Dependencies (per module)
- `spring-boot-starter-web` for web functionality
- `spring-boot-starter-actuator` for monitoring and health checks
- `jackson-module-kotlin` for JSON processing
- `kotlin-reflect` for Kotlin reflection support

## Endpoints

- **Health Check**: `/actuator/health` - Application health status

## Important Notes

- Multi-module Gradle project structure ready for tuk-batch module addition
- Uses Java 21 toolchain across all modules
- JUnit 5 is configured for testing
- Spring Boot Actuator health endpoint is exposed
- YAML configuration format used instead of properties