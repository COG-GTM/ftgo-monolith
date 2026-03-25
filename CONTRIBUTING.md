# Contributing to FTGO Monolith

Thank you for your interest in contributing to the FTGO Monolith project! This guide will help you get started.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Environment Setup](#development-environment-setup)
- [Building the Project](#building-the-project)
- [Running the Application Locally](#running-the-application-locally)
- [Running Tests](#running-tests)
- [Submitting Changes](#submitting-changes)
- [Code Style](#code-style)

## Getting Started

1. **Fork** the repository on GitHub.
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/<your-username>/ftgo-monolith.git
   cd ftgo-monolith
   ```
3. **Add the upstream remote** so you can keep your fork in sync:
   ```bash
   git remote add upstream https://github.com/microservices-patterns/ftgo-monolith.git
   ```

## Development Environment Setup

Make sure you have the following tools installed:

| Tool             | Version       | Notes                                      |
|------------------|---------------|--------------------------------------------|
| **JDK**          | 8             | Required — the project targets Java 8      |
| **Docker**       | 18.06+        | Used for MySQL and infrastructure services  |
| **Docker Compose** | 1.22+       | Orchestrates local infrastructure           |
| **Gradle**       | 4.10+ (wrapper included) | No global install needed — use `./gradlew` |

### Setting JAVA_HOME

Ensure your `JAVA_HOME` environment variable points to a JDK 8 installation:

```bash
export JAVA_HOME=/path/to/jdk8
```

## Building the Project

The project uses the Gradle wrapper, so no global Gradle installation is needed:

```bash
./gradlew build
```

To compile without running tests:

```bash
./gradlew compileJava
```

## Running the Application Locally

### Using the provided scripts

1. **Start infrastructure services** (MySQL):
   ```bash
   ./start-infrastructure-services.sh
   ```

2. **Build and run the application**:
   ```bash
   ./build-and-run.sh
   ```

The application will be available at `http://localhost:8081`.

### Using Docker Compose directly

```bash
docker-compose up --build
```

### Swagger UI

Once the application is running, you can explore the API documentation via Swagger UI at:

- `http://localhost:8081/swagger-ui.html`

## Running Tests

### Unit tests

```bash
./gradlew test
```

### Integration tests

Integration tests require a running MySQL instance. Start the infrastructure first, then run:

```bash
./gradlew integrationTest
```

### End-to-end tests

End-to-end tests run against a fully deployed application. See the `ftgo-end-to-end-tests/` module for details.

```bash
./gradlew :ftgo-end-to-end-tests:test
```

> **Note:** End-to-end tests may require additional dependencies that are not available in public repositories. See the project README for more information.

## Submitting Changes

### Branch naming

Create a descriptive branch from `master`:

```bash
git checkout -b feature/short-description
# or
git checkout -b fix/short-description
```

Use prefixes like:
- `feature/` — new functionality
- `fix/` — bug fixes
- `docs/` — documentation changes
- `refactor/` — code restructuring

### Commit messages

Write clear, concise commit messages:

```
<type>: <short summary>

<optional body explaining the change in more detail>
```

Examples:
- `feat: add delivery time estimation to orders`
- `fix: correct order total calculation for revised orders`
- `docs: update README with build instructions`

### Pull request process

1. Push your branch to your fork:
   ```bash
   git push origin feature/short-description
   ```
2. Open a Pull Request against the `master` branch of the upstream repository.
3. Provide a clear description of the changes and the motivation behind them.
4. Ensure all tests pass before requesting a review.
5. Address any review feedback promptly.

## Code Style

This project follows standard Java and Spring conventions:

- **Indentation:** 4 spaces (no tabs)
- **Naming:** Use standard Java naming conventions — `camelCase` for methods and variables, `PascalCase` for classes
- **Annotations:** Place Spring annotations (`@Service`, `@Repository`, `@RestController`, etc.) on their own lines
- **Imports:** Avoid wildcard imports; organize imports alphabetically
- **Testing:** Write unit tests for new business logic; use descriptive test method names
- **Dependencies:** Use `implementation` and `testImplementation` configurations in Gradle (not the deprecated `compile`/`testCompile`)

When in doubt, follow the patterns established in the existing codebase.
