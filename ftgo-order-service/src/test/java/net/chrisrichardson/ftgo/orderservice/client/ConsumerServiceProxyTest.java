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
  private MockRestServiceServer server;
  private ConsumerServiceProxy proxy;

  @Before
  public void setUp() {
    restTemplate = new RestTemplate();
    server = MockRestServiceServer.createServer(restTemplate);
    proxy = new ConsumerServiceProxy(BASE_URL, restTemplate);
  }

  @Test
  public void shouldValidateExistingConsumer() {
    server.expect(requestTo(BASE_URL + "/consumers/1"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"consumerId\":1}", MediaType.APPLICATION_JSON));

    proxy.validateOrderForConsumer(1L, new Money("12.34"));

    server.verify();
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowConsumerNotFoundOn404() {
    server.expect(requestTo(BASE_URL + "/consumers/99"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

    proxy.validateOrderForConsumer(99L, new Money("12.34"));
  }

  @Test(expected = ConsumerVerificationFailedException.class)
  public void shouldWrapOtherHttpErrors() {
    server.expect(requestTo(BASE_URL + "/consumers/5"))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    proxy.validateOrderForConsumer(5L, new Money("12.34"));
  }
}
