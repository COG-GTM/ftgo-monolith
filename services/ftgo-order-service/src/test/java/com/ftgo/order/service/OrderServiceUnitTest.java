package com.ftgo.order.service;

import net.chrisrichardson.ftgo.testlib.builders.OrderBuilder;
import net.chrisrichardson.ftgo.testlib.assertions.FtgoAssertions;
import net.chrisrichardson.ftgo.testlib.assertions.MoneyAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example unit test for the Order bounded context.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>JUnit 5 with {@code @ExtendWith(MockitoExtension.class)}</li>
 *   <li>Test data builders from ftgo-test-lib</li>
 *   <li>Custom assertions from ftgo-test-lib</li>
 *   <li>Nested test classes for grouping</li>
 *   <li>BDD-style given/when/then structure</li>
 * </ul>
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service - Unit Tests")
class OrderServiceUnitTest {

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("should create order with default values using builder")
        void shouldCreateOrderWithDefaults() {
            // Given
            Map<String, Object> order = OrderBuilder.anOrder().build();

            // Then
            FtgoAssertions.assertOrder(order)
                    .hasState("APPROVAL_PENDING")
                    .hasConsumerId(100L)
                    .hasRestaurantId(200L);
        }

        @Test
        @DisplayName("should create order with custom consumer and restaurant")
        void shouldCreateOrderWithCustomValues() {
            // Given
            Map<String, Object> order = OrderBuilder.anOrder()
                    .withConsumerId(42L)
                    .withRestaurantId(7L)
                    .withOrderTotal(new BigDecimal("55.98"))
                    .build();

            // Then
            FtgoAssertions.assertOrder(order)
                    .hasConsumerId(42L)
                    .hasRestaurantId(7L);
            MoneyAssertions.assertMoneyEquals(
                    (BigDecimal) order.get("orderTotal"),
                    new BigDecimal("55.98"));
        }
    }

    @Nested
    @DisplayName("Order State Transitions")
    class OrderStateTransitions {

        @Test
        @DisplayName("should represent an approved order")
        void shouldRepresentApprovedOrder() {
            // Given
            Map<String, Object> order = OrderBuilder.anApprovedOrder().build();

            // Then
            FtgoAssertions.assertOrder(order).hasState("APPROVED");
        }

        @Test
        @DisplayName("should represent a cancelled order")
        void shouldRepresentCancelledOrder() {
            // Given
            Map<String, Object> order = OrderBuilder.aCancelledOrder().build();

            // Then
            FtgoAssertions.assertOrder(order).hasState("CANCELLED");
        }
    }

    @Nested
    @DisplayName("Order Total Calculations")
    class OrderTotalCalculations {

        @Test
        @DisplayName("should have positive order total")
        void shouldHavePositiveOrderTotal() {
            Map<String, Object> order = OrderBuilder.anOrder()
                    .withOrderTotal(new BigDecimal("29.99"))
                    .build();

            MoneyAssertions.assertMoneyPositive((BigDecimal) order.get("orderTotal"));
        }

        @Test
        @DisplayName("should validate order total in expected range")
        void shouldValidateOrderTotalInRange() {
            Map<String, Object> order = OrderBuilder.anOrder()
                    .withOrderTotal(new BigDecimal("29.99"))
                    .build();

            MoneyAssertions.assertMoneyInRange(
                    (BigDecimal) order.get("orderTotal"),
                    new BigDecimal("1.00"),
                    new BigDecimal("1000.00"));
        }
    }

    @Nested
    @DisplayName("Order Line Items")
    class OrderLineItems {

        @Test
        @DisplayName("should have default line items")
        @SuppressWarnings("unchecked")
        void shouldHaveDefaultLineItems() {
            Map<String, Object> order = OrderBuilder.anOrder().build();

            FtgoAssertions.assertValidLineItems(
                    (java.util.List<Map<String, Object>>) order.get("lineItems"));
        }

        @Test
        @DisplayName("should allow adding additional line items")
        @SuppressWarnings("unchecked")
        void shouldAllowAddingLineItems() {
            Map<String, Object> order = OrderBuilder.anOrder()
                    .addLineItem("MI-005", "Garlic Naan", 2, new BigDecimal("3.99"))
                    .build();

            java.util.List<Map<String, Object>> items =
                    (java.util.List<Map<String, Object>>) order.get("lineItems");
            assertThat(items).hasSizeGreaterThan(1);
        }
    }
}
