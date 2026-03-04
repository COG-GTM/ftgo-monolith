package net.chrisrichardson.ftgo.testlib.builders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test data builder for Courier entities.
 *
 * <p>Usage:
 * <pre>{@code
 * Map<String, Object> courier = CourierBuilder.aCourier()
 *     .withFirstName("Mike")
 *     .withAvailable(true)
 *     .build();
 * }</pre>
 *
 * @see OrderBuilder
 */
public final class CourierBuilder {

    private Long courierId = 300L;
    private String firstName = "Mike";
    private String lastName = "Johnson";
    private String phoneNumber = "+1-555-987-6543";
    private boolean available = true;
    private String currentLocation = "40.7128,-74.0060";
    private LocalDateTime createdAt = LocalDateTime.of(2026, 1, 8, 7, 0, 0);

    private CourierBuilder() {
    }

    /**
     * Creates a new CourierBuilder with sensible defaults.
     *
     * @return a new CourierBuilder instance
     */
    public static CourierBuilder aCourier() {
        return new CourierBuilder();
    }

    public CourierBuilder withCourierId(Long courierId) {
        this.courierId = courierId;
        return this;
    }

    public CourierBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CourierBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public CourierBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public CourierBuilder withAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public CourierBuilder withCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
        return this;
    }

    public CourierBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Creates a builder preset for an unavailable (busy) courier.
     *
     * @return a new CourierBuilder with available=false
     */
    public static CourierBuilder aBusyCourier() {
        return aCourier().withAvailable(false);
    }

    /**
     * Builds the courier as a Map representation.
     *
     * @return courier data as a Map
     */
    public Map<String, Object> build() {
        Map<String, Object> courier = new HashMap<>();
        courier.put("courierId", courierId);
        courier.put("firstName", firstName);
        courier.put("lastName", lastName);
        courier.put("phoneNumber", phoneNumber);
        courier.put("available", available);
        courier.put("currentLocation", currentLocation);
        courier.put("createdAt", createdAt);
        return courier;
    }

    // --- Getters ---

    public Long getCourierId() {
        return courierId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
