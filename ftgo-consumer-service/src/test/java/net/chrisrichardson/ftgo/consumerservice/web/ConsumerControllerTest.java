package net.chrisrichardson.ftgo.consumerservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import net.chrisrichardson.ftgo.consumerservice.domain.Consumer;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConsumerControllerTest {

  private ConsumerService consumerService;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    consumerService = mock(ConsumerService.class);
    ConsumerController controller = new ConsumerController();
    try {
      java.lang.reflect.Field field = ConsumerController.class.getDeclaredField("consumerService");
      field.setAccessible(true);
      field.set(controller, consumerService);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter).build();
  }

  @Test
  public void shouldValidateOrderForConsumer() throws Exception {
    doNothing().when(consumerService).validateOrderForConsumer(anyLong(), any(Money.class));

    ValidateOrderRequest request = new ValidateOrderRequest(new Money("12.34"));

    mockMvc.perform(post("/consumers/1/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

    verify(consumerService).validateOrderForConsumer(1L, new Money("12.34"));
  }

  @Test(expected = Exception.class)
  public void shouldThrowWhenConsumerNotFound() throws Exception {
    doThrow(new ConsumerNotFoundException()).when(consumerService).validateOrderForConsumer(anyLong(), any(Money.class));

    ValidateOrderRequest request = new ValidateOrderRequest(new Money("12.34"));

    mockMvc.perform(post("/consumers/999/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
  }

  @Test
  public void shouldGetConsumer() throws Exception {
    when(consumerService.findById(1L)).thenReturn(Optional.of(new Consumer(new PersonName("John", "Doe"))));

    mockMvc.perform(get("/consumers/1"))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldReturnNotFoundForMissingConsumer() throws Exception {
    when(consumerService.findById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/consumers/999"))
            .andExpect(status().isNotFound());
  }
}
