package com.ftgo.common.error.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MoneyAmountValidator}.
 */
class MoneyAmountValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    static class TestDto {
        @MoneyAmount(min = 0.01, max = 99999.99)
        private BigDecimal amount;

        TestDto(BigDecimal amount) {
            this.amount = amount;
        }
    }

    @Test
    @DisplayName("Valid amount passes validation")
    void validAmount() {
        TestDto dto = new TestDto(new BigDecimal("10.00"));
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Null amount passes (use @NotNull for null check)")
    void nullAmountPasses() {
        TestDto dto = new TestDto(null);
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Amount below minimum fails")
    void belowMinimum() {
        TestDto dto = new TestDto(new BigDecimal("0.00"));
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Amount above maximum fails")
    void aboveMaximum() {
        TestDto dto = new TestDto(new BigDecimal("100000.00"));
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Amount with more than 2 decimal places fails")
    void tooManyDecimals() {
        TestDto dto = new TestDto(new BigDecimal("10.123"));
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("2 decimal places");
    }

    @Test
    @DisplayName("Boundary value at minimum passes")
    void boundaryMin() {
        TestDto dto = new TestDto(new BigDecimal("0.01"));
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Boundary value at maximum passes")
    void boundaryMax() {
        TestDto dto = new TestDto(new BigDecimal("99999.99"));
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}
