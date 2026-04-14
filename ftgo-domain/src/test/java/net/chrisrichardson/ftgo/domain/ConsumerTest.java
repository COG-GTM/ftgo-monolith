package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConsumerTest {

  @Test
  public void shouldCreateConsumerWithName() {
    PersonName name = new PersonName("John", "Doe");
    Consumer consumer = new Consumer(name);

    assertEquals("John", consumer.getName().getFirstName());
    assertEquals("Doe", consumer.getName().getLastName());
  }

  @Test
  public void shouldValidateOrderByConsumerWithoutThrowing() {
    Consumer consumer = new Consumer(new PersonName("Jane", "Doe"));
    // validateOrderByConsumer is a no-op placeholder, should not throw
    consumer.validateOrderByConsumer(new Money("50.00"));
  }

  @Test
  public void shouldReturnNullIdBeforePersistence() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    assertNull(consumer.getId());
  }
}
