package net.chrisrichardson.ftgo.testlib.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link MoneyAssertions}.
 */
@DisplayName("MoneyAssertions")
class MoneyAssertionsTest {

    @Test
    @DisplayName("should pass when money values are equal")
    void shouldPassWhenMoneyValuesAreEqual() {
        MoneyAssertions.assertMoneyEquals(
                new BigDecimal("29.99"),
                new BigDecimal("29.99"));
    }

    @Test
    @DisplayName("should pass when money values are equal with different scales")
    void shouldPassWhenMoneyValuesEqualWithDifferentScales() {
        MoneyAssertions.assertMoneyEquals(
                new BigDecimal("29.990"),
                new BigDecimal("29.99"));
    }

    @Test
    @DisplayName("should fail when money values are not equal")
    void shouldFailWhenMoneyValuesAreNotEqual() {
        assertThrows(AssertionError.class, () ->
                MoneyAssertions.assertMoneyEquals(
                        new BigDecimal("29.99"),
                        new BigDecimal("30.00")));
    }

    @Test
    @DisplayName("should pass when money is in range")
    void shouldPassWhenMoneyIsInRange() {
        MoneyAssertions.assertMoneyInRange(
                new BigDecimal("25.00"),
                new BigDecimal("20.00"),
                new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("should pass when money is positive")
    void shouldPassWhenMoneyIsPositive() {
        MoneyAssertions.assertMoneyPositive(new BigDecimal("1.00"));
    }

    @Test
    @DisplayName("should pass when money is zero")
    void shouldPassWhenMoneyIsZero() {
        MoneyAssertions.assertMoneyZero(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("should parse money string and compare")
    void shouldParseMoneyStringAndCompare() {
        MoneyAssertions.assertMoneyEquals("29.99", new BigDecimal("29.99"));
    }
}
