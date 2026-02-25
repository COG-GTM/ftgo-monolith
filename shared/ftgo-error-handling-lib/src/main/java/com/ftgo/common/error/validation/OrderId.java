package com.ftgo.common.error.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite constraint for validating order IDs.
 *
 * <p>Ensures the ID is non-null and positive.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * &#64;OrderId
 * private Long orderId;
 * </pre>
 */
@Documented
@NotNull(message = "Order ID is required")
@Positive(message = "Order ID must be a positive number")
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderId {

    String message() default "Invalid order ID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
