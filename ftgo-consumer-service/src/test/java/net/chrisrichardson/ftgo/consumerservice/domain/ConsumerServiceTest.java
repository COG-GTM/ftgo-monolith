package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ConsumerServiceTest {

  @Mock
  private ConsumerRepository consumerRepository;

  @InjectMocks
  private ConsumerService consumerService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldCreateConsumer() {
    PersonName name = new PersonName("Alice", "Jones");
    Consumer consumer = new Consumer(name);
    when(consumerRepository.save(any(Consumer.class))).thenReturn(consumer);

    Consumer result = consumerService.create(name);
    assertThat(result.getName().getFirstName()).isEqualTo("Alice");
    verify(consumerRepository).save(any(Consumer.class));
  }

  @Test
  public void shouldFindConsumerById() {
    Consumer consumer = new Consumer(new PersonName("Bob", "Smith"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    Optional<Consumer> result = consumerService.findById(1L);
    assertThat(result).isPresent();
    assertThat(result.get().getName().getFirstName()).isEqualTo("Bob");
  }

  @Test
  public void shouldReturnEmptyWhenConsumerNotFound() {
    when(consumerRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Consumer> result = consumerService.findById(999L);
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldValidateOrderForConsumer() {
    Consumer consumer = new Consumer(new PersonName("Test", "User"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    consumerService.validateOrderForConsumer(1L, new Money("50.00"));
    // should not throw
  }

  @Test
  public void shouldThrowWhenConsumerNotFoundForValidation() {
    when(consumerRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consumerService.validateOrderForConsumer(999L, new Money("50.00")))
            .isInstanceOf(ConsumerNotFoundException.class);
  }
}
