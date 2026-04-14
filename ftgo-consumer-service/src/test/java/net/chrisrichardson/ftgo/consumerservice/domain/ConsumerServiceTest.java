package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConsumerServiceTest {

  private ConsumerRepository consumerRepository;
  private ConsumerService consumerService;

  @Before
  public void setUp() throws Exception {
    consumerRepository = mock(ConsumerRepository.class);
    consumerService = new ConsumerService();

    // Inject the mock via reflection since it uses @Autowired field injection
    Field field = ConsumerService.class.getDeclaredField("consumerRepository");
    field.setAccessible(true);
    field.set(consumerService, consumerRepository);
  }

  @Test
  public void shouldValidateOrderForExistingConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    consumerService.validateOrderForConsumer(1L, new Money("50.00"));

    verify(consumerRepository).findById(1L);
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowWhenConsumerNotFound() {
    when(consumerRepository.findById(999L)).thenReturn(Optional.empty());

    consumerService.validateOrderForConsumer(999L, new Money("50.00"));
  }

  @Test
  public void shouldCreateConsumer() {
    PersonName name = new PersonName("Jane", "Doe");
    Consumer savedConsumer = new Consumer(name);
    when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

    Consumer result = consumerService.create(name);

    assertNotNull(result);
    assertEquals("Jane", result.getName().getFirstName());
    verify(consumerRepository).save(any(Consumer.class));
  }

  @Test
  public void shouldFindConsumerById() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

    Optional<Consumer> result = consumerService.findById(1L);

    assertTrue(result.isPresent());
    assertEquals("John", result.get().getName().getFirstName());
  }

  @Test
  public void shouldReturnEmptyWhenConsumerNotFound() {
    when(consumerRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Consumer> result = consumerService.findById(999L);

    assertFalse(result.isPresent());
  }
}
