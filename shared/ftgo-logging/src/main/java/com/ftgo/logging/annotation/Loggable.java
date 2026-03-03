package com.ftgo.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or method for automatic entry/exit logging via the {@code LoggingAspect}.
 *
 * <p>When applied to a class, all public methods of that class will have
 * entry/exit logging at DEBUG level. When applied to a method, only that
 * specific method will be logged.</p>
 *
 * <h3>Usage:</h3>
 * <pre>
 * &#64;Loggable
 * &#64;Service
 * public class OrderService {
 *     public Order createOrder(CreateOrderRequest request) {
 *         // Entry and exit will be logged automatically
 *     }
 * }
 * </pre>
 *
 * <p>Log output includes method name, parameter types (not values to avoid PII),
 * return type, and execution duration in milliseconds.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
}
