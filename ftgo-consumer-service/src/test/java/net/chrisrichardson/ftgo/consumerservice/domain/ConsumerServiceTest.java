package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerServiceTest {

  @Mock
  private ConsumerRepository consumerRepository;

  @InjectMocks
  private ConsumerService consumerService;

  @Test
  public void shouldCreateConsumer() {
    PersonName name = new PersonName("John", "Doe");
    Consumer savedConsumer = new Consumer(name);
    ReflectionTestUtils.setField(savedConsumer, "id", 101L);
    when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

    Consumer result = consumerService.create(name);

    assertThat(result).isSameAs(savedConsumer);
    ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
    verify(consumerRepository).save(consumerCaptor.capture());
    assertThat(consumerCaptor.getValue().getName()).isSameAs(name);
    assertThat(consumerCaptor.getValue().getName().getFirstName()).isEqualTo("John");
    assertThat(consumerCaptor.getValue().getName().getLastName()).isEqualTo("Doe");
  }

  @Test
  public void shouldFindConsumerById() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(101L)).thenReturn(Optional.of(consumer));

    Optional<Consumer> result = consumerService.findById(101L);

    assertThat(result).containsSame(consumer);
    verify(consumerRepository).findById(101L);
  }

  @Test
  public void shouldReturnEmptyWhenNotFound() {
    when(consumerRepository.findById(101L)).thenReturn(Optional.empty());

    Optional<Consumer> result = consumerService.findById(101L);

    assertThat(result).isEmpty();
    verify(consumerRepository).findById(101L);
  }

  @Test
  public void shouldValidateOrderForConsumer() {
    Consumer consumer = mock(Consumer.class);
    Money orderTotal = new Money("42.50");
    when(consumerRepository.findById(101L)).thenReturn(Optional.of(consumer));

    consumerService.validateOrderForConsumer(101L, orderTotal);

    verify(consumer).validateOrderByConsumer(same(orderTotal));
  }

  @Test
  public void shouldThrowConsumerNotFoundWhenValidatingUnknownConsumer() {
    when(consumerRepository.findById(101L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consumerService.validateOrderForConsumer(101L, new Money("42.50")))
            .isInstanceOf(ConsumerNotFoundException.class)
            .isInstanceOf(ConsumerVerificationFailedException.class);
    verify(consumerRepository, never()).save(any(Consumer.class));
  }

  @Test
  public void validateOrderForConsumerCurrentlyAcceptsAnyOrderTotal() {
    // Suspected bug: Consumer.validateOrderByConsumer is a no-op, so ConsumerService.validateOrderForConsumer never rejects an order (even a negative total).
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(101L)).thenReturn(Optional.of(consumer));

    assertThatCode(() -> {
      consumerService.validateOrderForConsumer(101L, new Money("-1"));
      consumerService.validateOrderForConsumer(101L, new Money("999999999999999999999999999999999999"));
    }).doesNotThrowAnyException();
  }
}
