package net.chrisrichardson.ftgo.orderservice.domain.proxy;

import net.chrisrichardson.ftgo.common.Money;
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

public class ConsumerServiceHttpProxyTest {

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private ConsumerServiceHttpProxy proxy;

  @Before
  public void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    proxy = new ConsumerServiceHttpProxy(restTemplate, "http://consumer-service:8082");
  }

  @Test
  public void shouldValidateOrderForConsumerSuccessfully() {
    mockServer.expect(requestTo("http://consumer-service:8082/consumers/1/validate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess());

    proxy.validateOrderForConsumer(1L, new Money("10.00"));

    mockServer.verify();
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowConsumerNotFoundWhen404() {
    mockServer.expect(requestTo("http://consumer-service:8082/consumers/99/validate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON));

    proxy.validateOrderForConsumer(99L, new Money("10.00"));
  }

  @Test(expected = ConsumerVerificationFailedException.class)
  public void shouldThrowVerificationFailedOnClientError() {
    mockServer.expect(requestTo("http://consumer-service:8082/consumers/1/validate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY).contentType(MediaType.APPLICATION_JSON));

    proxy.validateOrderForConsumer(1L, new Money("10.00"));
  }
}
