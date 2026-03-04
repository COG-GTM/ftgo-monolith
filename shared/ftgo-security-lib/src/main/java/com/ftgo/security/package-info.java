/**
 * FTGO Security Library — Shared Spring Security configuration for FTGO microservices.
 *
 * <p>Package structure:
 * <ul>
 *   <li>{@code com.ftgo.security} — Library root package</li>
 *   <li>{@code com.ftgo.security.config} — SecurityFilterChain, CORS, and actuator security configuration</li>
 *   <li>{@code com.ftgo.security.handler} — Security exception handlers (401/403 responses)</li>
 *   <li>{@code com.ftgo.security.jwt} — JWT authentication (token provider, filter, user details)</li>
 *   <li>{@code com.ftgo.security.util} — Security utility classes</li>
 * </ul>
 *
 * <p>This library provides a base security configuration that each microservice can
 * import and customize. It establishes:
 * <ul>
 *   <li>Stateless session management (no HTTP sessions)</li>
 *   <li>CSRF protection disabled for stateless REST APIs</li>
 *   <li>JWT Bearer token authentication via JJWT</li>
 *   <li>CORS configuration for API gateway origins</li>
 *   <li>Actuator endpoint security (/health public, others secured)</li>
 *   <li>JSON error responses for authentication/authorization failures</li>
 *   <li>Method-level security via {@code @PreAuthorize} / {@code @PostAuthorize}</li>
 * </ul>
 */
package com.ftgo.security;
