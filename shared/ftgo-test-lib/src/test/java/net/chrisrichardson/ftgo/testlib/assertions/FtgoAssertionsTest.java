package net.chrisrichardson.ftgo.testlib.assertions;

import net.chrisrichardson.ftgo.testlib.builders.OrderBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link FtgoAssertions}.
 */
@DisplayName("FtgoAssertions")
class FtgoAssertionsTest {

    @Test
    @DisplayName("should pass when order has expected state")
    void shouldPassWhenOrderHasExpectedState() {
        Map<String, Object> order = OrderBuilder.anOrder().build();

        FtgoAssertions.assertOrder(order)
                .hasState("APPROVAL_PENDING")
                .hasConsumerId(100L)
                .hasRestaurantId(200L)
                .hasOrderId(1L);
    }

    @Test
    @DisplayName("should fail when order has unexpected state")
    void shouldFailWhenOrderHasUnexpectedState() {
        Map<String, Object> order = OrderBuilder.anOrder().build();

        assertThrows(AssertionError.class, () ->
                FtgoAssertions.assertOrder(order).hasState("APPROVED"));
    }

    @Test
    @DisplayName("should validate address")
    void shouldValidateAddress() {
        FtgoAssertions.assertValidAddress("123 Main St, Springfield, IL 62701");
    }

    @Test
    @DisplayName("should fail for blank address")
    void shouldFailForBlankAddress() {
        assertThrows(AssertionError.class, () ->
                FtgoAssertions.assertValidAddress(""));
    }
}
