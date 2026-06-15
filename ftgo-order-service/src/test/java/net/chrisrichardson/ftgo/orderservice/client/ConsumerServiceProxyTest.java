package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerVerificationFailedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class ConsumerServiceProxyTest {

  private static final String BASE_URL = "http://localhost:8082";

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private ConsumerServiceProxy proxy;

  @Before
  public void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    proxy = new ConsumerServiceProxy(restTemplate, BASE_URL);
  }

  @Test
  public void shouldValidateExistingConsumer() {
    mockServer.expect(requestTo(BASE_URL + "/consumers/1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"name\":{\"firstName\":\"a\",\"lastName\":\"b\"}}", MediaType.APPLICATION_JSON));

    proxy.validateOrderForConsumer(1L, new Money("12.34"));

    mockServer.verify();
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowConsumerNotFoundWhenConsumerMissing() {
    mockServer.expect(requestTo(BASE_URL + "/consumers/99"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    proxy.validateOrderForConsumer(99L, new Money("1.00"));
  }

  @Test(expected = ConsumerVerificationFailedException.class)
  public void shouldThrowVerificationFailedOnServerError() {
    mockServer.expect(requestTo(BASE_URL + "/consumers/2"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    proxy.validateOrderForConsumer(2L, new Money("1.00"));
  }
}
