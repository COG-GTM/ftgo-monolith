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
import org.springframework.web.util.NestedServletException;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class ConsumerControllerTest {

  private final ConsumerRepository repository = mock(ConsumerRepository.class);
  private MockMvc mvc;

  @Before
  public void setUp() {
    ConsumerService service = new ConsumerService();
    ReflectionTestUtils.setField(service, "consumerRepository", repository);
    ConsumerController controller = new ConsumerController();
    ReflectionTestUtils.setField(controller, "consumerService", service);
    mvc = standaloneSetup(controller).build();
  }

  @Test
  public void createsAndGetsConsumerWithSameId() throws Exception {
    Consumer saved = new Consumer(new PersonName("John", "Doe"));
    ReflectionTestUtils.setField(saved, "id", 42L);
    when(repository.save(any(Consumer.class))).thenReturn(saved);
    when(repository.findById(42L)).thenReturn(Optional.of(saved));

    mvc.perform(post("/consumers").contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"}}"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"consumerId\":42}", true));
    mvc.perform(get("/consumers/42"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"consumerId\":42,\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"}}", true));
  }

  @Test
  public void returnsNotFoundForMissingConsumer() throws Exception {
    when(repository.findById(999999L)).thenReturn(Optional.empty());
    mvc.perform(get("/consumers/999999")).andExpect(status().isNotFound());
  }

  @Test
  public void rejectsMissingNameWithoutPersisting() throws Exception {
    assertInvalidCreate("{}");
  }

  @Test
  public void rejectsBlankFirstNameWithoutPersisting() throws Exception {
    assertInvalidCreate("{\"name\":{\"firstName\":\"\",\"lastName\":\"X\"}}");
  }

  @Test
  public void rejectsWhitespaceLastNameWithoutPersisting() throws Exception {
    assertInvalidCreate("{\"name\":{\"firstName\":\"John\",\"lastName\":\"  \\t\"}}");
  }

  private void assertInvalidCreate(String requestBody) throws Exception {
    try {
      mvc.perform(post("/consumers").contentType(MediaType.APPLICATION_JSON).content(requestBody));
      fail("Expected invalid consumer name to be rejected");
    } catch (NestedServletException ex) {
      assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }
    verifyZeroInteractions(repository);
  }
}
