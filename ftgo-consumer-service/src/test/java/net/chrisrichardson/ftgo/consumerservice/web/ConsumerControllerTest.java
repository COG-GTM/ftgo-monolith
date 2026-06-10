package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerControllerTest {

  @Mock
  private ConsumerService consumerService;

  @Mock
  private Consumer mockConsumer;

  @InjectMocks
  private ConsumerController controller;

  @Test
  void shouldCreateConsumer() {
    when(mockConsumer.getId()).thenReturn(1L);
    when(consumerService.create(any(PersonName.class))).thenReturn(mockConsumer);

    CreateConsumerRequest request = new CreateConsumerRequest(new PersonName("John", "Doe"));

    CreateConsumerResponse response = controller.create(request);

    assertThat(response.getConsumerId()).isEqualTo(1L);
  }

  @Test
  void shouldGetConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerService.findById(1L)).thenReturn(Optional.of(consumer));

    ResponseEntity<GetConsumerResponse> response = controller.get(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getName().getFirstName()).isEqualTo("John");
  }

  @Test
  void shouldReturn404WhenConsumerNotFound() {
    when(consumerService.findById(999L)).thenReturn(Optional.empty());

    ResponseEntity<GetConsumerResponse> response = controller.get(999L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
