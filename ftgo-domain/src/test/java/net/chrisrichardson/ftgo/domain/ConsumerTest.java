package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConsumerTest {

  @Test
  public void shouldCreateConsumerWithPersonName() {
    PersonName name = new PersonName("John", "Doe");
    Consumer consumer = new Consumer(name);

    assertEquals("John", consumer.getName().getFirstName());
    assertEquals("Doe", consumer.getName().getLastName());
  }

  @Test
  public void shouldHaveNullIdBeforePersistence() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    assertNull(consumer.getId());
  }

  @Test
  public void shouldValidateOrderByConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    consumer.validateOrderByConsumer(new Money("100.00"));
  }

  @Test
  public void shouldValidateOrderWithZeroTotal() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    consumer.validateOrderByConsumer(Money.ZERO);
  }

  @Test
  public void shouldValidateOrderWithLargeTotal() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    consumer.validateOrderByConsumer(new Money("9999.99"));
  }

  @Test
  public void shouldReturnName() {
    PersonName name = new PersonName("Jane", "Smith");
    Consumer consumer = new Consumer(name);
    assertSame(name, consumer.getName());
  }
}
