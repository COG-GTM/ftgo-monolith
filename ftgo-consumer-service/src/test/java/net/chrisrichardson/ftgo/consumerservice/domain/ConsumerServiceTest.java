package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConsumerServiceTest {

  private ConsumerRepository consumerRepository;
  private ConsumerService consumerService;

  @Before
  public void setUp() {
    consumerRepository = mock(ConsumerRepository.class);
    consumerService = new ConsumerService();
    ReflectionTestUtils.setField(consumerService, "consumerRepository", consumerRepository);
  }

  @Test
  public void shouldCreateConsumer() {
    PersonName name = new PersonName("John", "Doe");
    Consumer consumer = new Consumer(name);
    when(consumerRepository.save(any(Consumer.class))).thenReturn(consumer);

    Consumer result = consumerService.create(name);
    assertNotNull(result);
    assertEquals("John", result.getName().getFirstName());
    verify(consumerRepository).save(any(Consumer.class));
  }

  @Test
  public void shouldFindConsumerById() {
    Consumer consumer = new Consumer(new PersonName("Jane", "Smith"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    Optional<Consumer> result = consumerService.findById(1L);
    assertTrue(result.isPresent());
    assertEquals("Jane", result.get().getName().getFirstName());
  }

  @Test
  public void shouldReturnEmptyWhenNotFound() {
    when(consumerRepository.findById(99L)).thenReturn(Optional.empty());

    Optional<Consumer> result = consumerService.findById(99L);
    assertFalse(result.isPresent());
  }

  @Test
  public void shouldValidateOrderForConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    consumerService.validateOrderForConsumer(1L, new Money("100.00"));
    verify(consumerRepository).findById(1L);
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowWhenConsumerNotFoundOnValidate() {
    when(consumerRepository.findById(99L)).thenReturn(Optional.empty());
    consumerService.validateOrderForConsumer(99L, new Money("100.00"));
  }
}
