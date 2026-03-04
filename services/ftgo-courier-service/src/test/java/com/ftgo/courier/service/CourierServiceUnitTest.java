package com.ftgo.courier.service;

import net.chrisrichardson.ftgo.testlib.builders.CourierBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example unit test for the Courier bounded context.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>CourierBuilder usage for test data creation</li>
 *   <li>Availability state testing patterns</li>
 *   <li>Preset builder methods (aBusyCourier)</li>
 * </ul>
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Courier Service - Unit Tests")
class CourierServiceUnitTest {

    @Nested
    @DisplayName("Courier Registration")
    class CourierRegistration {

        @Test
        @DisplayName("should create courier with default values")
        void shouldCreateCourierWithDefaults() {
            Map<String, Object> courier = CourierBuilder.aCourier().build();

            assertThat(courier.get("firstName")).isEqualTo("Mike");
            assertThat(courier.get("lastName")).isEqualTo("Johnson");
            assertThat(courier.get("available")).isEqualTo(true);
        }

        @Test
        @DisplayName("should create courier with custom values")
        void shouldCreateCourierWithCustomValues() {
            Map<String, Object> courier = CourierBuilder.aCourier()
                    .withFirstName("Sarah")
                    .withLastName("Williams")
                    .withPhoneNumber("+1-555-111-2222")
                    .build();

            assertThat(courier.get("firstName")).isEqualTo("Sarah");
            assertThat(courier.get("lastName")).isEqualTo("Williams");
        }
    }

    @Nested
    @DisplayName("Courier Availability")
    class CourierAvailability {

        @Test
        @DisplayName("should create available courier by default")
        void shouldCreateAvailableCourierByDefault() {
            Map<String, Object> courier = CourierBuilder.aCourier().build();

            assertThat(courier.get("available")).isEqualTo(true);
        }

        @Test
        @DisplayName("should create busy courier using preset")
        void shouldCreateBusyCourierUsingPreset() {
            Map<String, Object> courier = CourierBuilder.aBusyCourier().build();

            assertThat(courier.get("available")).isEqualTo(false);
        }

        @Test
        @DisplayName("should toggle courier availability")
        void shouldToggleCourierAvailability() {
            Map<String, Object> courier = CourierBuilder.aCourier()
                    .withAvailable(false)
                    .build();

            assertThat(courier.get("available")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Courier Location")
    class CourierLocation {

        @Test
        @DisplayName("should have default location")
        void shouldHaveDefaultLocation() {
            Map<String, Object> courier = CourierBuilder.aCourier().build();

            assertThat(courier.get("currentLocation")).isNotNull();
            assertThat(courier.get("currentLocation").toString()).contains(",");
        }

        @Test
        @DisplayName("should allow custom location")
        void shouldAllowCustomLocation() {
            Map<String, Object> courier = CourierBuilder.aCourier()
                    .withCurrentLocation("41.8781,-87.6298")
                    .build();

            assertThat(courier.get("currentLocation")).isEqualTo("41.8781,-87.6298");
        }
    }
}
