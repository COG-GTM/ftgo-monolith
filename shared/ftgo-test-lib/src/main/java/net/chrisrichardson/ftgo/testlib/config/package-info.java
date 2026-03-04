/**
 * Base test configuration classes for FTGO microservices.
 *
 * <p>Provides abstract base classes that configure common test infrastructure:
 * <ul>
 *   <li>{@link net.chrisrichardson.ftgo.testlib.config.AbstractUnitTest} — JUnit 5 + Mockito unit tests</li>
 *   <li>{@link net.chrisrichardson.ftgo.testlib.config.AbstractIntegrationTest} — Spring Boot + Testcontainers integration tests</li>
 *   <li>{@link net.chrisrichardson.ftgo.testlib.config.AbstractApiTest} — Rest-Assured API tests</li>
 *   <li>{@link net.chrisrichardson.ftgo.testlib.config.TestJsonHelper} — JSON serialization utilities</li>
 * </ul>
 */
package net.chrisrichardson.ftgo.testlib.config;
