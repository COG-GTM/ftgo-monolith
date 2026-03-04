/**
 * FTGO Error Handling Library.
 *
 * <p>Provides centralized error handling for all FTGO microservices via
 * {@link org.springframework.web.bind.annotation.RestControllerAdvice}.
 *
 * <h2>Features</h2>
 * <ul>
 *     <li>Standardized {@link net.chrisrichardson.ftgo.errorhandling.model.FtgoErrorResponse} envelope</li>
 *     <li>Error code constants in {@link net.chrisrichardson.ftgo.errorhandling.constants.FtgoErrorCodes}</li>
 *     <li>Bean Validation integration (jakarta.validation)</li>
 *     <li>Distributed traceId in all error responses</li>
 *     <li>Inter-service communication error handling</li>
 *     <li>No stack traces leaked to clients</li>
 * </ul>
 *
 * <h2>Auto-Configuration</h2>
 * <p>Add this library as a dependency — the
 * {@link net.chrisrichardson.ftgo.errorhandling.config.FtgoErrorHandlingAutoConfiguration}
 * is activated automatically via Spring Boot's auto-configuration mechanism.</p>
 */
package net.chrisrichardson.ftgo.errorhandling;
