package net.chrisrichardson.ftgo.consumerservice.web;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * MockMvc tests for {@link ConsumerController} (UNIT_TEST_PLAN.md section 4.2).
 * Uses REST Assured's standalone MockMvc support so only the controller and Spring MVC's
 * default Jackson converter are exercised; {@link ConsumerService} is a Mockito mock injected
 * into the controller's {@code @Autowired} field.
 */
@ExtendWith(MockitoExtension.class)
public class ConsumerControllerTest {

  private static final long CONSUMER_ID = 101L;
  private static final PersonName CONSUMER_NAME = new PersonName("Chris", "Richardson");

  @Mock
  private ConsumerService consumerService;

  @Mock
  private Consumer consumer;

  @InjectMocks
  private ConsumerController consumerController;

  @BeforeEach
  public void setUp() {
    // Reset REST Assured's static MockMvc state so tests do not leak configuration into each other.
    RestAssuredMockMvc.reset();
  }

  @Test
  public void shouldCreateConsumer() {
    // Consumer is mocked because its id is normally assigned by JPA and has no setter.
    when(consumer.getId()).thenReturn(CONSUMER_ID);
    when(consumerService.create(any(PersonName.class))).thenReturn(consumer);

    given().
            standaloneSetup(MockMvcBuilders.standaloneSetup(consumerController)).
            contentType(MediaType.APPLICATION_JSON_VALUE).
            body(new CreateConsumerRequest(CONSUMER_NAME)).
    when().
            post("/consumers").
    then().
            statusCode(200).
            body("consumerId", equalTo((int) CONSUMER_ID));
  }

  @Test
  public void shouldGetConsumer() {
    when(consumerService.findById(CONSUMER_ID)).thenReturn(Optional.of(new Consumer(CONSUMER_NAME)));

    given().
            standaloneSetup(MockMvcBuilders.standaloneSetup(consumerController)).
    when().
            get("/consumers/" + CONSUMER_ID).
    then().
            statusCode(200).
            body("name.firstName", equalTo(CONSUMER_NAME.getFirstName())).
            body("name.lastName", equalTo(CONSUMER_NAME.getLastName()));
  }

  @Test
  public void shouldReturn404WhenConsumerNotFound() {
    when(consumerService.findById(CONSUMER_ID)).thenReturn(Optional.empty());

    given().
            standaloneSetup(MockMvcBuilders.standaloneSetup(consumerController)).
    when().
            get("/consumers/" + CONSUMER_ID).
    then().
            statusCode(404);
  }
}
