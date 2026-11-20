package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ConsumerControllerTest {
  private ConsumerRepository repository;
  private MockMvc mvc;

  @Before
  public void setUp() {
    repository = mock(ConsumerRepository.class);
    ConsumerService service = new ConsumerService();
    ReflectionTestUtils.setField(service, "consumerRepository", repository);
    ConsumerController controller = new ConsumerController();
    ReflectionTestUtils.setField(controller, "consumerService", service);
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void shouldCreateAndGetConsumer() throws Exception {
    when(repository.save(any(Consumer.class))).thenAnswer(invocation -> {
      Consumer consumer = invocation.getArgument(0);
      ReflectionTestUtils.setField(consumer, "id", 42L);
      when(repository.findById(42L)).thenReturn(Optional.of(consumer));
      return consumer;
    });

    mvc.perform(post("/consumers").contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":{\"firstName\":\"Jane\",\"lastName\":\"Doe\"}}"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"consumerId\":42}", true));
    mvc.perform(get("/consumers/42"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"consumerId\":42,\"name\":{\"firstName\":\"Jane\",\"lastName\":\"Doe\"}}", true));
  }

  @Test
  public void shouldReturn404ForUnknownConsumer() throws Exception {
    when(repository.findById(999999L)).thenReturn(Optional.empty());
    mvc.perform(get("/consumers/999999"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
  }

  @Test
  public void shouldRejectMissingName() throws Exception {
    assertInvalid("{}", "name is required");
    assertInvalid("{\"name\":null}", "name is required");
  }

  @Test
  public void shouldRejectMissingOrBlankFirstName() throws Exception {
    assertInvalid("{\"name\":{\"lastName\":\"Doe\"}}", "name.firstName must not be blank");
    for (String value : new String[]{"null", "\"\"", "\" \\t\\n\""}) {
      assertInvalid("{\"name\":{\"firstName\":" + value + ",\"lastName\":\"Doe\"}}",
              "name.firstName must not be blank");
    }
  }

  @Test
  public void shouldRejectMissingOrBlankLastName() throws Exception {
    assertInvalid("{\"name\":{\"firstName\":\"Jane\"}}", "name.lastName must not be blank");
    for (String value : new String[]{"null", "\"\"", "\" \\t\\n\""}) {
      assertInvalid("{\"name\":{\"firstName\":\"Jane\",\"lastName\":" + value + "}}",
              "name.lastName must not be blank");
    }
  }

  @Test
  public void shouldPopulateResponseIdAndName() {
    PersonName name = new PersonName("Jane", "Doe");
    GetConsumerResponse response = new GetConsumerResponse(42L, name);
    assertEquals(42L, response.getConsumerId());
    assertSame(name, response.getName());
  }

  private void assertInvalid(String body, String message) throws Exception {
    try {
      mvc.perform(post("/consumers").contentType(MediaType.APPLICATION_JSON).content(body));
      fail("Expected IllegalArgumentException for " + body);
    } catch (NestedServletException ex) {
      assertTrue(ex.getCause() instanceof IllegalArgumentException);
      assertEquals(message, ex.getCause().getMessage());
    }
    verifyZeroInteractions(repository);
  }
}
