package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerServiceTest {

  @InjectMocks
  private ConsumerService consumerService;

  @Mock
  private ConsumerRepository consumerRepository;

  @Test
  public void shouldCreateConsumer() {
    PersonName name = new PersonName("John", "Doe");
    Consumer savedConsumer = new Consumer(name);
    when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

    Consumer result = consumerService.create(name);

    assertNotNull(result);
    assertEquals("John", result.getName().getFirstName());
    assertEquals("Doe", result.getName().getLastName());
    verify(consumerRepository).save(any(Consumer.class));
  }

  @Test
  public void shouldFindConsumerById() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    Optional<Consumer> result = consumerService.findById(1L);

    assertTrue(result.isPresent());
    assertEquals("John", result.get().getName().getFirstName());
    verify(consumerRepository).findById(1L);
  }

  @Test
  public void shouldReturnEmptyWhenConsumerNotFound() {
    when(consumerRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Consumer> result = consumerService.findById(999L);

    assertFalse(result.isPresent());
    verify(consumerRepository).findById(999L);
  }

  @Test
  public void shouldValidateOrderForExistingConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    consumerService.validateOrderForConsumer(1L, new Money("50.00"));

    verify(consumerRepository).findById(1L);
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowWhenValidatingOrderForNonExistentConsumer() {
    when(consumerRepository.findById(999L)).thenReturn(Optional.empty());

    consumerService.validateOrderForConsumer(999L, new Money("50.00"));
  }
}
