package com.ftgo.ORDER_SERVICE_PACKAGE.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CONTROLLER LAYER UNIT TEST TEMPLATE
 *
 * Copy this template when creating a new controller-layer unit test.
 * Uses @WebMvcTest which configures MockMvc and scans only web-layer beans.
 *
 * Conventions:
 *   - File location: src/test/java/<package>/web/
 *   - Naming:        <Controller>Test.java
 *   - No @Tag needed (unit tests are the default suite)
 *
 * Replace:
 *   - ORDER_SERVICE_PACKAGE -> your service package (e.g., order)
 *   - OrderController       -> your controller class
 *   - OrderService          -> your service dependency
 *   - Order, CreateOrderRequest, OrderResponse -> your DTOs
 */
@WebMvcTest(OrderController.class)
@DisplayName("OrderController")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrder {

        @Test
        @DisplayName("should return 201 when order is created")
        void shouldReturn201WhenCreated() throws Exception {
            // Given
            CreateOrderRequest request = new CreateOrderRequest(1L, 2L);
            OrderResponse response = new OrderResponse(100L, "PENDING");
            given(orderService.createOrder(any())).willReturn(response);

            // When / Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderId").value(100L))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void shouldReturn400WhenInvalid() throws Exception {
            // Given
            String invalidBody = "{}";

            // When / Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrder {

        @Test
        @DisplayName("should return 200 with order when found")
        void shouldReturn200WhenFound() throws Exception {
            // Given
            OrderResponse response = new OrderResponse(1L, "APPROVED");
            given(orderService.findOrder(1L)).willReturn(Optional.of(response));

            // When / Then
            mockMvc.perform(get("/api/orders/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1L))
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            given(orderService.findOrder(99L)).willReturn(Optional.empty());

            // When / Then
            mockMvc.perform(get("/api/orders/{id}", 99L))
                    .andExpect(status().isNotFound());
        }
    }
}
