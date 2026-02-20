package com.ftgo.ORDER_SERVICE_PACKAGE.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CONSUMER-SIDE CONTRACT TEST TEMPLATE (Pact)
 *
 * This template demonstrates consumer-driven contract testing using Pact.
 * The consumer defines expected interactions, and the provider verifies them.
 *
 * Conventions:
 *   - File location: src/test/java/<package>/contract/
 *   - Naming:        <Provider>ConsumerContractTest.java
 *   - Pact files stored in: build/pacts/
 *
 * Replace:
 *   - ORDER_SERVICE_PACKAGE -> your service package
 *   - "OrderService"        -> consumer service name
 *   - "RestaurantService"   -> provider service name
 *
 * Dependencies required in build.gradle:
 *   testImplementation 'au.com.dius.pact.consumer:junit5:4.6.7'
 *
 * Workflow:
 *   1. Consumer writes this test defining expected interactions
 *   2. Running this test generates a Pact file in build/pacts/
 *   3. Pact file is shared with the provider (via broker or file)
 *   4. Provider runs verification against the Pact file
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "RestaurantService")
@DisplayName("Order Service -> Restaurant Service Contract")
class RestaurantServiceConsumerContractTest {

    @Pact(consumer = "OrderService", provider = "RestaurantService")
    V4Pact getRestaurantPact(PactDslWithProvider builder) {
        DslPart responseBody = new PactDslJsonBody()
                .integerType("id", 1L)
                .stringType("name", "Sample Restaurant")
                .object("address")
                    .stringType("street", "123 Main St")
                    .stringType("city", "Springfield")
                    .stringType("state", "IL")
                    .stringType("zip", "62704")
                .closeObject()
                .eachLike("menuItems")
                    .stringType("id", "item-1")
                    .stringType("name", "Pizza Margherita")
                    .decimalType("price", 12.99)
                .closeArray();

        return builder
                .given("restaurant with ID 1 exists")
                .uponReceiving("a request to get restaurant by ID")
                .path("/api/restaurants/1")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(java.util.Map.of("Content-Type", "application/json"))
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    @Pact(consumer = "OrderService", provider = "RestaurantService")
    V4Pact getRestaurantNotFoundPact(PactDslWithProvider builder) {
        DslPart errorBody = new PactDslJsonBody()
                .stringType("error", "Not Found")
                .stringType("message", "Restaurant not found")
                .integerType("status", 404);

        return builder
                .given("restaurant with ID 999 does not exist")
                .uponReceiving("a request to get non-existent restaurant")
                .path("/api/restaurants/999")
                .method("GET")
                .willRespondWith()
                .status(404)
                .headers(java.util.Map.of("Content-Type", "application/json"))
                .body(errorBody)
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getRestaurantPact")
    @DisplayName("should get restaurant details from provider")
    void shouldGetRestaurant(MockServer mockServer) {
        // Given
        RestTemplate restTemplate = new RestTemplate();
        String url = mockServer.getUrl() + "/api/restaurants/1";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Sample Restaurant");
        assertThat(response.getBody()).contains("menuItems");
    }

    @Test
    @PactTestFor(pactMethod = "getRestaurantNotFoundPact")
    @DisplayName("should handle restaurant not found from provider")
    void shouldHandleNotFound(MockServer mockServer) {
        // Given
        RestTemplate restTemplate = new RestTemplate();
        String url = mockServer.getUrl() + "/api/restaurants/999";

        // When
        ResponseEntity<String> response;
        try {
            response = restTemplate.getForEntity(url, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            return;
        }

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
