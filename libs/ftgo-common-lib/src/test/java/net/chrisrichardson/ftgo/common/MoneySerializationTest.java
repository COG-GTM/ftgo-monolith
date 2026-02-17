package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class MoneySerializationTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void initialize() {
    objectMapper.registerModule(new MoneyModule());
  }

  public static class MoneyContainer {
    private Money price;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MoneyContainer that = (MoneyContainer) o;
      return Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
      return Objects.hash(price);
    }

    @Override
    public String toString() {
      return "MoneyContainer{price=" + price + "}";
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
  void shouldSerialize() throws IOException {
    Money price = new Money("12.34");
    MoneyContainer mc = new MoneyContainer(price);
    assertEquals("{\"price\":\"12.34\"}", objectMapper.writeValueAsString(mc));
  }

  @Test
  void shouldDeserialize() throws IOException {
    Money price = new Money("12.34");
    MoneyContainer mc = new MoneyContainer(price);
    assertEquals(mc, objectMapper.readValue("{\"price\":\"12.34\"}", MoneyContainer.class));
  }

  @Test
  void shouldFailToDeserializeNestedObject() {
    assertThrows(JsonMappingException.class, () ->
            objectMapper.readValue("{\"price\": { \"amount\" : \"12.34\"} }", MoneyContainer.class));
  }
}
