# FTGO Platform - JUnit 4 to JUnit 5 Migration Guide

> Version: 1.0
> Status: Approved
> Last Updated: 2024-03-01
> Applies To: All FTGO modules migrating from JUnit 4 to JUnit 5

## Overview

This guide provides step-by-step instructions for migrating existing JUnit 4 tests to JUnit 5 (Jupiter). New microservices should use JUnit 5 from the start (via the `ftgo.testing-conventions` plugin). This guide is for migrating legacy monolith module tests.

## Backward Compatibility

Legacy JUnit 4 tests continue to work without modification thanks to the **JUnit Vintage Engine**, configured in the root `build.gradle`:

```groovy
dependencies {
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.junit.vintage:junit-vintage-engine:5.10.2'
}
```

This means migration can be done **incrementally** — one test class at a time.

## Step-by-Step Migration

### Step 1: Update Imports

| JUnit 4 Import | JUnit 5 Import |
|----------------|----------------|
| `org.junit.Test` | `org.junit.jupiter.api.Test` |
| `org.junit.Before` | `org.junit.jupiter.api.BeforeEach` |
| `org.junit.After` | `org.junit.jupiter.api.AfterEach` |
| `org.junit.BeforeClass` | `org.junit.jupiter.api.BeforeAll` |
| `org.junit.AfterClass` | `org.junit.jupiter.api.AfterAll` |
| `org.junit.Ignore` | `org.junit.jupiter.api.Disabled` |
| `org.junit.Assert.*` | `org.assertj.core.api.Assertions.*` (preferred) |
| `org.junit.runner.RunWith` | `org.junit.jupiter.api.extension.ExtendWith` |
| `org.junit.Rule` | (removed — use extensions) |

### Step 2: Update Annotations

```java
// BEFORE (JUnit 4)
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    @Before
    public void setUp() { /* ... */ }

    @After
    public void tearDown() { /* ... */ }

    @Ignore("not implemented yet")
    @Test
    public void shouldDoSomething() { /* ... */ }
}
```

```java
// AFTER (JUnit 5)
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @BeforeEach
    void setUp() { /* ... */ }

    @AfterEach
    void tearDown() { /* ... */ }

    @Disabled("not implemented yet")
    @Test
    @DisplayName("should do something")
    void shouldDoSomething() { /* ... */ }
}
```

**Key changes:**
- Test classes and methods no longer need to be `public`
- `@RunWith` → `@ExtendWith`
- `@Before` → `@BeforeEach`
- `@After` → `@AfterEach`
- `@Ignore` → `@Disabled` (reason is required)
- Add `@DisplayName` for human-readable test names

### Step 3: Update Assertions

We recommend migrating from JUnit assertions to **AssertJ** for richer, more readable assertions:

```java
// BEFORE (JUnit 4 Assert)
import static org.junit.Assert.*;

assertEquals("expected", actual);
assertTrue(condition);
assertFalse(condition);
assertNull(value);
assertNotNull(value);
assertSame(expected, actual);
```

```java
// AFTER (AssertJ — preferred)
import static org.assertj.core.api.Assertions.*;

assertThat(actual).isEqualTo("expected");
assertThat(condition).isTrue();
assertThat(condition).isFalse();
assertThat(value).isNull();
assertThat(value).isNotNull();
assertThat(actual).isSameAs(expected);

// AssertJ advantages — richer assertions:
assertThat(list).hasSize(3).contains("a", "b").doesNotContain("x");
assertThat(string).startsWith("Hello").endsWith("World").contains("lovely");
assertThat(number).isBetween(1, 10);
assertThat(optional).isPresent().hasValue("expected");
```

### Step 4: Update Exception Testing

```java
// BEFORE (JUnit 4 — expected attribute)
@Test(expected = IllegalArgumentException.class)
public void shouldThrowForInvalidInput() {
    service.process(null);
}
```

```java
// AFTER (JUnit 5 — assertThrows)
@Test
void shouldThrowForInvalidInput() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.process(null)
    );
    assertEquals("Input must not be null", exception.getMessage());
}
```

```java
// AFTER (AssertJ — preferred, more fluent)
@Test
void shouldThrowForInvalidInput() {
    assertThatThrownBy(() -> service.process(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Input must not be null")
        .hasNoCause();
}
```

### Step 5: Update Timeout Testing

```java
// BEFORE (JUnit 4)
@Test(timeout = 1000)
public void shouldCompleteWithinOneSecond() {
    service.longRunningOperation();
}
```

```java
// AFTER (JUnit 5)
@Test
@Timeout(value = 1, unit = TimeUnit.SECONDS)
void shouldCompleteWithinOneSecond() {
    service.longRunningOperation();
}
```

### Step 6: Replace Rules with Extensions

```java
// BEFORE (JUnit 4 — @Rule)
@Rule
public ExpectedException thrown = ExpectedException.none();

@Rule
public TemporaryFolder tempFolder = new TemporaryFolder();

@Test
public void shouldThrow() {
    thrown.expect(IOException.class);
    thrown.expectMessage("File not found");
    service.readFile("missing.txt");
}
```

```java
// AFTER (JUnit 5 — @TempDir extension + assertThatThrownBy)
@TempDir
Path tempDir;

@Test
void shouldThrow() {
    assertThatThrownBy(() -> service.readFile("missing.txt"))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("File not found");
}
```

### Step 7: Add Nested Test Classes

JUnit 5's `@Nested` allows grouping related tests for better organization:

```java
// BEFORE (JUnit 4 — flat structure)
public class OrderTest {
    @Test public void shouldCreateOrder() { /* ... */ }
    @Test public void shouldCalculateTotal() { /* ... */ }
    @Test public void shouldCancelOrder() { /* ... */ }
    @Test public void shouldNotCancelDeliveredOrder() { /* ... */ }
}
```

```java
// AFTER (JUnit 5 — nested structure)
@DisplayName("Order")
class OrderTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {
        @Test @DisplayName("should be in PENDING state")
        void shouldBeInPendingState() { /* ... */ }

        @Test @DisplayName("should calculate total")
        void shouldCalculateTotal() { /* ... */ }
    }

    @Nested
    @DisplayName("when cancelling")
    class WhenCancelling {
        @Test @DisplayName("should cancel approved order")
        void shouldCancelApprovedOrder() { /* ... */ }

        @Test @DisplayName("should reject cancelling delivered order")
        void shouldRejectCancellingDeliveredOrder() { /* ... */ }
    }
}
```

### Step 8: Use Parameterized Tests

JUnit 5 has much richer parameterized test support than JUnit 4:

```java
// BEFORE (JUnit 4 — cumbersome Parameterized runner)
@RunWith(Parameterized.class)
public class MoneyTest {
    @Parameter public int amount;
    @Parameter(1) public String expected;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {10, "10"}, {0, "0"}, {100, "100"}
        });
    }

    @Test
    public void shouldConvertToString() {
        assertEquals(expected, new Money(amount).asString());
    }
}
```

```java
// AFTER (JUnit 5 — inline parameterized tests)
class MoneyTest {

    @ParameterizedTest(name = "Money({0}) should display as \"{1}\"")
    @CsvSource({
        "10, 10",
        "0, 0",
        "100, 100"
    })
    void shouldConvertToString(int amount, String expected) {
        assertThat(new Money(amount).asString()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
    void shouldRejectNegativeAmounts(int negativeAmount) {
        assertThatThrownBy(() -> new Money(negativeAmount))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("additionTestCases")
    void shouldAddMoneyValues(Money a, Money b, Money expected) {
        assertThat(a.add(b)).isEqualTo(expected);
    }

    static Stream<Arguments> additionTestCases() {
        return Stream.of(
            Arguments.of(new Money(10), new Money(5), new Money(15)),
            Arguments.of(new Money(0), new Money(0), new Money(0)),
            Arguments.of(new Money(100), new Money(200), new Money(300))
        );
    }
}
```

---

## Concrete Migration Examples

### Example 1: MoneyTest (from ftgo-common)

**Before (JUnit 4):**
```java
package net.chrisrichardson.ftgo.common;

import org.junit.Test;
import static org.junit.Assert.*;

public class MoneyTest {
    private final int M1_AMOUNT = 10;
    private final int M2_AMOUNT = 15;
    private Money m1 = new Money(M1_AMOUNT);
    private Money m2 = new Money(M2_AMOUNT);

    @Test
    public void shouldReturnAsString() {
        assertEquals(Integer.toString(M1_AMOUNT), new Money(M1_AMOUNT).asString());
    }

    @Test
    public void shouldCompare() {
        assertTrue(m2.isGreaterThanOrEqual(m2));
        assertTrue(m2.isGreaterThanOrEqual(m1));
        assertFalse(m1.isGreaterThanOrEqual(m2));
    }

    @Test
    public void shouldAdd() {
        assertEquals(new Money(M1_AMOUNT + M2_AMOUNT), m1.add(m2));
    }

    @Test
    public void shouldMultiply() {
        int multiplier = 12;
        assertEquals(new Money(M2_AMOUNT * multiplier), m2.multiply(multiplier));
    }
}
```

**After (JUnit 5 + AssertJ):**
```java
package com.ftgo.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Money")
class MoneyTest {

    private static final int M1_AMOUNT = 10;
    private static final int M2_AMOUNT = 15;
    private final Money m1 = new Money(M1_AMOUNT);
    private final Money m2 = new Money(M2_AMOUNT);

    @Test
    @DisplayName("should return amount as string")
    void shouldReturnAsString() {
        assertThat(new Money(M1_AMOUNT).asString()).isEqualTo(Integer.toString(M1_AMOUNT));
    }

    @Nested
    @DisplayName("comparison")
    class Comparison {
        @Test
        @DisplayName("should be >= to equal amount")
        void shouldBeGreaterThanOrEqualToSelf() {
            assertThat(m2.isGreaterThanOrEqual(m2)).isTrue();
        }

        @Test
        @DisplayName("should be >= to smaller amount")
        void shouldBeGreaterThanOrEqualToSmaller() {
            assertThat(m2.isGreaterThanOrEqual(m1)).isTrue();
        }

        @Test
        @DisplayName("should not be >= to larger amount")
        void shouldNotBeGreaterThanOrEqualToLarger() {
            assertThat(m1.isGreaterThanOrEqual(m2)).isFalse();
        }
    }

    @Nested
    @DisplayName("arithmetic")
    class Arithmetic {
        @Test
        @DisplayName("should add two Money values")
        void shouldAdd() {
            assertThat(m1.add(m2)).isEqualTo(new Money(M1_AMOUNT + M2_AMOUNT));
        }

        @ParameterizedTest(name = "Money({0}) * {1} = Money({2})")
        @CsvSource({"15, 12, 180", "10, 5, 50", "0, 100, 0"})
        @DisplayName("should multiply Money by integer")
        void shouldMultiply(int amount, int multiplier, int expected) {
            assertThat(new Money(amount).multiply(multiplier)).isEqualTo(new Money(expected));
        }
    }
}
```

### Example 2: OrderControllerTest (from ftgo-order-service)

**Before (JUnit 4 + Rest-Assured):**
```java
import org.junit.Before;
import org.junit.Test;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {
    private OrderService orderService;
    private OrderRepository orderRepository;
    private OrderController orderController;

    @Before
    public void setUp() {
        orderService = mock(OrderService.class);
        orderRepository = mock(OrderRepository.class);
        orderController = new OrderController(orderService, orderRepository);
    }

    @Test
    public void shouldFindOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));
        given().standaloneSetup(configureControllers(orderController)).
        when().get("/orders/1").
        then().statusCode(200).body("orderId", equalTo(...));
    }
}
```

**After (JUnit 5 + MockMvc):**
```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController")
class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private OrderRepository orderRepository;
    @InjectMocks private OrderController orderController;

    @Nested
    @DisplayName("GET /orders/{id}")
    class GetOrderById {
        @Test
        @DisplayName("should return order when found")
        void shouldReturnOrderWhenFound() {
            when(orderRepository.findById(1L))
                .thenReturn(Optional.of(OrderTestBuilder.anOrder().build()));
            // ... test with MockMvc or RestAssuredMockMvc
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());
            // ... test 404 response
        }
    }
}
```

---

## Migration Priority

Migrate tests in this order (by bounded context importance):

| Priority | Module | Test Count | Notes |
|----------|--------|------------|-------|
| 1 | `shared/ftgo-common` | 2 tests | Foundation library, migrate first |
| 2 | `shared/ftgo-domain` | 5 tests | Domain model tests |
| 3 | `shared/ftgo-common-jpa` | 4 tests | JPA configuration tests |
| 4 | `ftgo-order-service` | 2 tests | Primary business service |
| 5 | Other services | Varies | Migrate as services are extracted |

**Note:** Legacy tests in `net.chrisrichardson.ftgo.*` packages will be migrated to `com.ftgo.*` packages as part of the broader microservices migration.

---

## Verification

After migrating each test class:

1. Run the individual test: `./gradlew :module:test --tests "com.ftgo.*.MigratedTest"`
2. Run all module tests: `./gradlew :module:test`
3. Verify coverage hasn't dropped: `./gradlew :module:jacocoTestReport`
4. Check CI passes: Push and verify GitHub Actions status

---

## References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JUnit 5 Migration Guide](https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Mockito JUnit 5 Extension](https://javadoc.io/doc/org.mockito/mockito-junit-jupiter/latest/index.html)
- [FTGO Testing Strategy](testing-strategy.md)
- [FTGO Testing Pipeline](testing-pipeline.md)
