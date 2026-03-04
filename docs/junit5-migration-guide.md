# JUnit 4 to JUnit 5 Migration Guide

## Overview

This guide documents the patterns for migrating FTGO tests from JUnit 4 (4.12) to JUnit 5 (Jupiter 5.10.x). The migration improves test expressiveness, supports parallel execution, and aligns with modern Spring Boot 3.x testing practices.

## Migration Summary

| JUnit 4                         | JUnit 5                                    |
|---------------------------------|--------------------------------------------|
| `org.junit.Test`                | `org.junit.jupiter.api.Test`               |
| `org.junit.Before`              | `org.junit.jupiter.api.BeforeEach`         |
| `org.junit.After`               | `org.junit.jupiter.api.AfterEach`          |
| `org.junit.BeforeClass`         | `org.junit.jupiter.api.BeforeAll`          |
| `org.junit.AfterClass`          | `org.junit.jupiter.api.AfterAll`           |
| `org.junit.Ignore`              | `org.junit.jupiter.api.Disabled`           |
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` |
| `@RunWith(SpringRunner.class)`  | `@ExtendWith(SpringExtension.class)` or `@SpringBootTest` |
| `@Rule` / `@ClassRule`          | `@ExtendWith` or `@RegisterExtension`      |
| `Assert.assertEquals()`         | `Assertions.assertEquals()` or AssertJ     |
| `@Test(expected = ...)`         | `assertThrows()`                           |
| `@Test(timeout = ...)`          | `@Timeout`                                 |

## Step-by-Step Migration

### Step 1: Update Imports

**Before (JUnit 4):**
```java
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Assert;
```

**After (JUnit 5):**
```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assertions;
```

### Step 2: Update Test Class Visibility

JUnit 4 requires test classes and methods to be `public`. JUnit 5 allows package-private visibility.

**Before:**
```java
public class OrderServiceTest {
    @Test
    public void shouldCreateOrder() { ... }
}
```

**After:**
```java
class OrderServiceTest {
    @Test
    void shouldCreateOrder() { ... }
}
```

### Step 3: Migrate Lifecycle Annotations

**Before:**
```java
@Before
public void setUp() { ... }

@After
public void tearDown() { ... }

@BeforeClass
public static void setUpClass() { ... }

@AfterClass
public static void tearDownClass() { ... }
```

**After:**
```java
@BeforeEach
void setUp() { ... }

@AfterEach
void tearDown() { ... }

@BeforeAll
static void setUpClass() { ... }

@AfterAll
static void tearDownClass() { ... }
```

### Step 4: Migrate Runner to Extension

**Before (Mockito):**
```java
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    ...
}
```

**After (Mockito):**
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    ...
}
```

**Before (Spring):**
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderIntegrationTest { ... }
```

**After (Spring):**
```java
@SpringBootTest  // @ExtendWith(SpringExtension.class) is implied
class OrderIntegrationTest { ... }
```

### Step 5: Migrate Assertions

**Before (JUnit 4 Assert):**
```java
Assert.assertEquals("Expected state", "APPROVED", order.getState());
Assert.assertNotNull(order);
Assert.assertTrue(order.isApproved());
Assert.assertNull(order.getCancelReason());
```

**After (AssertJ — recommended):**
```java
assertThat(order.getState()).isEqualTo("APPROVED");
assertThat(order).isNotNull();
assertThat(order.isApproved()).isTrue();
assertThat(order.getCancelReason()).isNull();
```

**After (JUnit 5 Assertions — alternative):**
```java
assertEquals("APPROVED", order.getState(), "Expected state");
assertNotNull(order);
assertTrue(order.isApproved());
assertNull(order.getCancelReason());
```

> **Note:** JUnit 5 puts the message parameter LAST, not first. This is the opposite of JUnit 4.

### Step 6: Migrate Exception Testing

**Before:**
```java
@Test(expected = OrderNotFoundException.class)
public void shouldThrowForMissingOrder() {
    orderService.findById(999L);
}
```

**After (JUnit 5):**
```java
@Test
void shouldThrowForMissingOrder() {
    assertThrows(OrderNotFoundException.class, () ->
        orderService.findById(999L));
}
```

**After (AssertJ):**
```java
@Test
void shouldThrowForMissingOrder() {
    assertThatThrownBy(() -> orderService.findById(999L))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("not found");
}
```

### Step 7: Migrate Timeout Testing

**Before:**
```java
@Test(timeout = 5000)
public void shouldCompleteWithinTimeout() { ... }
```

**After:**
```java
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
void shouldCompleteWithinTimeout() { ... }
```

### Step 8: Migrate Rules to Extensions

**Before (Temporary Folder):**
```java
@Rule
public TemporaryFolder tempFolder = new TemporaryFolder();
```

**After:**
```java
@TempDir
Path tempDir;
```

**Before (ExpectedException):**
```java
@Rule
public ExpectedException thrown = ExpectedException.none();

@Test
public void shouldThrow() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("invalid");
    service.process(null);
}
```

**After:**
```java
@Test
void shouldThrow() {
    assertThatThrownBy(() -> service.process(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("invalid");
}
```

### Step 9: Add Display Names

JUnit 5 supports `@DisplayName` for human-readable test names:

```java
@DisplayName("Order Service")
class OrderServiceTest {

    @Nested
    @DisplayName("create order")
    class CreateOrder {

        @Test
        @DisplayName("should create order with valid consumer")
        void shouldCreateOrderWithValidConsumer() { ... }

        @Test
        @DisplayName("should reject order when consumer not found")
        void shouldRejectOrderWhenConsumerNotFound() { ... }
    }
}
```

### Step 10: Use Nested Test Classes

JUnit 5 `@Nested` classes group related tests for better organization:

```java
class OrderServiceTest {

    @Nested
    class WhenOrderIsPending {
        @Test void canBeApproved() { ... }
        @Test void canBeCancelled() { ... }
    }

    @Nested
    class WhenOrderIsApproved {
        @Test void cannotBeApprovedAgain() { ... }
        @Test void canBeCancelled() { ... }
    }
}
```

## FTGO-Specific Migration Examples

### Monolith OrderControllerTest

**Before (JUnit 4 — current monolith):**
```java
public class OrderControllerTest {

    private OrderService orderService;
    private OrderRepository orderRepository;

    @Before
    public void setUp() throws Exception {
        orderService = mock(OrderService.class);
        orderRepository = mock(OrderRepository.class);
    }

    @Test
    public void shouldFindOrder() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

        given().standaloneSetup(configureControllers(orderController))
            .when().get("/orders/1")
            .then().statusCode(200)
                   .body("orderId", equalTo(1));
    }
}
```

**After (JUnit 5 — migrated):**
```java
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Controller")
class OrderControllerTest {

    @Mock
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderController orderController;

    @Nested
    @DisplayName("GET /orders/{id}")
    class GetOrder {

        @Test
        @DisplayName("should return order when found")
        void shouldReturnOrderWhenFound() {
            given(orderRepository.findById(1L))
                .willReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

            RestAssuredMockMvc.given()
                .standaloneSetup(orderController)
            .when()
                .get("/orders/1")
            .then()
                .statusCode(200)
                .body("orderId", equalTo(1));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() {
            given(orderRepository.findById(1L))
                .willReturn(Optional.empty());

            RestAssuredMockMvc.given()
                .standaloneSetup(orderController)
            .when()
                .get("/orders/1")
            .then()
                .statusCode(404);
        }
    }
}
```

## Gradle Configuration

The `FtgoTestingConventionsPlugin` already configures JUnit 5 for all microservice modules:

```groovy
// Applied automatically by the convention plugin
test {
    useJUnitPlatform()  // Enables JUnit 5 test engine
}

dependencies {
    testImplementation "org.junit.jupiter:junit-jupiter:5.10.2"
    testImplementation "org.mockito:mockito-junit-jupiter:5.11.0"
    testImplementation "org.assertj:assertj-core:3.25.3"
}
```

## Migration Checklist

For each test class being migrated:

- [ ] Update imports from `org.junit` to `org.junit.jupiter.api`
- [ ] Replace `@Before`/`@After` with `@BeforeEach`/`@AfterEach`
- [ ] Replace `@RunWith` with `@ExtendWith`
- [ ] Remove `public` from class and method declarations
- [ ] Replace `Assert.*` with AssertJ `assertThat()` or JUnit 5 `Assertions.*`
- [ ] Replace `@Test(expected=...)` with `assertThrows()` or `assertThatThrownBy()`
- [ ] Replace `@Rule`/`@ClassRule` with `@ExtendWith`/`@RegisterExtension`
- [ ] Add `@DisplayName` annotations
- [ ] Consider grouping with `@Nested` classes
- [ ] Add appropriate `@Tag` annotation (`"unit"`, `"integration"`, `"api"`)
- [ ] Verify test passes with `./gradlew test`

## Related Documentation

- [Testing Strategy](testing-strategy.md)
- [Testcontainers Guide](testcontainers-guide.md)
- [When to Write Which Test](when-to-test.md)
