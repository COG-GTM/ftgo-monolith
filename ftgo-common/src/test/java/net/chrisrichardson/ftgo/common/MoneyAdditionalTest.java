package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyAdditionalTest {

  @Test
  void shouldCreateFromBigDecimal() {
    Money money = new Money(new BigDecimal("12.34"));
    assertThat(money.asString()).isEqualTo("12.34");
  }

  @Test
  void shouldCreateFromString() {
    Money money = new Money("99.99");
    assertThat(money.asString()).isEqualTo("99.99");
  }

  @Test
  void shouldCreateFromInt() {
    Money money = new Money(42);
    assertThat(money.asString()).isEqualTo("42");
  }

  @Test
  void shouldHaveZeroConstant() {
    assertThat(Money.ZERO.asString()).isEqualTo("0");
  }

  @Test
  void shouldBeEqualToSelf() {
    Money money = new Money("10.00");
    assertThat(money).isEqualTo(money);
  }

  @Test
  void shouldNotBeEqualToNull() {
    Money money = new Money("10.00");
    assertThat(money).isNotEqualTo(null);
  }

  @Test
  void shouldNotBeEqualToDifferentClass() {
    Money money = new Money("10.00");
    assertThat(money).isNotEqualTo("10.00");
  }

  @Test
  void shouldHaveConsistentHashCode() {
    Money money1 = new Money("10.00");
    Money money2 = new Money("10.00");
    assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
  }

  @Test
  void shouldHaveToString() {
    Money money = new Money("10.00");
    assertThat(money.toString()).contains("10.00");
  }
}
