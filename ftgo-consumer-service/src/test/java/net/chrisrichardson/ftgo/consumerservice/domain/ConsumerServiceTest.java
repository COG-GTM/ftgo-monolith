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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConsumerService} (UNIT_TEST_PLAN.md section 3.2).
 * The repository is a Mockito mock, so no Spring context or database is needed;
 * {@code @InjectMocks} satisfies the service's field-injected {@code @Autowired} repository.
 */
@ExtendWith(MockitoExtension.class)
public class ConsumerServiceTest {

  private static final long CONSUMER_ID = 101L;
  private static final PersonName CONSUMER_NAME = new PersonName("Chris", "Richardson");
  private static final Money ORDER_TOTAL = new Money("12.34");

  @Mock
  private ConsumerRepository consumerRepository;

  @InjectMocks
  private ConsumerService consumerService;

  private Consumer consumer;

  @BeforeEach
  public void setUp() {
    consumer = new Consumer(CONSUMER_NAME);
  }

  @Test
  public void shouldCreateConsumer() {
    // The repository echoes back whatever entity it is asked to persist.
    when(consumerRepository.save(any(Consumer.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Consumer created = consumerService.create(CONSUMER_NAME);

    // The service must hand a new Consumer carrying the requested name to the repository.
    verify(consumerRepository).save(any(Consumer.class));
    assertThat(created.getName()).isSameAs(CONSUMER_NAME);
  }

  @Test
  public void shouldFindConsumerById() {
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.of(consumer));

    Optional<Consumer> found = consumerService.findById(CONSUMER_ID);

    assertThat(found).containsSame(consumer);
  }

  @Test
  public void shouldReturnEmptyWhenNotFound() {
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.empty());

    assertThat(consumerService.findById(CONSUMER_ID)).isEmpty();
  }

  @Test
  public void shouldValidateOrderForConsumer() {
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.of(consumer));

    // An existing consumer validates without throwing.
    consumerService.validateOrderForConsumer(CONSUMER_ID, ORDER_TOTAL);

    verify(consumerRepository).findById(CONSUMER_ID);
  }

  @Test
  public void shouldThrowWhenConsumerNotFoundOnValidate() {
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.empty());

    // A missing consumer surfaces as ConsumerNotFoundException (a ConsumerVerificationFailedException).
    assertThatThrownBy(() -> consumerService.validateOrderForConsumer(CONSUMER_ID, ORDER_TOTAL))
            .isInstanceOf(ConsumerNotFoundException.class)
            .isInstanceOf(ConsumerVerificationFailedException.class);
  }
}
