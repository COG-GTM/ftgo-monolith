package com.ftgo.ORDER_SERVICE_PACKAGE.integration;

import com.ftgo.ORDER_SERVICE_PACKAGE.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * INTEGRATION TEST TEMPLATE (Testcontainers)
 *
 * Copy this template when creating a new integration test.
 * Extends AbstractIntegrationTest which provides MySQL and Kafka containers.
 *
 * Conventions:
 *   - File location: src/test/java/<package>/integration/
 *   - Naming:        <Feature>IntegrationTest.java
 *   - MUST have @Tag("integration")
 *   - MUST extend AbstractIntegrationTest
 *
 * Replace:
 *   - ORDER_SERVICE_PACKAGE -> your service package (e.g., order)
 *
 * Prerequisites:
 *   - Docker must be running
 *   - AbstractIntegrationTest must be configured (see services/order-service example)
 *   - application-integration-test.yml must exist in src/test/resources/
 *
 * Run with: ./gradlew :services:<service>:integrationTest
 */
@Tag("integration")
@DisplayName("Order API Integration")
class OrderApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Nested
    @DisplayName("Health Check")
    class HealthCheck {

        @Test
        @DisplayName("should return healthy status")
        void shouldReturnHealthy() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "/actuator/health", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Create Order Flow")
    class CreateOrderFlow {

        @Test
        @DisplayName("should create order and persist to database")
        void shouldCreateAndPersist() {
            // Given
            String requestBody = """
                    {
                        "consumerId": 1,
                        "restaurantId": 2,
                        "lineItems": [
                            {"menuItemId": "item-1", "quantity": 2}
                        ]
                    }
                    """;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/orders",
                    new HttpEntity<>(requestBody, headers),
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(orderRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("should reject invalid order request")
        void shouldRejectInvalid() {
            // Given
            String invalidBody = "{}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/orders",
                    new HttpEntity<>(invalidBody, headers),
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(orderRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Database Integration")
    class DatabaseIntegration {

        @Test
        @DisplayName("should read and write entities through JPA")
        void shouldReadAndWrite() {
            // Given
            Order order = new Order();
            order.setConsumerId(1L);
            order.setRestaurantId(2L);

            // When
            Order saved = orderRepository.save(order);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(orderRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("MySQL container should be running")
        void mysqlShouldBeRunning() {
            assertThat(MYSQL.isRunning()).isTrue();
        }

        @Test
        @DisplayName("Kafka container should be running")
        void kafkaShouldBeRunning() {
            assertThat(KAFKA.isRunning()).isTrue();
        }
    }
}
