package com.ftgo.ORDER_SERVICE_PACKAGE.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * SERVICE LAYER UNIT TEST TEMPLATE
 *
 * Copy this template when creating a new service-layer unit test.
 *
 * Conventions:
 *   - File location: src/test/java/<package>/service/
 *   - Naming:        <Service>Test.java
 *   - No @Tag needed (unit tests are the default suite)
 *
 * Replace:
 *   - ORDER_SERVICE_PACKAGE -> your service package (e.g., order)
 *   - OrderService          -> your service class
 *   - OrderRepository       -> your repository dependency
 *   - Order                 -> your domain entity
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        @DisplayName("should create order when input is valid")
        void shouldCreateOrderWhenValid() {
            // Given
            CreateOrderRequest request = new CreateOrderRequest(1L, 2L);
            Order expectedOrder = new Order(1L, 2L);
            given(orderRepository.save(any(Order.class))).willReturn(expectedOrder);

            // When
            Order result = orderService.createOrder(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getConsumerId()).isEqualTo(1L);
            then(orderRepository).should().save(any(Order.class));
        }

        @Test
        @DisplayName("should throw exception when consumer ID is null")
        void shouldThrowWhenConsumerIdNull() {
            // Given
            CreateOrderRequest request = new CreateOrderRequest(null, 2L);

            // When / Then
            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Consumer ID");

            then(orderRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("findOrder")
    class FindOrder {

        @Test
        @DisplayName("should return order when it exists")
        void shouldReturnOrderWhenExists() {
            // Given
            Long orderId = 1L;
            Order order = new Order(1L, 2L);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // When
            Optional<Order> result = orderService.findOrder(orderId);

            // Then
            assertThat(result).isPresent().contains(order);
        }

        @Test
        @DisplayName("should return empty when order does not exist")
        void shouldReturnEmptyWhenNotExists() {
            // Given
            given(orderRepository.findById(99L)).willReturn(Optional.empty());

            // When
            Optional<Order> result = orderService.findOrder(99L);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
