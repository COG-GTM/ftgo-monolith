/**
 * FTGO Test Library — Shared test utilities for FTGO microservices.
 *
 * <p>This library provides reusable test infrastructure for all FTGO
 * bounded contexts:
 *
 * <ul>
 *   <li><strong>builders</strong> — Test data builders for Order, Consumer, Restaurant, Courier</li>
 *   <li><strong>assertions</strong> — Custom domain-specific assertions (Money, Order, Address)</li>
 *   <li><strong>containers</strong> — Testcontainers configuration (MySQL)</li>
 *   <li><strong>config</strong> — Base test classes (unit, integration, API)</li>
 *   <li><strong>templates</strong> — Copy-and-adapt test templates for each tier</li>
 * </ul>
 *
 * <h3>Quick Start</h3>
 * <p>Add the test library as a dependency in your service's build.gradle:
 * <pre>{@code
 * testCompile project(':shared-ftgo-test-lib')
 * }</pre>
 *
 * <p>Then extend the appropriate base class:
 * <pre>{@code
 * // Unit test
 * class MyServiceTest extends AbstractUnitTest { ... }
 *
 * // Integration test
 * class MyRepositoryTest extends AbstractIntegrationTest { ... }
 *
 * // API test
 * class MyApiTest extends AbstractApiTest { ... }
 * }</pre>
 *
 * @see <a href="../../../../../../../docs/testing-strategy.md">Testing Strategy Documentation</a>
 */
package net.chrisrichardson.ftgo.testlib;
