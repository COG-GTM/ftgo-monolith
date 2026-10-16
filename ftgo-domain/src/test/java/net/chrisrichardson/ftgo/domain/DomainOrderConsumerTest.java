package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class DomainOrderConsumerTest {

  @Test
  public void newConsumerHasNameAndNoIdUntilPersisted() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));

    assertThat(consumer.getId()).isNull();
    assertThat(consumer.getName().getFirstName()).isEqualTo("John");
    assertThat(consumer.getName().getLastName()).isEqualTo("Doe");
  }

  @Test
  public void validateOrderByConsumerAcceptsPositiveTotal() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    assertThatCode(() -> consumer.validateOrderByConsumer(new Money("68.70"))).doesNotThrowAnyException();
  }

  // Suspected bug: validateOrderByConsumer is an empty stub ("implement some business logic"),
  // so it accepts any order total, including negative and null totals.
  @Test
  public void validateOrderByConsumerCurrentlyAcceptsNegativeAndNullTotals() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    assertThatCode(() -> consumer.validateOrderByConsumer(new Money("-100.00"))).doesNotThrowAnyException();
    assertThatCode(() -> consumer.validateOrderByConsumer(null)).doesNotThrowAnyException();
  }
}
