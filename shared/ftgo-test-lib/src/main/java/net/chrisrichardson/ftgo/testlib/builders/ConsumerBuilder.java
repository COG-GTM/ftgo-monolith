package net.chrisrichardson.ftgo.testlib.builders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test data builder for Consumer entities.
 *
 * <p>Usage:
 * <pre>{@code
 * Map<String, Object> consumer = ConsumerBuilder.aConsumer()
 *     .withFirstName("Jane")
 *     .withLastName("Doe")
 *     .build();
 * }</pre>
 *
 * @see OrderBuilder
 */
public final class ConsumerBuilder {

    private Long consumerId = 100L;
    private String firstName = "John";
    private String lastName = "Doe";
    private String email = "john.doe@example.com";
    private String phoneNumber = "+1-555-123-4567";
    private String deliveryAddress = "123 Main St, Springfield, IL 62701";
    private LocalDateTime createdAt = LocalDateTime.of(2026, 1, 10, 9, 0, 0);

    private ConsumerBuilder() {
    }

    /**
     * Creates a new ConsumerBuilder with sensible defaults.
     *
     * @return a new ConsumerBuilder instance
     */
    public static ConsumerBuilder aConsumer() {
        return new ConsumerBuilder();
    }

    public ConsumerBuilder withConsumerId(Long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public ConsumerBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ConsumerBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ConsumerBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public ConsumerBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public ConsumerBuilder withDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
        return this;
    }

    public ConsumerBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Builds the consumer as a Map representation.
     *
     * @return consumer data as a Map
     */
    public Map<String, Object> build() {
        Map<String, Object> consumer = new HashMap<>();
        consumer.put("consumerId", consumerId);
        consumer.put("firstName", firstName);
        consumer.put("lastName", lastName);
        consumer.put("email", email);
        consumer.put("phoneNumber", phoneNumber);
        consumer.put("deliveryAddress", deliveryAddress);
        consumer.put("createdAt", createdAt);
        return consumer;
    }

    // --- Getters ---

    public Long getConsumerId() {
        return consumerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
