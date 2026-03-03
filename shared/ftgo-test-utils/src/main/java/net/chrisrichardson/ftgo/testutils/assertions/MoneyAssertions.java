package net.chrisrichardson.ftgo.testutils.assertions;

import net.chrisrichardson.ftgo.common.Money;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom AssertJ-style assertions for {@link Money} value objects.
 *
 * <p>Usage:
 * <pre>{@code
 * import static net.chrisrichardson.ftgo.testutils.assertions.MoneyAssertions.*;
 *
 * assertMoneyEquals(actual, new Money("12.34"));
 * assertMoneyIsZero(actual);
 * assertMoneyGreaterThanOrEqual(actual, new Money("10.00"));
 * }</pre>
 */
public final class MoneyAssertions {

    private MoneyAssertions() {
        // Utility class - prevent instantiation
    }

    /**
     * Asserts that two Money instances are equal.
     */
    public static void assertMoneyEquals(Money actual, Money expected) {
        assertThat(actual)
                .as("Money should equal %s", expected.asString())
                .isEqualTo(expected);
    }

    /**
     * Asserts that a Money instance represents zero.
     */
    public static void assertMoneyIsZero(Money actual) {
        assertMoneyEquals(actual, Money.ZERO);
    }

    /**
     * Asserts that the actual money amount is greater than or equal to the expected amount.
     */
    public static void assertMoneyGreaterThanOrEqual(Money actual, Money threshold) {
        assertThat(actual.isGreaterThanOrEqual(threshold))
                .as("Money %s should be >= %s", actual.asString(), threshold.asString())
                .isTrue();
    }

    /**
     * Asserts that the Money string representation matches the expected value.
     */
    public static void assertMoneyString(Money actual, String expectedString) {
        assertThat(actual.asString())
                .as("Money string should be '%s'", expectedString)
                .isEqualTo(expectedString);
    }
}
