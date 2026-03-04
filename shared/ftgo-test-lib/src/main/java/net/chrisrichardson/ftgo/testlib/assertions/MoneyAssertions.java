package net.chrisrichardson.ftgo.testlib.assertions;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom AssertJ-style assertions for Money and currency-related values.
 *
 * <p>Provides domain-specific assertion methods that make test code more
 * readable and produce clearer failure messages.
 *
 * <p>Usage:
 * <pre>{@code
 * MoneyAssertions.assertMoney(actual)
 *     .isEqualTo(new BigDecimal("29.99"));
 *
 * MoneyAssertions.assertMoneyInRange(actual,
 *     new BigDecimal("20.00"), new BigDecimal("50.00"));
 * }</pre>
 */
public final class MoneyAssertions {

    private MoneyAssertions() {
        // Utility class
    }

    /**
     * Asserts that a BigDecimal money value equals the expected amount
     * (compared with scale 2, HALF_UP rounding).
     *
     * @param actual   the actual money value
     * @param expected the expected money value
     */
    public static void assertMoneyEquals(BigDecimal actual, BigDecimal expected) {
        assertThat(actual.setScale(2, RoundingMode.HALF_UP))
                .as("Money value")
                .isEqualByComparingTo(expected.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Asserts that a money string equals the expected BigDecimal amount.
     *
     * @param actualString the actual money value as a string (e.g., "29.99")
     * @param expected     the expected money value
     */
    public static void assertMoneyEquals(String actualString, BigDecimal expected) {
        BigDecimal actual = new BigDecimal(actualString);
        assertMoneyEquals(actual, expected);
    }

    /**
     * Asserts that a money value falls within the specified range (inclusive).
     *
     * @param actual the actual money value
     * @param min    the minimum expected value (inclusive)
     * @param max    the maximum expected value (inclusive)
     */
    public static void assertMoneyInRange(BigDecimal actual, BigDecimal min, BigDecimal max) {
        assertThat(actual)
                .as("Money value should be between %s and %s", min, max)
                .isGreaterThanOrEqualTo(min)
                .isLessThanOrEqualTo(max);
    }

    /**
     * Asserts that a money value is positive (greater than zero).
     *
     * @param actual the actual money value
     */
    public static void assertMoneyPositive(BigDecimal actual) {
        assertThat(actual)
                .as("Money value should be positive")
                .isGreaterThan(BigDecimal.ZERO);
    }

    /**
     * Asserts that a money value is zero.
     *
     * @param actual the actual money value
     */
    public static void assertMoneyZero(BigDecimal actual) {
        assertThat(actual.setScale(2, RoundingMode.HALF_UP))
                .as("Money value should be zero")
                .isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
}
