package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConsumerControllerTest {

  private static final long CONSUMER_ID = 42L;

  private ConsumerRepository consumerRepository;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    consumerRepository = mock(ConsumerRepository.class);
    ConsumerService consumerService = new ConsumerService();
    ReflectionTestUtils.setField(consumerService, "consumerRepository", consumerRepository);
    ConsumerController controller = new ConsumerController();
    ReflectionTestUtils.setField(controller, "consumerService", consumerService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new IllegalArgumentAsBadRequest())
            .build();
  }

  @Test
  public void shouldReturnIdAndNameForExistingConsumer() throws Exception {
    Consumer consumer = consumer(CONSUMER_ID, "John", "Doe");
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.of(consumer));

    mockMvc.perform(get("/consumers/" + CONSUMER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId").value((int) CONSUMER_ID))
            .andExpect(jsonPath("$.name.firstName").value("John"))
            .andExpect(jsonPath("$.name.lastName").value("Doe"));
  }

  @Test
  public void shouldReturn404ForUnknownConsumer() throws Exception {
    when(consumerRepository.findById(CONSUMER_ID)).thenReturn(Optional.empty());

    mockMvc.perform(get("/consumers/" + CONSUMER_ID))
            .andExpect(status().isNotFound());
  }

  @Test
  public void shouldCreateConsumerWithValidName() throws Exception {
    Consumer saved = consumer(CONSUMER_ID, "John", "Doe");
    when(consumerRepository.save(any(Consumer.class))).thenReturn(saved);

    postConsumer("{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"}}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId").value((int) CONSUMER_ID));
  }

  @Test
  public void shouldRejectBlankFirstName() throws Exception {
    assertBadRequest("{\"name\":{\"firstName\":\" \",\"lastName\":\"Doe\"}}");
  }

  @Test
  public void shouldRejectMissingFirstName() throws Exception {
    assertBadRequest("{\"name\":{\"lastName\":\"Doe\"}}");
  }

  @Test
  public void shouldRejectBlankLastName() throws Exception {
    assertBadRequest("{\"name\":{\"firstName\":\"John\",\"lastName\":\"\"}}");
  }

  @Test
  public void shouldRejectMissingLastName() throws Exception {
    assertBadRequest("{\"name\":{\"firstName\":\"John\"}}");
  }

  @Test
  public void shouldRejectMissingName() throws Exception {
    assertBadRequest("{}");
  }

  @Test
  public void shouldRejectMissingBody() throws Exception {
    mockMvc.perform(post("/consumers").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    verify(consumerRepository, never()).save(any(Consumer.class));
  }

  private void assertBadRequest(String body) throws Exception {
    postConsumer(body).andExpect(status().isBadRequest());
    verify(consumerRepository, never()).save(any(Consumer.class));
  }

  private ResultActions postConsumer(String body) throws Exception {
    return mockMvc.perform(post("/consumers").contentType(MediaType.APPLICATION_JSON).content(body));
  }

  private static Consumer consumer(long id, String firstName, String lastName) {
    Consumer consumer = new Consumer(new PersonName(firstName, lastName));
    ReflectionTestUtils.setField(consumer, "id", id);
    return consumer;
  }

  // Mirrors the application's GlobalExceptionHandler mapping of IllegalArgumentException to 400.
  @ControllerAdvice
  static class IllegalArgumentAsBadRequest {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handle(IllegalArgumentException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}
