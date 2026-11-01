package net.chrisrichardson.ftgo.consumerservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.lang.reflect.Field;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerControllerTest {

  @Mock
  private ConsumerService consumerService;

  @InjectMocks
  private ConsumerController consumerController;

  @Test
  public void shouldCreateConsumer() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    setConsumerId(consumer, 1L);
    when(consumerService.create(any(PersonName.class))).thenReturn(consumer);

    given().
            standaloneSetup(configureControllers(consumerController)).
            contentType("application/json").
            body("{\"name\": {\"firstName\": \"John\", \"lastName\": \"Doe\"}}").
    when().
            post("/consumers").
    then().
            statusCode(200).
            body("consumerId", equalTo(1));
  }

  @Test
  public void shouldGetConsumerById() {
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerService.findById(1L)).thenReturn(Optional.of(consumer));

    given().
            standaloneSetup(configureControllers(consumerController)).
    when().
            get("/consumers/1").
    then().
            statusCode(200).
            body("name.firstName", equalTo("John")).
            body("name.lastName", equalTo("Doe"));
  }

  @Test
  public void shouldReturn404WhenConsumerNotFound() {
    when(consumerService.findById(999L)).thenReturn(Optional.empty());

    given().
            standaloneSetup(configureControllers(consumerController)).
    when().
            get("/consumers/999").
    then().
            statusCode(404);
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

  private void setConsumerId(Consumer consumer, long id) {
    try {
      Field idField = Consumer.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(consumer, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
