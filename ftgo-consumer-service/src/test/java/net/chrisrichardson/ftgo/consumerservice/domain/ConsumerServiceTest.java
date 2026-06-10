package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

  @Mock
  private ConsumerRepository consumerRepository;

  @InjectMocks
  private ConsumerService consumerService;

  @Test
  void shouldValidateOrderForConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    consumerService.validateOrderForConsumer(1L, new Money("50.00"));

    verify(consumerRepository).findById(1L);
  }

  @Test
  void shouldThrowWhenConsumerNotFound() {
    when(consumerRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consumerService.validateOrderForConsumer(999L, new Money("50.00")))
            .isInstanceOf(ConsumerNotFoundException.class);
  }

  @Test
  void shouldCreateConsumer() {
    PersonName name = new PersonName("John", "Doe");
    Consumer consumer = new Consumer(name);
    when(consumerRepository.save(any(Consumer.class))).thenReturn(consumer);

    Consumer result = consumerService.create(name);

    assertThat(result).isNotNull();
    assertThat(result.getName().getFirstName()).isEqualTo("John");
    verify(consumerRepository).save(any(Consumer.class));
  }

  @Test
  void shouldFindConsumerById() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    Optional<Consumer> result = consumerService.findById(1L);

    assertThat(result).isPresent();
    assertThat(result.get().getName().getFirstName()).isEqualTo("John");
  }

  @Test
  void shouldReturnEmptyWhenConsumerNotFound() {
    when(consumerRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Consumer> result = consumerService.findById(999L);

    assertThat(result).isEmpty();
  }
}
