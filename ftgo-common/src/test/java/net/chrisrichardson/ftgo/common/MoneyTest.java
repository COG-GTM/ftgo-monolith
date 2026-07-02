package net.chrisrichardson.ftgo.common;


import org.junit.Test;

import static org.junit.Assert.*;

public class MoneyTest {

  private final int M1_AMOUNT = 10;
  private final int M2_AMOUNT = 15;

  private Money m1 = new Money(M1_AMOUNT);
  private Money m2 = new Money(M2_AMOUNT);

  @Test
  public void shouldReturnAsString() {
    assertEquals(Integer.toString(M1_AMOUNT), new Money(M1_AMOUNT).asString());
  }

  @Test
  public void shouldCompare() {
    assertTrue(m2.isGreaterThanOrEqual(m2));
    assertTrue(m2.isGreaterThanOrEqual(m1));
    assertFalse(m1.isGreaterThanOrEqual(m2));
  }

  @Test
  public void shouldAdd() {
    assertEquals(new Money(M1_AMOUNT + M2_AMOUNT), m1.add(m2));
  }

  @Test
  public void shouldMultiply() {
    int multiplier = 12;
    assertEquals(new Money(M2_AMOUNT * multiplier), m2.multiply(multiplier));
  }

  @Test
  public void shouldConstructFromString() {
    assertEquals(new Money("10"), m1);
    assertEquals("12.34", new Money("12.34").asString());
  }

  @Test
  public void shouldHaveValueEqualityAndHashCode() {
    assertEquals(new Money(M1_AMOUNT), m1);
    assertEquals(new Money(M1_AMOUNT).hashCode(), m1.hashCode());
    assertNotEquals(m1, m2);
    assertNotEquals(m1, null);
    assertNotEquals(m1, "not money");
    assertEquals(m1, m1);
  }

  @Test
  public void shouldRenderToString() {
    assertTrue(m1.toString().contains("10"));
  }

  @Test
  public void zeroShouldBeAdditiveIdentity() {
    assertEquals(m1, Money.ZERO.add(m1));
    assertTrue(m1.isGreaterThanOrEqual(Money.ZERO));
  }



}