/**
 * JWT authentication support for FTGO microservices.
 *
 * <p>This package provides JWT-based stateless authentication:
 * <ul>
 *   <li>{@link com.ftgo.security.jwt.FtgoJwtProperties} — externalized JWT configuration</li>
 *   <li>{@link com.ftgo.security.jwt.JwtTokenProvider} — token creation, validation, and claims extraction</li>
 *   <li>{@link com.ftgo.security.jwt.JwtAuthenticationFilter} — servlet filter for Bearer token authentication</li>
 *   <li>{@link com.ftgo.security.jwt.JwtAuthenticationToken} — Spring Security Authentication implementation</li>
 *   <li>{@link com.ftgo.security.jwt.FtgoUserDetails} — user context extracted from JWT claims</li>
 *   <li>{@link com.ftgo.security.jwt.JwtAutoConfiguration} — auto-configuration entry point</li>
 * </ul>
 *
 * <p>Authentication flow:
 * <ol>
 *   <li>Client sends request with {@code Authorization: Bearer <token>} header</li>
 *   <li>{@code JwtAuthenticationFilter} extracts and validates the token</li>
 *   <li>User details are extracted from JWT claims and stored in SecurityContext</li>
 *   <li>Downstream code accesses user context via {@code SecurityContextHolder} or
 *       {@link com.ftgo.security.util.SecurityUtils}</li>
 * </ol>
 */
package com.ftgo.security.jwt;
