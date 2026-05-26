package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class HttpConsumerServiceProxyTest {

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private HttpConsumerServiceProxy proxy;

  @Before
  public void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    proxy = new HttpConsumerServiceProxy(restTemplate, "http://localhost:8082");
  }

  @Test
  public void shouldValidateOrderForConsumer() {
    mockServer.expect(requestTo("http://localhost:8082/consumers/1/validate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess());

    proxy.validateOrderForConsumer(1L, new Money("12.34"));

    mockServer.verify();
  }

  @Test(expected = Exception.class)
  public void shouldThrowExceptionWhenConsumerServiceReturnsError() {
    mockServer.expect(requestTo("http://localhost:8082/consumers/999/validate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

    proxy.validateOrderForConsumer(999L, new Money("12.34"));
  }
}
