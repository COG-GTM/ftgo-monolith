# Unit Test Plan — FTGO Monolith

## Current Coverage

| Module | Existing Tests | Classes Covered |
|--------|---------------|-----------------|
| `ftgo-common` | `MoneyTest`, `MoneySerializationTest` | `Money`, `MoneyModule` |
| `ftgo-domain` | `CourierAssignmentStrategyTest` | `DistanceOptimizedCourierAssignmentStrategy` |
| `ftgo-order-service` | `OrderControllerTest` | `OrderController` (GET only) |
| `ftgo-application` | `FtgoApplicationTest` | Integration / E2E (requires DB) |

**Untested classes: ~25+.** The sections below are ordered by priority (highest-value, lowest-risk tests first).

---

## Phase 1 — Domain Model (Pure Logic, No Mocks Needed)

> Module: `ftgo-domain` · Test framework: JUnit 4 (already in `build.gradle`)

### 1.1 `OrderTest`
**File:** `ftgo-domain/src/test/java/.../domain/OrderTest.java`

| # | Test Case | What It Verifies |
|---|-----------|-----------------|
| 1 | `shouldCreateOrderInApprovedState` | Constructor sets state = `APPROVED` |
| 2 | `shouldCalculateOrderTotal` | Delegates to `OrderLineItems.orderTotal()` |
| 3 | `shouldCancelApprovedOrder` | `cancel()` transitions APPROVED → CANCELLED |
| 4 | `shouldRejectCancelWhenNotApproved` | `cancel()` throws `UnsupportedStateTransitionException` for non-APPROVED |
| 5 | `shouldAcceptTicket` | `acceptTicket()` transitions APPROVED → ACCEPTED, stores readyBy |
| 6 | `shouldRejectAcceptTicketWhenNotApproved` | Throws for wrong state |
| 7 | `shouldTransitionPreparingOnlyFromAccepted` | `notePreparing()` happy + sad path |
| 8 | `shouldTransitionReadyOnlyFromPreparing` | `noteReadyForPickup()` happy + sad |
| 9 | `shouldTransitionPickedUpOnlyFromReady` | `notePickedUp()` happy + sad |
| 10 | `shouldTransitionDeliveredOnlyFromPickedUp` | `noteDelivered()` happy + sad |
| 11 | `shouldScheduleCourier` | `schedule()` sets `assignedCourier` |
| 12 | `shouldFollowFullHappyPath` | APPROVED → ACCEPTED → PREPARING → READY → PICKED_UP → DELIVERED |

### 1.2 `CourierTest`
**File:** `ftgo-domain/src/test/java/.../domain/CourierTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldStartUnavailable` |
| 2 | `shouldToggleAvailability` |
| 3 | `shouldInitLocationFromAddress` |
| 4 | `shouldUpdateLocation` |
| 5 | `shouldTrackActiveDeliveryCount` |
| 6 | `shouldHaveZeroDeliveriesWhenEmpty` |
| 7 | `shouldReturnActionsForDelivery` |
| 8 | `shouldCancelDelivery` |
| 9 | `hasLocationReturnsFalseWhenNotSet` |

### 1.3 `PlanTest`
**File:** `ftgo-domain/src/test/java/.../domain/PlanTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldAddAction` |
| 2 | `shouldRemoveDeliveryActions` |
| 3 | `shouldFilterActionsForDelivery` |
| 4 | `shouldHandleEmptyPlan` |

### 1.4 `RestaurantTest`
**File:** `ftgo-domain/src/test/java/.../domain/RestaurantTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldFindMenuItemById` |
| 2 | `shouldReturnEmptyForUnknownMenuItem` |
| 3 | `shouldThrowOnReviseMenu` (documents current behavior) |

---

## Phase 2 — Common Module (Value Objects, Utilities)

> Module: `ftgo-common` · Test framework: JUnit 4 + Spring Test (already available)

### 2.1 `AddressTest`
**File:** `ftgo-common/src/test/java/.../common/AddressTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldCreateWithoutLatLng` |
| 2 | `shouldCreateWithLatLng` |
| 3 | `shouldAllowSettingFields` |

### 2.2 `ErrorResponseTest`
**File:** `ftgo-common/src/test/java/.../common/ErrorResponseTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldPopulateAllFields` |
| 2 | `shouldSetTimestampAutomatically` |

### 2.3 `PersonNameTest`
**File:** `ftgo-common/src/test/java/.../common/PersonNameTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldStoreFirstAndLastName` |
| 2 | `shouldSerializeAndDeserialize` (Jackson round-trip) |

---

## Phase 3 — Service Layer (Requires Mockito)

> Modules: `ftgo-order-service`, `ftgo-consumer-service`, `ftgo-courier-service`  
> Add `testCompile "org.mockito:mockito-core:2.23.4"` where not already present via spring-boot-starter-test.

### 3.1 `OrderServiceTest`
**File:** `ftgo-order-service/src/test/java/.../domain/OrderServiceTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldCreateOrder` — mocks repos, verifies save + consumer validation |
| 2 | `shouldThrowWhenRestaurantNotFound` |
| 3 | `shouldCancelOrder` |
| 4 | `shouldThrowWhenOrderNotFoundOnCancel` |
| 5 | `shouldAcceptAndScheduleDelivery` — verifies courier assignment flow |
| 6 | `shouldTransitionThroughPreparing` |
| 7 | `shouldTransitionThroughReadyForPickup` |
| 8 | `shouldTransitionThroughPickedUp` |
| 9 | `shouldTransitionThroughDelivered` |
| 10 | `shouldEstimateDeliveryTimeWithLocation` |
| 11 | `shouldFallbackDeliveryTimeWithoutLocation` |

### 3.2 `ConsumerServiceTest`
**File:** `ftgo-consumer-service/src/test/java/.../domain/ConsumerServiceTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldCreateConsumer` |
| 2 | `shouldFindConsumerById` |
| 3 | `shouldReturnEmptyWhenNotFound` |
| 4 | `shouldValidateOrderForConsumer` |
| 5 | `shouldThrowWhenConsumerNotFoundOnValidate` |

### 3.3 `CourierServiceTest`
**File:** `ftgo-courier-service/src/test/java/.../domain/CourierServiceTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldCreateCourier` |
| 2 | `shouldUpdateAvailabilityToAvailable` |
| 3 | `shouldUpdateAvailabilityToUnavailable` |
| 4 | `shouldUpdateLocation` |
| 5 | `shouldThrowWhenCourierNotFoundOnLocationUpdate` |

---

## Phase 4 — Controller Layer (MockMvc)

> Expand existing `OrderControllerTest`, add new controller tests.

### 4.1 `OrderControllerTest` (expand)
**File:** `ftgo-order-service/src/test/java/.../web/OrderControllerTest.java`

| # | Test Case |
|---|-----------|
| 1 | ✅ `shouldFindOrder` (exists) |
| 2 | ✅ `shouldFindNotOrder` (exists) |
| 3 | `shouldCreateOrder` — POST /orders |
| 4 | `shouldCancelOrder` — POST /orders/{id}/cancel |
| 5 | `shouldAcceptOrder` — POST /orders/{id}/accept |
| 6 | `shouldReturnOrdersForConsumer` — GET /orders?consumerId= |

### 4.2 `ConsumerControllerTest`
**File:** `ftgo-consumer-service/src/test/java/.../web/ConsumerControllerTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldCreateConsumer` |
| 2 | `shouldGetConsumer` |
| 3 | `shouldReturn404WhenConsumerNotFound` |

### 4.3 `CourierControllerTest`
**File:** `ftgo-courier-service/src/test/java/.../web/CourierControllerTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldCreateCourier` |
| 2 | `shouldGetCourier` |
| 3 | `shouldUpdateAvailability` |
| 4 | `shouldUpdateLocation` |
| 5 | `shouldGetWorkload` |

### 4.4 `RestaurantControllerTest`
**File:** `ftgo-restaurant-service/src/test/java/.../web/RestaurantControllerTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldCreateRestaurant` |
| 2 | `shouldGetRestaurant` |
| 3 | `shouldReturn404WhenRestaurantNotFound` |

---

## Phase 5 — API Tracking & Cross-cutting

### 5.1 `ApiTrackingInterceptorTest`
**File:** `ftgo-common/src/test/java/.../tracking/ApiTrackingInterceptorTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldGenerateCorrelationIdWhenMissing` |
| 2 | `shouldPropagateExistingCorrelationId` |
| 3 | `shouldSetCorrelationIdInResponseHeader` |
| 4 | `shouldPersistLogAfterCompletion` |
| 5 | `shouldRecordErrorMessageOnException` |
| 6 | `shouldHandleSaveFailureGracefully` |

### 5.2 `ApiRequestLogTest`
**File:** `ftgo-common/src/test/java/.../tracking/ApiRequestLogTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldCreateWithRequiredFields` |
| 2 | `shouldCompleteWithStatus` |
| 3 | `shouldCompleteWithError` |

### 5.3 `GlobalExceptionHandlerTest`
**File:** `ftgo-application/src/test/java/.../GlobalExceptionHandlerTest.java`

| # | Test Case |
|---|-----------|
| 1 | `shouldReturn404ForOrderNotFound` |
| 2 | `shouldReturn404ForRestaurantNotFound` |
| 3 | `shouldReturn404ForCourierNotFound` |
| 4 | `shouldReturn409ForUnsupportedStateTransition` |
| 5 | `shouldReturn503ForNoCourierAvailable` |
| 6 | `shouldReturn400ForIllegalArgument` |
| 7 | `shouldReturn500ForUnhandledException` |

---

## Execution Order & Dependencies

```
Phase 1  →  No extra deps, can start immediately
Phase 2  →  No extra deps
Phase 3  →  Mockito (already available via spring-boot-starter-test)
Phase 4  →  MockMvc + RestAssured (already in build.gradle)
Phase 5  →  Mock HttpServletRequest/Response (spring-test)
```

### Suggested build.gradle additions

**`ftgo-domain/build.gradle`** — add Mockito for Phase 1 helper tests:
```groovy
testCompile "org.mockito:mockito-core:2.23.4"
```

No other dependency changes needed — the other modules already pull in `spring-boot-starter-test` which includes JUnit 4, Mockito, Hamcrest, and Spring Test.

---

## Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :ftgo-domain:test
./gradlew :ftgo-order-service:test

# Single test class
./gradlew :ftgo-domain:test --tests "*.OrderTest"
```

---

## Total: ~75 new test cases across 15 test classes

| Phase | Test Classes | Test Cases | Effort |
|-------|-------------|------------|--------|
| 1 — Domain Model | 4 | ~28 | Small — pure logic |
| 2 — Common | 3 | ~7 | Small — value objects |
| 3 — Services | 3 | ~21 | Medium — needs mocks |
| 4 — Controllers | 4 | ~14 | Medium — MockMvc setup |
| 5 — Cross-cutting | 3 | ~16 | Medium — interceptor mocking |
| **Total** | **17** | **~86** | |
