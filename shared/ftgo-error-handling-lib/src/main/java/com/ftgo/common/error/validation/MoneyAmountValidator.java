package com.ftgo.common.error.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Validator for the {@link MoneyAmount} constraint annotation.
 *
 * <p>Validates that a {@link BigDecimal} value is non-negative and
 * within the specified min/max range.</p>
 */
public class MoneyAmountValidator implements ConstraintValidator<MoneyAmount, BigDecimal> {

    private double min;
    private double max;

    @Override
    public void initialize(MoneyAmount annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }

        double doubleValue = value.doubleValue();
        if (doubleValue < min || doubleValue > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Amount must be between %.2f and %.2f", min, max)
            ).addConstraintViolation();
            return false;
        }

        if (value.scale() > 2) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Amount cannot have more than 2 decimal places"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
