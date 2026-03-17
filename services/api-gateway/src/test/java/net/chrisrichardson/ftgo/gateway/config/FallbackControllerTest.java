package net.chrisrichardson.ftgo.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FallbackControllerTest {

    private FallbackController controller;

    @BeforeEach
    void setUp() {
        controller = new FallbackController();
    }

    @Test
    void shouldReturnOrdersFallback() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/orders")
                .header("X-Correlation-ID", "test-correlation-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.ordersFallback(exchange).block();

        assertNotNull(response);
        assertEquals(503, response.get("status"));
        assertEquals("order-service", response.get("service"));
        assertEquals("test-correlation-id", response.get("correlationId"));
    }

    @Test
    void shouldReturnConsumersFallback() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/consumers")
                .header("X-Correlation-ID", "test-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.consumersFallback(exchange).block();

        assertNotNull(response);
        assertEquals("consumer-service", response.get("service"));
    }

    @Test
    void shouldReturnRestaurantsFallback() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/restaurants").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.restaurantsFallback(exchange).block();

        assertNotNull(response);
        assertEquals("restaurant-service", response.get("service"));
    }

    @Test
    void shouldReturnCouriersFallback() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/couriers").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.couriersFallback(exchange).block();

        assertNotNull(response);
        assertEquals("courier-service", response.get("service"));
    }

    @Test
    void shouldHandleMissingCorrelationId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.ordersFallback(exchange).block();

        assertNotNull(response);
        assertEquals("unknown", response.get("correlationId"));
    }
}
