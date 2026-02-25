package com.ftgo.common.error.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Bean Validation annotation for validating monetary amounts.
 *
 * <p>Validates that a numeric value represents a valid money amount
 * (non-negative with at most 2 decimal places).</p>
 *
 * <p>Usage:</p>
 * <pre>
 * &#64;MoneyAmount
 * private BigDecimal price;
 *
 * &#64;MoneyAmount(min = 10.00, message = "Order minimum is $10.00")
 * private BigDecimal orderTotal;
 * </pre>
 */
@Documented
@Constraint(validatedBy = MoneyAmountValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MoneyAmount {

    String message() default "Invalid monetary amount";

    double min() default 0.0;

    double max() default Double.MAX_VALUE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
