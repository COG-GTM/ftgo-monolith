package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.web.ConsumerVerificationFailedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsumerServiceProxyTest {

  private RestTemplate restTemplate;
  private ConsumerServiceProxy proxy;

  @Before
  public void setUp() {
    restTemplate = mock(RestTemplate.class);
    proxy = new ConsumerServiceProxy(restTemplate, "http://localhost:8082");
  }

  @Test
  public void shouldValidateWhenConsumerExists() {
    when(restTemplate.getForEntity(eq("http://localhost:8082/consumers/1"), any()))
            .thenReturn(ResponseEntity.ok("{}"));

    proxy.validateOrderForConsumer(1L, new Money("12.34"));
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowNotFoundOn404() {
    when(restTemplate.getForEntity(eq("http://localhost:8082/consumers/2"), any()))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    proxy.validateOrderForConsumer(2L, new Money("12.34"));
  }

  @Test(expected = ConsumerVerificationFailedException.class)
  public void shouldWrapOtherHttpErrors() {
    when(restTemplate.getForEntity(eq("http://localhost:8082/consumers/3"), any()))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    proxy.validateOrderForConsumer(3L, new Money("12.34"));
  }

  @Test(expected = ConsumerVerificationFailedException.class)
  public void shouldWrapConnectionErrors() {
    when(restTemplate.getForEntity(eq("http://localhost:8082/consumers/4"), any()))
            .thenThrow(new ResourceAccessException("connect timed out", new IOException()));

    proxy.validateOrderForConsumer(4L, new Money("12.34"));
  }
}
