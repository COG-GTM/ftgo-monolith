# FTGO Microservices Java Coding Standards

## 1. Package Structure Conventions

Each microservice follows a consistent package structure under `net.chrisrichardson.ftgo.{servicename}`:

```
net.chrisrichardson.ftgo.{servicename}/
├── domain/          # Domain entities, value objects, repositories, domain services
├── web/             # REST controllers, request/response DTOs, exception handlers
├── messaging/       # Event publishers, event consumers, event DTOs
├── config/          # Spring configuration classes, security config, bean definitions
└── {ServiceName}Application.java  # Spring Boot main class
```

### Package Responsibilities

| Package      | Contains                                                    | Does NOT Contain        |
|--------------|-------------------------------------------------------------|-------------------------|
| `domain`     | Entities, Repositories, Domain Services, Domain Events      | HTTP concerns, configs  |
| `web`        | Controllers, DTOs, Exception Handlers, Input Validation     | Business logic          |
| `messaging`  | Kafka listeners, Event publishers, Event serialization      | HTTP concerns           |
| `config`     | `@Configuration` classes, Security config, Bean definitions | Business logic          |

---

## 2. Naming Conventions

### Classes
| Type                  | Convention                   | Example                        |
|-----------------------|------------------------------|--------------------------------|
| Entity                | Noun, singular               | `Order`, `Consumer`, `Courier` |
| Repository            | `{Entity}Repository`         | `OrderRepository`              |
| Service (domain)      | `{Entity}Service`            | `OrderService`                 |
| Controller            | `{Entity}Controller`         | `OrderController`              |
| Configuration         | `{Feature}Configuration`     | `SecurityConfiguration`        |
| DTO (request)         | `Create{Entity}Request`      | `CreateOrderRequest`           |
| DTO (response)        | `Get{Entity}Response`        | `GetOrderResponse`             |
| Exception             | `{Description}Exception`     | `OrderNotFoundException`       |
| Event                 | `{Entity}{Action}Event`      | `OrderCreatedEvent`            |
| Event Handler         | `{Entity}EventHandler`       | `OrderEventHandler`            |

### Methods
| Type              | Convention                     | Example                          |
|-------------------|--------------------------------|----------------------------------|
| Controller GET    | `get{Entity}`, `get{Entities}` | `getOrder()`, `getOrders()`      |
| Controller POST   | `create`, `cancel`, `accept`   | `create()`, `cancel()`           |
| Service           | Verb-based                     | `createOrder()`, `cancelOrder()` |
| Repository        | Spring Data conventions        | `findByConsumerId()`             |
| Boolean methods   | `is{Condition}`, `has{Thing}`  | `isAvailable()`, `hasItems()`    |

### Variables
- Use `camelCase` for all variables
- Use descriptive names; avoid single-letter variables except in lambdas
- Collections: use plural nouns (`orders`, `consumers`)
- Booleans: prefix with `is`, `has`, `can` (`isAvailable`, `hasItems`)

---

## 3. Logging Standards

### Logger Declaration
Use SLF4J with a private static final logger in each class:

```java
private static final Logger log = LoggerFactory.getLogger(OrderService.class);
```

### Log Levels

| Level | Usage                                                        |
|-------|--------------------------------------------------------------|
| ERROR | Unrecoverable failures, data corruption, integration failures|
| WARN  | Recoverable issues, deprecated usage, retry scenarios        |
| INFO  | Business events, service lifecycle, configuration changes    |
| DEBUG | Detailed flow, method entry/exit, intermediate state         |
| TRACE | Very detailed debugging, full payloads (never in production) |

### Logging Best Practices
- Use parameterized messages: `log.info("Order {} created for consumer {}", orderId, consumerId)`
- Never log sensitive data (passwords, tokens, PII)
- Include correlation IDs (trace IDs) in structured logging
- Log at method boundaries in service layer for business operations
- Log exceptions with stack traces at ERROR level: `log.error("Failed to create order", exception)`

---

## 4. Exception Handling Patterns

### Controller-Level Exception Handling
Use `@ControllerAdvice` for centralized exception handling:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ConstraintViolationException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
```

### Custom Exceptions
- Extend `RuntimeException` for domain exceptions
- Include meaningful messages and relevant context
- Create service-specific exceptions (e.g., `OrderNotFoundException`, `InvalidMenuItemIdException`)

### Error Response Format
```json
{
  "message": "Order not found",
  "code": "ORDER_NOT_FOUND",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/orders/123"
}
```

---

## 5. Transaction Management Guidelines

### Single-Service Transactions
- Use `@Transactional` at the service layer (not controllers)
- Keep transactions short and focused
- Specify `readOnly = true` for read operations: `@Transactional(readOnly = true)`
- Default propagation: `REQUIRED`
- Default isolation: database default (typically `READ_COMMITTED`)

```java
@Transactional
public Order createOrder(CreateOrderRequest request) {
    // business logic with DB operations
}

@Transactional(readOnly = true)
public Order getOrder(Long orderId) {
    return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
}
```

### Cross-Service Transactions (Saga Pattern)
- Do NOT use distributed transactions (2PC)
- Implement the Saga pattern for operations spanning multiple services
- Each step has a compensating action for rollback
- Use event-driven choreography or orchestration-based sagas
- Document saga steps and compensation logic

### Optimistic Locking
- Use `@Version` annotation on entities for concurrent access
- Handle `OptimisticLockException` with retry logic

---

## 6. REST API Conventions

### URL Structure
- Use lowercase, hyphenated paths: `/order-line-items` (not `/orderLineItems`)
- Use plural nouns for resource collections: `/orders`, `/consumers`
- Use path variables for resource identification: `/orders/{orderId}`
- Use query parameters for filtering: `/orders?consumerId=123`
- Nested resources for strong relationships: `/orders/{orderId}/line-items`

### HTTP Methods
| Method | Usage           | Response Code     | Idempotent |
|--------|-----------------|-------------------|------------|
| GET    | Read resource   | 200 OK            | Yes        |
| POST   | Create resource | 201 Created       | No         |
| PUT    | Full update     | 200 OK            | Yes        |
| PATCH  | Partial update  | 200 OK            | No         |
| DELETE | Delete resource | 204 No Content    | Yes        |

### Response Patterns
- Return the created/updated resource in response body
- Use `ResponseEntity<T>` for explicit status code control
- Return `404 Not Found` for missing resources (not empty 200)
- Return `400 Bad Request` for validation errors with details
- Return `409 Conflict` for state violations (e.g., cancelling a delivered order)

### Controller Pattern (Reference: OrderController)
```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return new ResponseEntity<>(new CreateOrderResponse(order.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrder(@PathVariable long orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(new GetOrderResponse(order));
    }
}
```

---

## 7. Event Naming Conventions

### Event Class Names
- Format: `{Aggregate}{PastTenseVerb}Event`
- Examples: `OrderCreatedEvent`, `OrderCancelledEvent`, `CourierAssignedEvent`

### Kafka Topic Names
- Format: `ftgo.{service}.{aggregate}.{event-type}`
- Examples: `ftgo.order-service.order.created`, `ftgo.courier-service.courier.location-updated`

### Event Payload Structure
```json
{
  "eventId": "uuid",
  "eventType": "OrderCreatedEvent",
  "aggregateType": "Order",
  "aggregateId": "123",
  "timestamp": "2024-01-01T00:00:00Z",
  "payload": {
    "consumerId": 456,
    "restaurantId": 789,
    "orderTotal": "25.99"
  }
}
```

---

## 8. Dependency Injection

- Use constructor injection exclusively (no field injection with `@Autowired`)
- Single constructor does not require `@Autowired` annotation
- Mark dependencies as `final`
- Use `@Configuration` + `@Bean` for complex wiring (reference: `OrderConfiguration`)

```java
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }
}
```

---

## 9. Configuration Management

- Use `application.yml` (not `.properties`) for readability
- Externalize all environment-specific values using `${ENV_VAR:default}`
- Group related properties under a common prefix
- Use `@ConfigurationProperties` for complex configuration objects
- Never hardcode credentials, URLs, or environment-specific values
