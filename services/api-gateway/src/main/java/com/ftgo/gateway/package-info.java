/**
 * API Gateway - Single entry point for FTGO microservices.
 *
 * <p>This service routes external requests to the appropriate downstream
 * microservice, applying cross-cutting concerns such as authentication,
 * rate limiting, circuit breaking, and request correlation.
 *
 * <p>Package structure:
 * <ul>
 *   <li>{@code com.ftgo.gateway} - Application entry point</li>
 *   <li>{@code com.ftgo.gateway.config} - Spring configuration and route definitions</li>
 *   <li>{@code com.ftgo.gateway.filter} - Gateway filters (JWT, rate limiting, etc.)</li>
 * </ul>
 */
package com.ftgo.gateway;
