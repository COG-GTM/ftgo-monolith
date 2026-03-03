# JUnit 4 to JUnit 5 Migration Guide

## Overview

This guide documents the patterns for migrating from JUnit 4.12 to JUnit 5 (Jupiter) as part of the FTGO microservices migration. The monolith currently uses JUnit 4 with Hamcrest matchers and manual Mockito setup. New microservices should use JUnit 5 exclusively.

## Table of Contents

1. [Quick Reference: Annotation Changes](#quick-reference-annotation-changes)
2. [Dependency Changes](#dependency-changes)
3. [Migration Patterns](#migration-patterns)
4. [Assertion Migration](#assertion-migration)
5. [Mockito Migration](#mockito-migration)
6. [Test Lifecycle Changes](#test-lifecycle-changes)
7. [Advanced Features](#advanced-features)
8. [Gradual Migration Strategy](#gradual-migration-strategy)
9. [Common Pitfalls](#common-pitfalls)

---

## Quick Reference: Annotation Changes

| JUnit 4 | JUnit 5 | Notes |
|---------|---------|-------|
| `@org.junit.Test` | `@org.junit.jupiter.api.Test` | Different package |
| `@Before` | `@BeforeEach` | Runs before each test |
| `@After` | `@AfterEach` | Runs after each test |
| `@BeforeClass` | `@BeforeAll` | Must be `static` (unless `@TestInstance(PER_CLASS)`) |
| `@AfterClass` | `@AfterAll` | Must be `static` (unless `@TestInstance(PER_CLASS)`) |
| `@Ignore` | `@Disabled` | Optionally provide a reason string |
| `@RunWith(...)` | `@ExtendWith(...)` | Supports multiple extensions |
| `@Rule` | `@ExtendWith(...)` or `@RegisterExtension` | Rules replaced by extensions |
| `@Category(...)` | `@Tag(...)` | For filtering test execution |
| `expected = Exception.class` | `assertThrows(...)` | More flexible exception testing |
| N/A | `@DisplayName` | Human-readable test names |
| N/A | `@Nested` | Inner test classes for grouping |
| N/A | `@ParameterizedTest` | Built-in parameterized tests |

---

## Dependency Changes

### JUnit 4 (Current Monolith)

```groovy
// gradle.properties
// Uses JUnit 4.12 via Spring Boot 2.0.3

dependencies {
    testCompile "org.springframework.boot:spring-boot-starter-test:$springBootVersion"
    // Includes: junit:junit:4.12, mockito-core, hamcrest
}
```

### JUnit 5 (New Microservices)

```groovy
// gradle/libs.versions.toml
// [versions]
// junit-jupiter = "5.10.2"
// mockito = "5.11.0"
// assertj = "3.25.3"

dependencies {
    testImplementation libs.bundles.testing
    // Includes: junit-jupiter, mockito-core, mockito-junit-jupiter, assertj-core
    // spring-boot-starter-test (3.2.5) includes JUnit 5 by default
}
```

### Dual-Mode (During Migration)

If you need to run both JUnit 4 and JUnit 5 tests simultaneously:

```groovy
dependencies {
    // JUnit 5
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    
    // Vintage engine runs JUnit 4 tests on JUnit 5 platform
    testRuntimeOnly 'org.junit.vintage:junit-vintage-engine:5.10.2'
    
    // JUnit 4 (for legacy tests)
    testImplementation 'junit:junit:4.12'
}

test {
    useJUnitPlatform()  // Required for JUnit 5
}
```

---

## Migration Patterns

### Pattern 1: Basic Test Class

**JUnit 4 (Before):**
```java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrderTest {

    private Order order;

    @Before
    public void setUp() {
        order = new Order(1L, restaurant, lineItems);
    }

    @Test
    public void testCreateOrder() {
        assertEquals(OrderState.APPROVED, order.getOrderState());
    }

    @Test(expected = UnsupportedStateTransitionException.class)
    public void testCancelNonApprovedOrder() {
        order.acceptTicket(LocalDateTime.now().plusHours(1));
        order.cancel(); // Should throw
    }
}
```

**JUnit 5 (After):**
```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Domain Tests")
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = OrderBuilder.anOrder().build();
    }

    @Test
    @DisplayName("should create order in APPROVED state")
    void shouldCreateOrderInApprovedState() {
        assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
    }

    @Test
    @DisplayName("should reject cancel for non-approved order")
    void shouldRejectCancelForNonApprovedOrder() {
        order.acceptTicket(LocalDateTime.now().plusHours(1));
        
        assertThatThrownBy(() -> order.cancel())
            .isInstanceOf(UnsupportedStateTransitionException.class);
    }
}
```

**Key changes:**
- Class and methods don't need to be `public`
- `@Before` becomes `@BeforeEach`
- `@Test` import changes to `org.junit.jupiter.api.Test`
- `@Test(expected = ...)` becomes `assertThatThrownBy()` or `assertThrows()`
- Added `@DisplayName` for readable output
- Used builder pattern for test data
- Used AssertJ instead of JUnit assertions

### Pattern 2: Mockito Setup

**JUnit 4 (Before) - Existing FTGO Pattern:**
```java
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private OrderController orderController;

    @Before
    public void setUp() throws Exception {
        orderService = mock(OrderService.class);
        orderRepository = mock(OrderRepository.class);
        orderController = new OrderController(orderService, orderRepository);
    }

    @Test
    public void shouldFindOrder() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));
        // ... test logic
    }
}
```

**JUnit 5 (After):**
```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Tests")
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
        void shouldFindOrder() {
            Order order = OrderBuilder.anOrder().withOrderId(1L).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            // ... test logic
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());
            // ... test logic
        }
    }
}
```

**Key changes:**
- `@RunWith(MockitoJUnitRunner.class)` or manual `mock()` becomes `@ExtendWith(MockitoExtension.class)`
- `mock(Foo.class)` becomes `@Mock` annotation
- Manual constructor injection becomes `@InjectMocks`
- Related tests grouped with `@Nested`

### Pattern 3: Rest-Assured API Test

**JUnit 4 (Before) - Existing FTGO Pattern:**
```java
import org.junit.Before;
import org.junit.Test;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

public class OrderControllerTest {

    @Before
    public void setUp() throws Exception {
        orderService = mock(OrderService.class);
        orderRepository = mock(OrderRepository.class);
        orderController = new OrderController(orderService, orderRepository);
    }

    @Test
    public void shouldFindOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

        given().
            standaloneSetup(configureControllers(orderController)).
        when().
            get("/orders/1").
        then().
            statusCode(200).
            body("orderId", equalTo(99))
        ;
    }
}
```

**JUnit 5 (After):**
```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order API Tests")
class OrderControllerApiTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderService, orderRepository);
    }

    @Test
    @DisplayName("should return order details when found")
    void shouldReturnOrderWhenFound() {
        Order order = OrderBuilder.anOrder().withOrderId(1L).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        given()
            .standaloneSetup(configureControllers(orderController))
        .when()
            .get("/orders/1")
        .then()
            .statusCode(200)
            .body("orderId", equalTo(1))
            .body("state", equalTo("APPROVED"));
    }
}
```

### Pattern 4: Spring Boot Test

**JUnit 4 (Before):**
```java
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FtgoApplicationTest {

    @Test
    public void contextLoads() {
    }
}
```

**JUnit 5 (After):**
```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Note: @ExtendWith(SpringExtension.class) is automatically included
// by @SpringBootTest in Spring Boot 2.1+
@SpringBootTest
@DisplayName("Application Context Tests")
class FtgoApplicationTest {

    @Test
    @DisplayName("should load application context")
    void contextLoads() {
    }
}
```

**Key change:** `@RunWith(SpringRunner.class)` is no longer needed with Spring Boot 2.1+.

---

## Assertion Migration

### From JUnit 4 Assert to AssertJ

| JUnit 4 | AssertJ (Recommended) |
|---------|----------------------|
| `assertEquals(expected, actual)` | `assertThat(actual).isEqualTo(expected)` |
| `assertNotNull(object)` | `assertThat(object).isNotNull()` |
| `assertTrue(condition)` | `assertThat(condition).isTrue()` |
| `assertFalse(condition)` | `assertThat(condition).isFalse()` |
| `assertNull(object)` | `assertThat(object).isNull()` |
| `assertSame(expected, actual)` | `assertThat(actual).isSameAs(expected)` |
| `assertArrayEquals(expected, actual)` | `assertThat(actual).isEqualTo(expected)` |

### From Hamcrest Matchers to AssertJ

| Hamcrest | AssertJ |
|---------|---------|
| `assertThat(x, is(y))` | `assertThat(x).isEqualTo(y)` |
| `assertThat(x, equalTo(y))` | `assertThat(x).isEqualTo(y)` |
| `assertThat(list, hasSize(3))` | `assertThat(list).hasSize(3)` |
| `assertThat(list, hasItem(x))` | `assertThat(list).contains(x)` |
| `assertThat(list, empty())` | `assertThat(list).isEmpty()` |
| `assertThat(str, containsString("x"))` | `assertThat(str).contains("x")` |
| `assertThat(str, startsWith("x"))` | `assertThat(str).startsWith("x")` |
| `assertThat(x, notNullValue())` | `assertThat(x).isNotNull()` |
| `assertThat(x, instanceOf(Y.class))` | `assertThat(x).isInstanceOf(Y.class)` |

### Exception Testing

**JUnit 4:**
```java
@Test(expected = IllegalArgumentException.class)
public void testThrows() {
    service.doSomething(null);
}

// Or with ExpectedException Rule:
@Rule
public ExpectedException thrown = ExpectedException.none();

@Test
public void testThrowsWithMessage() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("must not be null");
    service.doSomething(null);
}
```

**JUnit 5 + AssertJ:**
```java
@Test
void shouldThrowWhenNull() {
    assertThatThrownBy(() -> service.doSomething(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null");
}

// Or with JUnit 5's assertThrows:
@Test
void shouldThrowWhenNull() {
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.doSomething(null)
    );
    assertThat(ex.getMessage()).contains("must not be null");
}
```

---

## Mockito Migration

### Initialization Changes

| Approach | JUnit 4 | JUnit 5 |
|---------|---------|---------|
| Extension/Runner | `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` |
| Manual | `MockitoAnnotations.initMocks(this)` | `MockitoAnnotations.openMocks(this)` |
| Inline | `Foo foo = mock(Foo.class)` | Same (still works) |

### Strict Stubbing (JUnit 5 Default)

JUnit 5's `MockitoExtension` enables **strict stubbing** by default, which means:

1. **Unnecessary stubbings fail the test.** If you `when(...)` something that's never called, the test fails.
2. **Argument mismatch is reported.** If a stub is set up for `findById(1L)` but `findById(2L)` is called, Mockito warns you.

**Fix unnecessary stubbing warnings:**
```java
// Option 1: Remove the unused stub
// Option 2: Use lenient stubbing for specific stubs
lenient().when(mock.someMethod()).thenReturn(value);

// Option 3: Set class-level lenient mode (not recommended)
@MockitoSettings(strictness = Strictness.LENIENT)
class MyTest { ... }
```

---

## Test Lifecycle Changes

### JUnit 4 Lifecycle

```
@BeforeClass (static) → runs once before all tests
    @Before → runs before each test
        @Test → test method
    @After → runs after each test
    @Before → runs before each test
        @Test → test method
    @After → runs after each test
@AfterClass (static) → runs once after all tests
```

### JUnit 5 Lifecycle

```
@BeforeAll (static*) → runs once before all tests
    @BeforeEach → runs before each test
        @Test → test method
    @AfterEach → runs after each test
    @BeforeEach → runs before each test
        @Test → test method
    @AfterEach → runs after each test
@AfterAll (static*) → runs once after all tests

* Unless @TestInstance(Lifecycle.PER_CLASS) is used
```

### Test Instance Lifecycle

JUnit 5 creates a **new instance** per test method by default (same as JUnit 4). You can change this:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MyTest {
    // @BeforeAll and @AfterAll don't need to be static
    // Instance state is shared across tests (use with care)
}
```

---

## Advanced Features

### Parameterized Tests

**JUnit 5 built-in (replaces JUnit 4's `@Parameterized` runner):**
```java
@ParameterizedTest
@ValueSource(strings = {"APPROVED", "ACCEPTED", "PREPARING"})
@DisplayName("should parse valid order states")
void shouldParseValidOrderStates(String state) {
    assertThat(OrderState.valueOf(state)).isNotNull();
}

@ParameterizedTest
@CsvSource({
    "12.34, 2, 24.68",
    "10.00, 3, 30.00",
    "5.50,  1, 5.50"
})
@DisplayName("should calculate line item total")
void shouldCalculateLineItemTotal(String price, int quantity, String expectedTotal) {
    OrderLineItem item = OrderLineItemBuilder.anOrderLineItem()
        .withPrice(price)
        .withQuantity(quantity)
        .build();
    assertThat(item.getTotal()).isEqualTo(new Money(expectedTotal));
}

@ParameterizedTest
@EnumSource(value = OrderState.class, names = {"APPROVED", "CANCELLED"})
@DisplayName("should handle terminal states")
void shouldHandleTerminalStates(OrderState state) {
    // test logic for each state
}
```

### Nested Test Classes

```java
@DisplayName("Order")
class OrderTest {

    @Nested
    @DisplayName("when newly created")
    class WhenNew {
        @Test
        void shouldBeInApprovedState() { ... }
        
        @Test
        void shouldHaveLineItems() { ... }
    }

    @Nested
    @DisplayName("when accepted")
    class WhenAccepted {
        @Test
        void shouldTransitionToPreparing() { ... }
        
        @Test
        void shouldRejectCancel() { ... }
    }
}
```

### Conditional Test Execution

```java
@Test
@EnabledOnOs(OS.LINUX)
void onlyOnLinux() { ... }

@Test
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
void onlyInCI() { ... }

@Test
@DisabledIf("isSlowTestsDisabled")
void slowTest() { ... }
```

### Test Execution Order

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderedTest {

    @Test
    @Order(1)
    void createFirst() { ... }

    @Test
    @Order(2)
    void verifySecond() { ... }
}
```

---

## Gradual Migration Strategy

### Phase 1: Enable Dual-Mode (No Code Changes)

Add the JUnit Vintage engine to run existing JUnit 4 tests on the JUnit 5 platform:

```groovy
test {
    useJUnitPlatform()  // Enable JUnit 5 platform
}

dependencies {
    testRuntimeOnly 'org.junit.vintage:junit-vintage-engine:5.10.2'
}
```

All existing JUnit 4 tests will continue to run unchanged.

### Phase 2: Write New Tests in JUnit 5

All new tests should use JUnit 5 from this point forward:
- Import from `org.junit.jupiter.api`
- Use `@ExtendWith(MockitoExtension.class)`
- Use AssertJ for assertions
- Use test data builders from `ftgo-test-utils`

### Phase 3: Migrate Existing Tests (Service by Service)

Migrate one service at a time, starting with the simplest:

1. **ftgo-common** tests (value object tests, simple)
2. **ftgo-domain** tests (entity tests, moderate)
3. **ftgo-order-service** tests (service + API tests, complex)
4. **ftgo-consumer-service** tests
5. **ftgo-restaurant-service** tests
6. **ftgo-courier-service** tests

### Phase 4: Remove Vintage Engine

Once all tests are migrated:

```groovy
dependencies {
    // Remove this line:
    // testRuntimeOnly 'org.junit.vintage:junit-vintage-engine:5.10.2'
}
```

### Migration Checklist Per Test Class

- [ ] Change `@Test` import to `org.junit.jupiter.api.Test`
- [ ] Change `@Before` to `@BeforeEach`
- [ ] Change `@After` to `@AfterEach`
- [ ] Change `@BeforeClass` to `@BeforeAll`
- [ ] Change `@AfterClass` to `@AfterAll`
- [ ] Change `@Ignore` to `@Disabled`
- [ ] Change `@RunWith(...)` to `@ExtendWith(...)`
- [ ] Remove `public` from class and methods
- [ ] Replace `Assert.*` with AssertJ `assertThat(...)`
- [ ] Replace `@Test(expected = ...)` with `assertThatThrownBy(...)`
- [ ] Replace `@Rule` with `@ExtendWith` or `@RegisterExtension`
- [ ] Add `@DisplayName` to class and test methods
- [ ] Group related tests with `@Nested`
- [ ] Replace Mother objects with Builder pattern
- [ ] Verify tests still pass

---

## Common Pitfalls

### 1. Wrong `@Test` Import

```java
// WRONG - JUnit 4
import org.junit.Test;

// CORRECT - JUnit 5
import org.junit.jupiter.api.Test;
```

If you mix imports, tests may silently not run.

### 2. Missing `useJUnitPlatform()`

```groovy
test {
    useJUnitPlatform()  // REQUIRED for JUnit 5
}
```

Without this, JUnit 5 tests won't be discovered.

### 3. Public Methods Not Required

```java
// JUnit 4: methods MUST be public
@Test
public void testSomething() { ... }

// JUnit 5: package-private is fine
@Test
void shouldDoSomething() { ... }
```

### 4. Strict Stubbing Failures

```java
// This FAILS in JUnit 5 + MockitoExtension if never called:
when(mock.someMethod()).thenReturn(value);

// Fix: Remove unused stubs or use lenient()
lenient().when(mock.someMethod()).thenReturn(value);
```

### 5. `@BeforeAll` Must Be Static

```java
// WRONG (unless using @TestInstance(PER_CLASS))
@BeforeAll
void setUp() { ... }

// CORRECT
@BeforeAll
static void setUp() { ... }
```

### 6. Assertions in Lambda

```java
// JUnit 5 assertAll for grouped assertions:
assertAll(
    () -> assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED),
    () -> assertThat(order.getConsumerId()).isEqualTo(1L),
    () -> assertThat(order.getLineItems()).hasSize(1)
);
// All assertions run even if early ones fail
```
