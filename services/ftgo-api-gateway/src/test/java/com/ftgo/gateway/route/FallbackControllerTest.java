package com.ftgo.gateway.route;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FallbackController}.
 */
class FallbackControllerTest {

    private final FallbackController controller = new FallbackController();

    @Test
    void shouldReturnServiceUnavailableForOrdersFallback() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/fallback/orders")
                .header("X-Correlation-ID", "test-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.ordersFallback(exchange).block();

        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("Service Unavailable");
        assertThat(response.get("status")).isEqualTo(503);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldReturnServiceUnavailableForConsumersFallback() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/fallback/consumers")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.consumersFallback(exchange).block();

        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("Service Unavailable");
    }

    @Test
    void shouldReturnServiceUnavailableForRestaurantsFallback() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/fallback/restaurants")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.restaurantsFallback(exchange).block();

        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("Service Unavailable");
    }

    @Test
    void shouldReturnServiceUnavailableForCouriersFallback() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/fallback/couriers")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Map<String, Object> response = controller.couriersFallback(exchange).block();

        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("Service Unavailable");
    }
}
