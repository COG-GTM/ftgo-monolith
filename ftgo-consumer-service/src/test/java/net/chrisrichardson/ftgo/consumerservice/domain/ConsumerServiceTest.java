package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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
  public void shouldCreateConsumerWithValidName() {
    Consumer saved = mock(Consumer.class);
    when(consumerRepository.save(any(Consumer.class))).thenReturn(saved);

    assertSame(saved, consumerService.create(new PersonName("John", "Doe")));
    verify(consumerRepository).save(any(Consumer.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMissingName() {
    try {
      consumerService.create(null);
    } finally {
      verifyZeroInteractions(consumerRepository);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMissingFirstName() {
    consumerService.create(new PersonName(null, "Doe"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectBlankFirstName() {
    consumerService.create(new PersonName("  ", "Doe"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMissingLastName() {
    consumerService.create(new PersonName("John", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectBlankLastName() {
    try {
      consumerService.create(new PersonName("John", ""));
    } finally {
      verifyZeroInteractions(consumerRepository);
    }
  }
}
