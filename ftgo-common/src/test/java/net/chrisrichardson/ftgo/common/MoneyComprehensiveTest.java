package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyComprehensiveTest {

  @Test
  public void shouldCreateMoneyFromBigDecimal() {
    Money money = new Money(new BigDecimal("19.99"));
    assertThat(money.asString()).isEqualTo("19.99");
  }

  @Test
  public void shouldCreateMoneyFromString() {
    Money money = new Money("25.50");
    assertThat(money.asString()).isEqualTo("25.50");
  }

  @Test
  public void shouldCreateMoneyFromInt() {
    Money money = new Money(100);
    assertThat(money.asString()).isEqualTo("100");
  }

  @Test
  public void shouldAddMoney() {
    Money a = new Money("10.00");
    Money b = new Money("5.50");
    Money result = a.add(b);
    assertThat(result.asString()).isEqualTo("15.50");
  }

  @Test
  public void shouldMultiplyMoney() {
    Money money = new Money("3.00");
    Money result = money.multiply(4);
    assertThat(result.asString()).isEqualTo("12.00");
  }

  @Test
  public void shouldCompareGreaterThanOrEqual() {
    Money large = new Money("100.00");
    Money small = new Money("50.00");
    Money equal = new Money("100.00");

    assertThat(large.isGreaterThanOrEqual(small)).isTrue();
    assertThat(large.isGreaterThanOrEqual(equal)).isTrue();
    assertThat(small.isGreaterThanOrEqual(large)).isFalse();
  }

  @Test
  public void shouldBeEqualForSameAmount() {
    Money a = new Money("10.00");
    Money b = new Money("10.00");
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  public void shouldNotBeEqualForDifferentAmounts() {
    Money a = new Money("10.00");
    Money b = new Money("20.00");
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  public void shouldNotEqualNull() {
    Money money = new Money("10.00");
    assertThat(money).isNotEqualTo(null);
  }

  @Test
  public void shouldNotEqualDifferentType() {
    Money money = new Money("10.00");
    assertThat(money.equals("10.00")).isFalse();
  }

  @Test
  public void shouldEqualSameReference() {
    Money money = new Money("10.00");
    assertThat(money.equals(money)).isTrue();
  }

  @Test
  public void shouldHaveToString() {
    Money money = new Money("42.00");
    assertThat(money.toString()).contains("42.00");
  }

  @Test
  public void zeroShouldHaveZeroAmount() {
    assertThat(Money.ZERO.asString()).isEqualTo("0");
  }

  @Test
  public void shouldAddToZero() {
    Money result = Money.ZERO.add(new Money("7.50"));
    assertThat(result.asString()).isEqualTo("7.50");
  }

  @Test
  public void shouldMultiplyByZero() {
    Money money = new Money("10.00");
    Money result = money.multiply(0);
    assertThat(result.asString()).isEqualTo("0.00");
  }
}
