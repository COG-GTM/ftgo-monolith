package net.chrisrichardson.ftgo.testlib.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for FTGO unit tests.
 *
 * <p>Provides standard configuration for unit tests:
 * <ul>
 *   <li>Mockito extension for {@code @Mock} and {@code @InjectMocks} support</li>
 *   <li>"unit" tag for test filtering (e.g., {@code ./gradlew test -Dinclude.tags=unit})</li>
 *   <li>No Spring context — unit tests should be fast and isolated</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * class OrderServiceTest extends AbstractUnitTest {
 *
 *     @Mock
 *     private OrderRepository orderRepository;
 *
 *     @InjectMocks
 *     private OrderService orderService;
 *
 *     @Test
 *     void shouldCreateOrder() {
 *         // arrange, act, assert
 *     }
 * }
 * }</pre>
 *
 * @see AbstractIntegrationTest
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
public abstract class AbstractUnitTest {
    // Common unit test utilities can be added here
}
