/**
 * Test data builders and fixtures for the template service.
 *
 * <p>This package contains reusable test data factories following the Builder pattern.
 * Each domain entity should have a corresponding {@code *TestBuilder} class that
 * provides sensible defaults and a fluent API for creating test instances.
 *
 * <h2>Conventions</h2>
 * <ul>
 *   <li>Builder class naming: {@code {Entity}TestBuilder}</li>
 *   <li>Factory method naming: {@code a{Entity}()} or {@code an{Entity}()}</li>
 *   <li>All fields have sensible defaults — tests only override what they care about</li>
 *   <li>Builders are immutable-style (return {@code this} for chaining)</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // In your test:
 * var order = OrderTestBuilder.anOrder()
 *     .withConsumerId(42L)
 *     .withRestaurant(RestaurantTestBuilder.aRestaurant().withName("Ajanta").build())
 *     .build();
 * }</pre>
 *
 * @see com.ftgo.template.testdata.ExampleEntityTestBuilder
 */
package com.ftgo.template.testdata;
