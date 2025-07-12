# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot web application written in Kotlin, managed with Gradle. The project is in its initial state with minimal setup.

**Tech Stack:**
- Spring Boot 3.5.3
- Kotlin 1.9.25
- Java 21
- Gradle build system

## Common Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "nexters.tuk_server.TukServerApplicationTests"

# Clean build
./gradlew clean build
```

### Development
```bash
# Check for dependency updates
./gradlew dependencyUpdates

# Generate build scan
./gradlew build --scan
```

## Architecture

This is a standard Spring Boot application with the following structure:

- **Main Application**: `nexters.tuk_server.TukServerApplication` - Standard Spring Boot entry point
- **Package Structure**: Uses `nexters.tuk_server` (note: underscore instead of hyphen due to Kotlin package naming requirements)
- **Configuration**: Minimal configuration in `application.properties` with just the application name

The project currently contains only the basic Spring Boot starter dependencies:
- `spring-boot-starter-web` for web functionality
- `jackson-module-kotlin` for JSON processing
- `kotlin-reflect` for Kotlin reflection support

## Important Notes

- The package name was changed from `nexters.tuk-server` to `nexters.tuk_server` due to Kotlin package naming conventions
- Uses Java 21 toolchain
- JUnit 5 is configured for testing
- Currently has no custom business logic implemented