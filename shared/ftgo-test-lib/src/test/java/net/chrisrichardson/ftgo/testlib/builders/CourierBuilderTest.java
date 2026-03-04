package net.chrisrichardson.ftgo.testlib.builders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CourierBuilder}.
 */
@DisplayName("CourierBuilder")
class CourierBuilderTest {

    @Test
    @DisplayName("should create courier with defaults")
    void shouldCreateCourierWithDefaults() {
        Map<String, Object> courier = CourierBuilder.aCourier().build();

        assertThat(courier).containsKeys("courierId", "firstName", "lastName",
                "phoneNumber", "available", "currentLocation", "createdAt");
        assertThat(courier.get("available")).isEqualTo(true);
    }

    @Test
    @DisplayName("should create busy courier preset")
    void shouldCreateBusyCourierPreset() {
        Map<String, Object> courier = CourierBuilder.aBusyCourier().build();

        assertThat(courier.get("available")).isEqualTo(false);
    }
}
