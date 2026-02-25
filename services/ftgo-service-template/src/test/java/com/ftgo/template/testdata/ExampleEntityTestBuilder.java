package com.ftgo.template.testdata;

// =============================================================================
// TEMPLATE: Test Data Builder
// =============================================================================
// Copy this file when creating a new microservice.
// Replace "ExampleEntity" with your actual domain entity.
//
// This template demonstrates the Builder pattern for test data:
//   - Fluent API for readable test setup
//   - Default values for all fields (tests only override what they care about)
//   - Factory method naming: a{Entity}() or an{Entity}()
//   - Composable with other builders
//
// Why builders over Object Mothers?
//   - Builders make test intent clear (only override relevant fields)
//   - Builders are composable (combine multiple builders)
//   - Builders avoid the "huge static field" problem of Object Mothers
//
// The legacy monolith uses Object Mothers (RestaurantMother, OrderDetailsMother).
// New microservices should prefer builders for clarity and maintainability.
// =============================================================================

/**
 * Test data builder for ExampleEntity.
 *
 * <p>Usage:
 * <pre>{@code
 * // Create entity with all defaults
 * var entity = ExampleEntityTestBuilder.anEntity().build();
 *
 * // Override specific fields for your test scenario
 * var entity = ExampleEntityTestBuilder.anEntity()
 *     .withId(42L)
 *     .withName("Custom Name")
 *     .build();
 *
 * // Create entity in a specific state
 * var cancelled = ExampleEntityTestBuilder.anEntity()
 *     .withStatus(Status.CANCELLED)
 *     .build();
 * }</pre>
 *
 * <p><b>Location:</b> {@code src/test/java/com/ftgo/{service}/testdata/}
 */
public class ExampleEntityTestBuilder {

    // -------------------------------------------------------------------------
    // Default values — sensible defaults for all fields
    // -------------------------------------------------------------------------

    private Long id = null;  // null = not yet persisted
    private String name = "Default Entity Name";
    private String description = "Default description for testing";
    // private Status status = Status.ACTIVE;
    // private Money price = new Money("10.00");
    // private Long consumerId = 1L;
    // private Restaurant restaurant = RestaurantTestBuilder.aRestaurant().build();
    // private List<OrderLineItem> lineItems = defaultLineItems();

    // -------------------------------------------------------------------------
    // Factory methods — entry point for the builder
    // -------------------------------------------------------------------------

    /**
     * Creates a new builder with default values.
     *
     * @return new builder instance
     */
    public static ExampleEntityTestBuilder anEntity() {
        return new ExampleEntityTestBuilder();
    }

    /**
     * Alias for {@link #anEntity()} — use whichever reads better in your test.
     */
    public static ExampleEntityTestBuilder withDefaults() {
        return new ExampleEntityTestBuilder();
    }

    // -------------------------------------------------------------------------
    // Builder methods — override specific fields
    // -------------------------------------------------------------------------

    public ExampleEntityTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ExampleEntityTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ExampleEntityTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    // public ExampleEntityTestBuilder withStatus(Status status) {
    //     this.status = status;
    //     return this;
    // }

    // public ExampleEntityTestBuilder withPrice(Money price) {
    //     this.price = price;
    //     return this;
    // }

    // public ExampleEntityTestBuilder withConsumerId(Long consumerId) {
    //     this.consumerId = consumerId;
    //     return this;
    // }

    // public ExampleEntityTestBuilder withRestaurant(Restaurant restaurant) {
    //     this.restaurant = restaurant;
    //     return this;
    // }

    // -------------------------------------------------------------------------
    // Convenience methods — common test scenarios
    // -------------------------------------------------------------------------

    /**
     * Creates a builder pre-configured for a "persisted" entity (with ID set).
     */
    public ExampleEntityTestBuilder persisted() {
        this.id = 1L;
        return this;
    }

    // /**
    //  * Creates a builder pre-configured for a cancelled entity.
    //  */
    // public ExampleEntityTestBuilder cancelled() {
    //     this.status = Status.CANCELLED;
    //     return this;
    // }

    // -------------------------------------------------------------------------
    // Build method — constructs the actual entity
    // -------------------------------------------------------------------------

    /**
     * Builds the entity using the configured values.
     *
     * @return new entity instance
     */
    // public ExampleEntity build() {
    //     ExampleEntity entity = new ExampleEntity(name, description);
    //     if (id != null) {
    //         entity.setId(id);
    //     }
    //     return entity;
    // }

    // -------------------------------------------------------------------------
    // Default collections — used when no override is provided
    // -------------------------------------------------------------------------

    // private static List<OrderLineItem> defaultLineItems() {
    //     return List.of(
    //         new OrderLineItem("item1", "Burger", new Money(10), 2),
    //         new OrderLineItem("item2", "Fries", new Money(5), 1)
    //     );
    // }
}
