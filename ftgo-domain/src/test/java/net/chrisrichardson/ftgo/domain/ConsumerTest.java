package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerTest {

  @Test
  public void shouldCreateConsumer() {
    Consumer consumer = new Consumer(new PersonName("Alice", "Johnson"));
    assertThat(consumer.getName().getFirstName()).isEqualTo("Alice");
    assertThat(consumer.getName().getLastName()).isEqualTo("Johnson");
  }

  @Test
  public void shouldValidateOrderByConsumer() {
    Consumer consumer = new Consumer(new PersonName("Bob", "Smith"));
    // Should not throw - the method is a no-op placeholder
    consumer.validateOrderByConsumer(new Money("100.00"));
  }

  @Test
  public void shouldGetId() {
    Consumer consumer = new Consumer(new PersonName("Test", "User"));
    assertThat(consumer.getId()).isNull();
  }
}
