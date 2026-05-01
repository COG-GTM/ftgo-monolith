package net.chrisrichardson.ftgo.consumerservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ConsumerControllerTest {

  private MockMvc mockMvc;
  private ConsumerService consumerService;
  private ConsumerController consumerController;

  @Before
  public void setUp() {
    consumerService = mock(ConsumerService.class);
    consumerController = new ConsumerController();
    ReflectionTestUtils.setField(consumerController, "consumerService", consumerService);
    mockMvc = MockMvcBuilders.standaloneSetup(consumerController).build();
  }

  @Test
  public void shouldCreateConsumer() throws Exception {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    ReflectionTestUtils.setField(consumer, "id", 1L);
    when(consumerService.create(any(PersonName.class))).thenReturn(consumer);

    mockMvc.perform(post("/consumers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId").value(1));
  }

  @Test
  public void shouldGetConsumer() throws Exception {
    Consumer consumer = new Consumer(new PersonName("Jane", "Smith"));
    when(consumerService.findById(1L)).thenReturn(Optional.of(consumer));

    mockMvc.perform(get("/consumers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName").value("Jane"));
  }

  @Test
  public void shouldReturn404WhenConsumerNotFound() throws Exception {
    when(consumerService.findById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/consumers/999"))
            .andExpect(status().isNotFound());
  }
}
