package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ConsumerControllerTest {

  @Mock
  private ConsumerService consumerService;

  @InjectMocks
  private ConsumerController controller;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldCreateConsumer() {
    PersonName name = new PersonName("Alice", "Jones");
    Consumer consumer = mock(Consumer.class);
    when(consumer.getId()).thenReturn(42L);
    when(consumer.getName()).thenReturn(name);
    when(consumerService.create(any(PersonName.class))).thenReturn(consumer);

    CreateConsumerRequest request = new CreateConsumerRequest(name);

    CreateConsumerResponse response = controller.create(request);
    assertThat(response).isNotNull();
    assertThat(response.getConsumerId()).isEqualTo(42L);
  }

  @Test
  public void shouldGetConsumerReturns200() {
    Consumer consumer = new Consumer(new PersonName("Bob", "Smith"));
    when(consumerService.findById(1L)).thenReturn(Optional.of(consumer));

    ResponseEntity<GetConsumerResponse> response = controller.get(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldGetConsumerReturns404WhenNotFound() {
    when(consumerService.findById(999L)).thenReturn(Optional.empty());

    ResponseEntity<GetConsumerResponse> response = controller.get(999L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
