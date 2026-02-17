package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

  private final int M1_AMOUNT = 10;
  private final int M2_AMOUNT = 15;

  private Money m1 = new Money(M1_AMOUNT);
  private Money m2 = new Money(M2_AMOUNT);

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
  void shouldBeEqual() {
    assertEquals(new Money(10), new Money("10"));
  }

  @Test
  void zeroShouldBeZero() {
    assertEquals(new Money(0), Money.ZERO);
  }
}
