package net.chrisrichardson.ftgo.consumerservice.web;

import io.restassured.RestAssured;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.ConsumerServiceApplication;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import net.chrisrichardson.ftgo.consumerservice.domain.Consumer;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsumerServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConsumerValidationControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @MockBean
  private ConsumerRepository consumerRepository;

  @Before
  public void setUp() {
    RestAssured.port = port;
  }

  @Test
  public void shouldValidateOrderForExistingConsumer() {
    long consumerId = 1L;
    Consumer consumer = new Consumer(new PersonName("John", "Doe"));
    when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(consumer));

    RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(new ValidateOrderRequest(new Money("19.99")))
            .when()
            .post("/consumers/" + consumerId + "/validate")
            .then()
            .statusCode(200);

    verify(consumerRepository).findById(eq(consumerId));
  }
}
