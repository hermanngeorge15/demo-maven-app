# Demo Maven App - Weather Service

A Spring Boot Kotlin application demonstrating Maven multi-module setup with code quality tools (ktlint, detekt) and GitHub Actions CI/CD.

## Prerequisites

- Java 21+
- Maven 3.9+ (or use included `./mvnw` wrapper)

## Project Structure

```
demo-maven-app/
├── pom.xml                 # Parent POM with plugin management
├── app/                    # Application module
│   ├── pom.xml
│   └── src/
│       ├── main/kotlin/    # Kotlin source code
│       └── test/kotlin/    # Test code
└── .github/
    └── workflows/
        └── pr-checks.yml   # GitHub Actions workflow
```

## Build & Run

### Build the project
```bash
./mvnw clean install
```

### Run the application
```bash
./mvnw spring-boot:run -pl app
```

### Run tests only
```bash
./mvnw test -pl app
```

## Code Quality Tools

### ktlint (Code Formatting)

ktlint automatically formats Kotlin code during the build.

```bash
# Auto-format code (runs during build)
./mvnw exec:exec@ktlint-format -pl app

# Check only (no auto-fix, for CI)
./mvnw exec:exec@ktlint-check -pl app
```

**Rules enforced:**
- No wildcard imports
- Consistent indentation
- Trailing commas
- Import ordering

### detekt (Static Analysis)

detekt analyzes code for potential issues and code smells.

```bash
# Run detekt analysis
./mvnw antrun:run@detekt -pl app

# Run as part of verify phase
./mvnw verify -pl app
```

**Report location:** `app/target/reports/detekt.xml`

**Rules enforced:**
- Magic numbers
- Swallowed exceptions
- Too many return statements
- Unused parameters
- Generic exception handling
- And more...

## API Endpoints

Base URL: `http://localhost:8080/api/weather`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{city}` | Get current weather for a city |
| GET | `/{city}?includeForecast=true&days=5` | Get weather with forecast |
| GET | `/{city}?unit=FAHRENHEIT` | Get weather in Fahrenheit/Kelvin |
| GET | `/{city}/forecast?days=7` | Get forecast only |
| GET | `/{city}/alerts` | Get weather alerts |
| POST | `/compare` | Compare weather for multiple cities |
| GET | `/report/{city}` | Get formatted weather report |
| DELETE | `/cache` | Clear weather cache |

### Example Requests

```bash
# Get weather for Prague
curl http://localhost:8080/api/weather/prague

# Get weather with 5-day forecast in Fahrenheit
curl "http://localhost:8080/api/weather/london?includeForecast=true&days=5&unit=FAHRENHEIT"

# Get alerts for Moscow
curl http://localhost:8080/api/weather/moscow/alerts

# Compare cities
curl -X POST http://localhost:8080/api/weather/compare \
  -H "Content-Type: application/json" \
  -d '["prague", "london", "tokyo", "moscow"]'
```

### Available Cities (Mock Data)

- Prague
- London
- New York
- Tokyo
- Moscow

## GitHub Actions CI/CD

The project includes a GitHub Actions workflow (`.github/workflows/pr-checks.yml`) that runs on every pull request:

| Job | Description |
|-----|-------------|
| **build-and-test** | Compiles and runs all tests |
| **ktlint** | Checks code style, comments on PR if issues |
| **detekt** | Static analysis, comments on PR with summary |
| **test-report** | Publishes test results to PR |

### PR Comments

When issues are found, the workflow automatically comments on the PR:

- **ktlint failures:** Lists violations with fix instructions
- **detekt issues:** Summary by category with issue count

## Configuration

### Detekt Behavior

By default, detekt reports issues but doesn't fail the build locally. To make it fail:

Edit `pom.xml` line 150:
```xml
failonerror="true"   <!-- Change to "true" to fail on issues -->
```

### ktlint Behavior

ktlint auto-formats code during the `validate` phase. To disable auto-format:
- Remove the `ktlint-format` execution from `pom.xml`
- Use only `ktlint-check` for CI validation

## Testing

The project includes 33 tests:

- **Unit tests:** `WeatherServiceTest` (15 tests)
- **Integration tests:** `WeatherControllerTest` (18 tests)

### Test Categories

- Temperature conversion (Celsius/Fahrenheit/Kelvin)
- Weather alerts (heat, freeze, wind, humidity warnings)
- Forecast generation
- Cache behavior
- API validation
- Error handling

## Technology Stack

- **Language:** Kotlin 1.9.25
- **Framework:** Spring Boot 3.5.9
- **Build:** Maven
- **Code Style:** ktlint 1.8.0
- **Static Analysis:** detekt 1.23.8
- **Testing:** JUnit 5, Mockito, Spring MockMvc
