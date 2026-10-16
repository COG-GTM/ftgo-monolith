package net.chrisrichardson.ftgo.common;


import org.junit.Test;

import java.math.BigDecimal;

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
  public void constructorsWithEquivalentAmountsHaveEqualValuesAndHashCodes() {
    Money fromBigDecimal = new Money(new BigDecimal("12.34"));
    Money fromString = new Money("12.34");

    assertEquals(fromBigDecimal, fromString);
    assertEquals(fromBigDecimal.hashCode(), fromString.hashCode());
    assertEquals(new Money(5), new Money("5"));
  }

  @Test
  public void equalsHandlesNullOtherTypesAndSameInstance() {
    Money money = new Money("12.34");

    assertFalse(money.equals(null));
    assertFalse(money.equals("12.34"));
    assertTrue(money.equals(money));
  }

  @Test
  public void moneyEqualityCurrentlyDependsOnScale() {
    // Suspected bug: Money.equals/hashCode use BigDecimal.equals, so 10.0 != 10 despite compareTo == 0.
    Money withScale = new Money("10.0");
    Money withoutScale = new Money(10);

    assertNotEquals(withScale, withoutScale);
    assertTrue(withScale.isGreaterThanOrEqual(withoutScale));
    assertTrue(withoutScale.isGreaterThanOrEqual(withScale));
  }

  @Test(expected = NumberFormatException.class)
  public void stringConstructorRejectsInvalidAmount() {
    new Money("abc");
  }

  @Test
  public void asStringUsesPlainDecimalRepresentation() {
    assertEquals("1000", new Money("1E+3").asString());
    assertEquals("12.50", new Money("12.50").asString());
  }

  @Test
  public void addSupportsNegativeAmounts() {
    assertEquals(new Money(-5), new Money(10).add(new Money(-15)));
  }

  @Test
  public void multiplySupportsZeroAndNegativeMultipliers() {
    assertEquals(new Money(0), new Money(12).multiply(0));
    assertEquals(new Money(-36), new Money(12).multiply(-3));
  }

  @Test(expected = NullPointerException.class)
  public void addRejectsNull() {
    new Money(10).add(null);
  }

  @Test
  public void zeroIsStableWhenAddingAnotherAmount() {
    Money amount = new Money("12.34");

    assertEquals("0", Money.ZERO.asString());
    assertEquals(amount, Money.ZERO.add(amount));
    assertEquals("0", Money.ZERO.asString());
  }

  @Test
  public void toStringIncludesAmount() {
    assertTrue(new Money("12.34").toString().contains("amount=12.34"));
  }


}