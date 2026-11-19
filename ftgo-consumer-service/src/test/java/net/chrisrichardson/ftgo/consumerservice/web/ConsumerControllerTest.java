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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConsumerControllerTest {

  private static final long CONSUMER_ID = 42L;

  private final ConsumerService consumerService = mock(ConsumerService.class);
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    ConsumerController controller = new ConsumerController();
    ReflectionTestUtils.setField(controller, "consumerService", consumerService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void shouldReturnMatchingConsumerIdAndNameAfterCreate() throws Exception {
    String name = "{\"firstName\":\"Ada\",\"lastName\":\"Example\"}";
    assertCreateThenGet("{\"name\":" + name + "}", name);
  }

  @Test
  public void shouldAcceptCreateWithoutNameAndReturnNullName() throws Exception {
    assertCreateThenGet("{}", "null");
  }

  @Test
  public void shouldReturnAnEmpty404ForMissingConsumer() throws Exception {
    long missingId = 99L;
    when(consumerService.findById(missingId)).thenReturn(Optional.empty());

    mockMvc.perform(get("/consumers/{consumerId}", missingId))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
  }

  private void assertCreateThenGet(String requestJson, String expectedNameJson) throws Exception {
    when(consumerService.create(any())).thenAnswer(invocation -> {
      PersonName name = invocation.getArgument(0);
      Consumer consumer = new Consumer(name);
      ReflectionTestUtils.setField(consumer, "id", CONSUMER_ID);
      when(consumerService.findById(CONSUMER_ID)).thenReturn(Optional.of(consumer));
      return consumer;
    });

    String createResponse = mockMvc.perform(post("/consumers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"consumerId\":" + CONSUMER_ID + "}", true))
            .andReturn().getResponse().getContentAsString();
    long createdId = objectMapper.readTree(createResponse).get("consumerId").longValue();

    mockMvc.perform(get("/consumers/{consumerId}", createdId))
            .andExpect(status().isOk())
            .andExpect(content().json(
                    "{\"consumerId\":" + createdId + ",\"name\":" + expectedNameJson + "}", true));
  }
}
