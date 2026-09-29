package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConsumerControllerTest {

  private static final long CONSUMER_ID = 101L;
  private static final PersonName CONSUMER_NAME = new PersonName("John", "Doe");

  private ConsumerService consumerService;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    consumerService = mock(ConsumerService.class);
    ConsumerController consumerController = new ConsumerController();
    ReflectionTestUtils.setField(consumerController, "consumerService", consumerService);
    mockMvc = MockMvcBuilders.standaloneSetup(consumerController).build();
  }

  @Test
  public void shouldCreateConsumer() throws Exception {
    when(consumerService.create(any(PersonName.class))).thenReturn(makeConsumer());

    mockMvc.perform(post("/consumers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": {\"firstName\": \"John\", \"lastName\": \"Doe\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId").value(equalTo((int) CONSUMER_ID)));

    ArgumentCaptor<PersonName> nameCaptor = ArgumentCaptor.forClass(PersonName.class);
    verify(consumerService).create(nameCaptor.capture());
    assertEquals("John", nameCaptor.getValue().getFirstName());
    assertEquals("Doe", nameCaptor.getValue().getLastName());
  }

  @Test
  public void shouldGetConsumer() throws Exception {
    when(consumerService.findById(CONSUMER_ID)).thenReturn(Optional.of(makeConsumer()));

    mockMvc.perform(get("/consumers/" + CONSUMER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName").value("John"))
            .andExpect(jsonPath("$.name.lastName").value("Doe"));
  }

  @Test
  public void shouldReturn404WhenConsumerNotFound() throws Exception {
    when(consumerService.findById(CONSUMER_ID)).thenReturn(Optional.empty());

    mockMvc.perform(get("/consumers/" + CONSUMER_ID))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
  }

  private Consumer makeConsumer() {
    Consumer consumer = new Consumer(CONSUMER_NAME);
    ReflectionTestUtils.setField(consumer, "id", CONSUMER_ID);
    return consumer;
  }
}
