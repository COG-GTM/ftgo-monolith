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

  @Before
  public void setUp() {
    consumerService = mock(ConsumerService.class);
    ConsumerController controller = new ConsumerController();
    ReflectionTestUtils.setField(controller, "consumerService", consumerService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void shouldCreateConsumer() throws Exception {
    PersonName name = new PersonName("John", "Doe");
    Consumer consumer = new Consumer(name);
    ReflectionTestUtils.setField(consumer, "id", 1L);
    when(consumerService.create(any(PersonName.class))).thenReturn(consumer);

    String json = new ObjectMapper().writeValueAsString(
            new net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest(name));

    mockMvc.perform(post("/consumers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId").value(1));
  }

  @Test
  public void shouldGetConsumer() throws Exception {
    Consumer consumer = new Consumer(new PersonName("Jane", "Smith"));
    when(consumerService.findById(1L)).thenReturn(Optional.of(consumer));

    mockMvc.perform(get("/consumers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName").value("Jane"))
            .andExpect(jsonPath("$.name.lastName").value("Smith"));
  }

  @Test
  public void shouldReturn404WhenConsumerNotFound() throws Exception {
    when(consumerService.findById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/consumers/99"))
            .andExpect(status().isNotFound());
  }
}
