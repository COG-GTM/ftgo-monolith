package com.ftgo.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    private final int M1_AMOUNT = 10;
    private final int M2_AMOUNT = 15;

    private Money m1 = new Money(M1_AMOUNT);
    private Money m2 = new Money(M2_AMOUNT);

    @Test
    void shouldCreateFromInt() {
        Money money = new Money(42);
        assertEquals("42", money.asString());
    }

    @Test
    void shouldCreateFromString() {
        Money money = new Money("12.34");
        assertEquals("12.34", money.asString());
    }

    @Test
    void shouldCreateFromBigDecimal() {
        Money money = new Money(new BigDecimal("99.99"));
        assertEquals("99.99", money.asString());
    }

    @Test
    void shouldReturnAsString() {
        assertEquals(Integer.toString(M1_AMOUNT), new Money(M1_AMOUNT).asString());
    }

    @Test
    void shouldCompare() {
        assertTrue(m2.isGreaterThanOrEqual(m2));
        assertTrue(m2.isGreaterThanOrEqual(m1));
        assertFalse(m1.isGreaterThanOrEqual(m2));
    }

    @Test
    void shouldAdd() {
        assertEquals(new Money(M1_AMOUNT + M2_AMOUNT), m1.add(m2));
    }

    @Test
    void shouldMultiply() {
        int multiplier = 12;
        assertEquals(new Money(M2_AMOUNT * multiplier), m2.multiply(multiplier));
    }

    @Test
    void shouldBeEqualForSameAmount() {
        Money a = new Money(100);
        Money b = new Money(100);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentAmounts() {
        assertNotEquals(m1, m2);
    }

    @Test
    void zeroShouldBeZero() {
        assertEquals("0", Money.ZERO.asString());
    }

    @Test
    void shouldReturnAmount() {
        Money money = new Money("55.50");
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("55.50"));
    }

    @Test
    void shouldHaveToString() {
        Money money = new Money(10);
        assertThat(money.toString()).contains("10");
    }
}
