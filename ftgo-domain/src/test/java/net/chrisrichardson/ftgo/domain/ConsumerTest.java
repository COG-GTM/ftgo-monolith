package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumerTest {

  @Test
  void shouldCreateConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));

    assertThat(consumer.getName().getFirstName()).isEqualTo("John");
    assertThat(consumer.getName().getLastName()).isEqualTo("Doe");
    assertThat(consumer.getId()).isNull();
  }

  @Test
  void shouldValidateOrderByConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    consumer.validateOrderByConsumer(new Money("100.00"));
  }
}
