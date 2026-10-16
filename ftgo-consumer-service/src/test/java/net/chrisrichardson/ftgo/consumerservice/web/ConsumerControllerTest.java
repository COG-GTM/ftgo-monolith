package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerControllerTest {

  @Mock
  private ConsumerService consumerService;

  @InjectMocks
  private ConsumerController controller;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void shouldCreateConsumer() throws Exception {
    Consumer consumer = consumerWithId(101L, new PersonName("John", "Doe"));
    when(consumerService.create(any(PersonName.class))).thenReturn(consumer);

    mockMvc.perform(post("/consumers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId", is(101)));

    ArgumentCaptor<PersonName> nameCaptor = ArgumentCaptor.forClass(PersonName.class);
    verify(consumerService).create(nameCaptor.capture());
    org.assertj.core.api.Assertions.assertThat(nameCaptor.getValue().getFirstName()).isEqualTo("John");
    org.assertj.core.api.Assertions.assertThat(nameCaptor.getValue().getLastName()).isEqualTo("Doe");
  }

  @Test
  public void shouldGetConsumer() throws Exception {
    when(consumerService.findById(101L)).thenReturn(Optional.of(consumerWithId(101L, new PersonName("John", "Doe"))));

    mockMvc.perform(get("/consumers/101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName", is("John")))
            .andExpect(jsonPath("$.name.lastName", is("Doe")));
  }

  @Test
  public void getConsumerCurrentlyReturnsZeroConsumerId() throws Exception {
    // Suspected bug: GetConsumerResponse extends CreateConsumerResponse but never sets consumerId, so GET /consumers/{id} always reports consumerId 0.
    when(consumerService.findById(101L)).thenReturn(Optional.of(consumerWithId(101L, new PersonName("John", "Doe"))));

    mockMvc.perform(get("/consumers/101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId", is(0)));
  }

  @Test
  public void shouldReturn404WhenConsumerNotFound() throws Exception {
    when(consumerService.findById(101L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/consumers/101"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
  }

  @Test
  public void shouldReturn400ForNonNumericConsumerId() throws Exception {
    mockMvc.perform(get("/consumers/abc"))
            .andExpect(status().isBadRequest());

    verifyZeroInteractions(consumerService);
  }

  @Test
  public void shouldReturn400WhenCreateBodyMissing() throws Exception {
    mockMvc.perform(post("/consumers").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

    verifyZeroInteractions(consumerService);
  }

  @Test
  public void createConsumerCurrentlyAcceptsMissingName() throws Exception {
    // Suspected bug: ConsumerController.create has no validation; a request without a name creates a consumer with a null name.
    when(consumerService.create(isNull(PersonName.class))).thenReturn(consumerWithId(101L, null));

    mockMvc.perform(post("/consumers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId", is(101)));

    verify(consumerService).create(isNull(PersonName.class));
  }

  @Test
  public void shouldReturn405ForUnsupportedMethod() throws Exception {
    mockMvc.perform(delete("/consumers/1"))
            .andExpect(status().isMethodNotAllowed());
  }

  private Consumer consumerWithId(long id, PersonName name) {
    Consumer consumer = new Consumer(name);
    ReflectionTestUtils.setField(consumer, "id", id);
    return consumer;
  }
}
