package net.chrisrichardson.ftgo.testutils.builders;

import net.chrisrichardson.ftgo.common.Money;

/**
 * Builder for creating {@link Money} instances in tests.
 *
 * <p>Provides convenient factory methods for common monetary amounts.
 *
 * <p>Usage:
 * <pre>{@code
 * Money price = MoneyBuilder.aMoney().withAmount("12.34").build();
 * Money zero = MoneyBuilder.zero();
 * Money ten = MoneyBuilder.dollars(10);
 * }</pre>
 */
public class MoneyBuilder {

    private String amount = "10.00";

    private MoneyBuilder() {
    }

    public static MoneyBuilder aMoney() {
        return new MoneyBuilder();
    }

    /**
     * Creates a Money instance representing zero.
     */
    public static Money zero() {
        return Money.ZERO;
    }

    /**
     * Creates a Money instance for the given whole dollar amount.
     */
    public static Money dollars(int amount) {
        return new Money(amount);
    }

    /**
     * Creates a Money instance for the given string amount (e.g., "12.34").
     */
    public static Money amount(String amount) {
        return new Money(amount);
    }

    public MoneyBuilder withAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public Money build() {
        return new Money(amount);
    }
}
