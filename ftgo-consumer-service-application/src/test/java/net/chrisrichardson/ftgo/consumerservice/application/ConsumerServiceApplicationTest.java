package net.chrisrichardson.ftgo.consumerservice.application;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsumerServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConsumerServiceApplicationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void shouldCreateAndRetrieveConsumer() {
    CreateConsumerRequest request = new CreateConsumerRequest(new PersonName("John", "Doe"));

    ResponseEntity<CreateConsumerResponse> createResponse =
            restTemplate.postForEntity("/consumers", request, CreateConsumerResponse.class);

    assertEquals(HttpStatus.OK, createResponse.getStatusCode());
    assertNotNull(createResponse.getBody());
    long consumerId = createResponse.getBody().getConsumerId();
    assertTrue("expected a generated consumer id", consumerId > 0);

    ResponseEntity<String> getResponse =
            restTemplate.getForEntity("/consumers/" + consumerId, String.class);

    assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    assertTrue(getResponse.getBody().contains("John"));
    assertTrue(getResponse.getBody().contains("Doe"));
  }

  @Test
  public void shouldReturn404ForUnknownConsumer() {
    ResponseEntity<String> getResponse =
            restTemplate.getForEntity("/consumers/999999", String.class);

    assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
  }
}
