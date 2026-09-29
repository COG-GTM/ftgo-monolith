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

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerServiceTest {

  private static final long CONSUMER_ID = 101L;
  private static final PersonName CONSUMER_NAME = new PersonName("John", "Doe");
  private static final Money ORDER_TOTAL = new Money("12.34");

  @Mock
  private ConsumerRepository consumerRepository;

  @InjectMocks
  private ConsumerService consumerService;

  @Test
  public void shouldCreateConsumer() {
    Consumer savedConsumer = new Consumer(CONSUMER_NAME);
    when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

    Consumer result = consumerService.create(CONSUMER_NAME);

    ArgumentCaptor<Consumer> captor = ArgumentCaptor.forClass(Consumer.class);
    verify(consumerRepository).save(captor.capture());
    assertSame(CONSUMER_NAME, captor.getValue().getName());
    assertSame(savedConsumer, result);
  }

  @Test
  public void shouldFindConsumerById() {
    Consumer consumer = new Consumer(CONSUMER_NAME);
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.of(consumer));

    Optional<Consumer> result = consumerService.findById(CONSUMER_ID);

    assertTrue(result.isPresent());
    assertSame(consumer, result.get());
  }

  @Test
  public void shouldReturnEmptyWhenNotFound() {
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.empty());

    Optional<Consumer> result = consumerService.findById(CONSUMER_ID);

    assertFalse(result.isPresent());
  }

  @Test
  public void shouldValidateOrderForConsumer() {
    Consumer consumer = mock(Consumer.class);
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.of(consumer));

    consumerService.validateOrderForConsumer(CONSUMER_ID, ORDER_TOTAL);

    verify(consumer).validateOrderByConsumer(ORDER_TOTAL);
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowWhenConsumerNotFoundOnValidate() {
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.empty());

    consumerService.validateOrderForConsumer(CONSUMER_ID, ORDER_TOTAL);
  }
}
