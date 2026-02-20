package com.ftgo.common.jpa;

import com.ftgo.common.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyConverterTest {

    private MoneyConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MoneyConverter();
    }

    @Test
    void convertToDatabaseColumn_withMoney_returnsAmount() {
        Money money = new Money(new BigDecimal("19.99"));
        BigDecimal result = converter.convertToDatabaseColumn(money);
        assertThat(result).isEqualByComparingTo(new BigDecimal("19.99"));
    }

    @Test
    void convertToDatabaseColumn_withNull_returnsNull() {
        BigDecimal result = converter.convertToDatabaseColumn(null);
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_withAmount_returnsMoney() {
        BigDecimal amount = new BigDecimal("25.50");
        Money result = converter.convertToEntityAttribute(amount);
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    @Test
    void convertToEntityAttribute_withNull_returnsNull() {
        Money result = converter.convertToEntityAttribute(null);
        assertThat(result).isNull();
    }

    @Test
    void roundTrip_preservesValue() {
        Money original = new Money(new BigDecimal("100.00"));
        BigDecimal dbValue = converter.convertToDatabaseColumn(original);
        Money restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored).isEqualTo(original);
    }

    @Test
    void convertToDatabaseColumn_withZero_returnsZero() {
        Money zero = new Money(BigDecimal.ZERO);
        BigDecimal result = converter.convertToDatabaseColumn(zero);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
