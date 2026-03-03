package net.chrisrichardson.ftgo.testutils.templates;

/**
 * Template and guidelines for writing JUnit 5 unit tests in FTGO microservices.
 *
 * <h2>Unit Test Template (JUnit 5 + Mockito)</h2>
 *
 * <p>Unit tests verify a single class in isolation by mocking all dependencies.
 * They should be fast, deterministic, and have no external dependencies.
 *
 * <h3>Template:</h3>
 * <pre>{@code
 * package net.chrisrichardson.ftgo.orderservice.domain;
 *
 * import org.junit.jupiter.api.BeforeEach;
 * import org.junit.jupiter.api.DisplayName;
 * import org.junit.jupiter.api.Nested;
 * import org.junit.jupiter.api.Test;
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import org.mockito.InjectMocks;
 * import org.mockito.Mock;
 * import org.mockito.junit.jupiter.MockitoExtension;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 * import static org.assertj.core.api.Assertions.assertThatThrownBy;
 * import static org.mockito.ArgumentMatchers.any;
 * import static org.mockito.Mockito.*;
 *
 * // Use @ExtendWith instead of JUnit 4's @RunWith
 * @ExtendWith(MockitoExtension.class)
 * @DisplayName("OrderService Unit Tests")
 * class OrderServiceTest {
 *
 *     // Use @Mock instead of Mockito.mock()
 *     @Mock
 *     private OrderRepository orderRepository;
 *
 *     @Mock
 *     private ConsumerService consumerService;
 *
 *     // Auto-injects mocks into the service
 *     @InjectMocks
 *     private OrderService orderService;
 *
 *     // Shared test data - use builders from ftgo-test-utils
 *     private Order testOrder;
 *     private Consumer testConsumer;
 *
 *     @BeforeEach
 *     void setUp() {
 *         testOrder = OrderBuilder.anOrder().withOrderId(1L).build();
 *         testConsumer = ConsumerBuilder.aConsumer().build();
 *     }
 *
 *     // Group related tests with @Nested
 *     @Nested
 *     @DisplayName("createOrder")
 *     class CreateOrder {
 *
 *         @Test
 *         @DisplayName("should create order when consumer is valid")
 *         void shouldCreateOrderWhenConsumerIsValid() {
 *             // Arrange
 *             when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
 *
 *             // Act
 *             Order result = orderService.createOrder(/* params * /);
 *
 *             // Assert
 *             assertThat(result).isNotNull();
 *             assertThat(result.getOrderState()).isEqualTo(OrderState.APPROVED);
 *             verify(orderRepository).save(any(Order.class));
 *         }
 *
 *         @Test
 *         @DisplayName("should throw when consumer not found")
 *         void shouldThrowWhenConsumerNotFound() {
 *             // Arrange
 *             when(consumerService.findById(anyLong())).thenReturn(Optional.empty());
 *
 *             // Act & Assert
 *             assertThatThrownBy(() -> orderService.createOrder(/* params * /))
 *                 .isInstanceOf(ConsumerNotFoundException.class);
 *         }
 *     }
 *
 *     @Nested
 *     @DisplayName("cancelOrder")
 *     class CancelOrder {
 *
 *         @Test
 *         @DisplayName("should cancel approved order")
 *         void shouldCancelApprovedOrder() {
 *             // test implementation
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>Key Conventions:</h3>
 * <ul>
 *   <li>Use {@code @ExtendWith(MockitoExtension.class)} instead of JUnit 4's {@code @RunWith}</li>
 *   <li>Use {@code @BeforeEach} instead of {@code @Before}</li>
 *   <li>Use {@code @DisplayName} for human-readable test names</li>
 *   <li>Use {@code @Nested} to group related tests</li>
 *   <li>Follow Arrange-Act-Assert pattern</li>
 *   <li>Use test data builders from {@code ftgo-test-utils}</li>
 *   <li>Use AssertJ for fluent assertions</li>
 *   <li>Test method names: {@code shouldDoSomethingWhenCondition()}</li>
 * </ul>
 *
 * @see net.chrisrichardson.ftgo.testutils.builders.OrderBuilder
 * @see net.chrisrichardson.ftgo.testutils.assertions.OrderAssertions
 */
public final class UnitTestTemplate {
    private UnitTestTemplate() {
        // Documentation-only class
    }
}
