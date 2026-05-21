# FTGO Consumer Service

Standalone microservice extracted from the [FTGO Monolith](https://github.com/COG-GTM/ftgo-monolith). Manages consumer data and order validation for the FTGO food delivery platform.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/consumers` | Create a new consumer |
| `GET` | `/consumers/{consumerId}` | Get consumer by ID |
| `POST` | `/consumers/{consumerId}/validate` | Validate an order for a consumer |

## Running

### Prerequisites
- Java 8+
- MySQL with a `ftgo_consumer` database

### Start the service
```bash
./gradlew bootRun
```

The service runs on port **8082** by default.

### Configuration
Configure via environment variables or `application.yml`:
- `DOCKER_HOST_IP` — MySQL host (default: `localhost`)
- Database: `ftgo_consumer`
- Credentials: `mysqluser` / `mysqlpw`

## Build
```bash
./gradlew build
```

## Database
Flyway manages schema migrations automatically on startup. The consumer service uses its own dedicated database (`ftgo_consumer`), separate from the monolith's `ftgo` database.
