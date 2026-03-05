/**
 * Test data builders for FTGO bounded contexts.
 *
 * <p>Each builder follows the Builder pattern with sensible defaults and provides
 * both preset factory methods (e.g., {@code anApprovedOrder()}) and fine-grained
 * customization via fluent setters.
 *
 * <p>Builders produce {@code Map<String, Object>} representations that can be:
 * <ul>
 *   <li>Serialized to JSON for REST API tests</li>
 *   <li>Used to construct domain entities in unit tests</li>
 *   <li>Used as expected values in assertion comparisons</li>
 * </ul>
 *
 * @see net.chrisrichardson.ftgo.testlib.builders.OrderBuilder
 * @see net.chrisrichardson.ftgo.testlib.builders.ConsumerBuilder
 * @see net.chrisrichardson.ftgo.testlib.builders.RestaurantBuilder
 * @see net.chrisrichardson.ftgo.testlib.builders.CourierBuilder
 */
package net.chrisrichardson.ftgo.testlib.builders;
