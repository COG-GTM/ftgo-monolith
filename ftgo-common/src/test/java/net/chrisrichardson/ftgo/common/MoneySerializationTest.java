package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MoneySerializationTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  @BeforeClass
  public static void initialize() {
    objectMapper.registerModule(new MoneyModule());
  }


  public static class MoneyContainer {
    private Money price;

    @Override
    public boolean equals(Object o) {
      return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

    public Money getPrice() {
      return price;
    }

    public void setPrice(Money price) {
      this.price = price;
    }

    public MoneyContainer() {

    }

    public MoneyContainer(Money price) {

      this.price = price;
    }
  }

  @Test
  public void shouldSer() throws IOException {
    Money price = new Money("12.34");
    MoneyContainer mc = new MoneyContainer(price);
    assertEquals("{\"price\":\"12.34\"}", objectMapper.writeValueAsString(mc));
  }

  @Test
  public void shouldDe() throws IOException  {
    Money price = new Money("12.34");
    MoneyContainer mc = new MoneyContainer(price);
    assertEquals(mc, objectMapper.readValue("{\"price\":\"12.34\"}", MoneyContainer.class));
  }

  @Test
  public void shouldFailToDe() throws IOException  {
    JsonMappingException jsonMappingException = null;
    try {
      objectMapper.readValue("{\"price\": { \"amount\" : \"12.34\"} }", MoneyContainer.class);
      fail("expected exception");
    } catch (JsonMappingException e) {
      jsonMappingException = e;
    }
    Assert.notNull(jsonMappingException);
  }



  @Test
  public void shouldRejectHugePositiveExponent() throws IOException {
    assertRejected("1e999999999");
  }

  @Test
  public void shouldRejectMaxIntExponent() throws IOException {
    assertRejected("1e2147483647");
  }

  @Test
  public void shouldRejectHugeNegativeExponent() throws IOException {
    assertRejected("1e-999999999");
  }

  @Test
  public void shouldRejectTooManyIntegerDigits() throws IOException {
    assertRejected("123456789012345678");
  }

  @Test
  public void shouldRejectTooManyFractionDigits() throws IOException {
    assertRejected("1.234");
  }

  @Test
  public void shouldRejectOverlongAmount() throws IOException {
    assertRejected("0000000000000000000000000000000001");
  }

  @Test
  public void shouldRejectNonNumericAmount() throws IOException {
    assertRejected("abc");
  }

  @Test
  public void shouldAcceptLargestStorableAmount() throws IOException {
    MoneyContainer mc = objectMapper.readValue("{\"price\":\"99999999999999999.99\"}", MoneyContainer.class);
    assertEquals(new Money("99999999999999999.99"), mc.getPrice());
  }

  @Test
  public void shouldAcceptSmallPositiveExponent() throws IOException {
    MoneyContainer mc = objectMapper.readValue("{\"price\":\"1.5E+3\"}", MoneyContainer.class);
    assertEquals("1500", mc.getPrice().asString());
  }

  private void assertRejected(String amount) throws IOException {
    try {
      objectMapper.readValue("{\"price\":\"" + amount + "\"}", MoneyContainer.class);
      fail("expected exception for " + amount);
    } catch (JsonMappingException e) {
      // expected
    }
  }

}