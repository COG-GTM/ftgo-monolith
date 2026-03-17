/**
 * Cross-service DTO/API contracts for the FTGO domain.
 *
 * <p>These DTOs define the data transfer contracts used for communication
 * between microservices, replacing direct entity sharing across bounded contexts.
 *
 * <h2>Bounded Context Contracts</h2>
 *
 * <h3>Consumer Context</h3>
 * <ul>
 *   <li>{@link net.chrisrichardson.ftgo.domain.dto.ConsumerDTO} - Consumer representation for cross-service use</li>
 * </ul>
 *
 * <h3>Restaurant Context</h3>
 * <ul>
 *   <li>{@link net.chrisrichardson.ftgo.domain.dto.RestaurantDTO} - Restaurant representation</li>
 *   <li>{@link net.chrisrichardson.ftgo.domain.dto.MenuItemDTO} - Menu item representation</li>
 *   <li>{@link net.chrisrichardson.ftgo.domain.dto.RestaurantMenuDTO} - Restaurant menu representation</li>
 * </ul>
 *
 * <h3>Order Context</h3>
 * <ul>
 *   <li>{@link net.chrisrichardson.ftgo.domain.dto.OrderDTO} - Order representation</li>
 *   <li>{@link net.chrisrichardson.ftgo.domain.dto.OrderLineItemDTO} - Order line item representation</li>
 * </ul>
 *
 * <h3>Courier Context</h3>
 * <ul>
 *   <li>{@link net.chrisrichardson.ftgo.domain.dto.CourierDTO} - Courier representation</li>
 *   <li>{@link net.chrisrichardson.ftgo.domain.dto.DeliveryActionDTO} - Delivery action representation</li>
 * </ul>
 */
package net.chrisrichardson.ftgo.domain.dto;
