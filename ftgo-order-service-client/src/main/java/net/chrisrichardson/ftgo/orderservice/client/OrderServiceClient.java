package net.chrisrichardson.ftgo.orderservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.GetOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OrderServiceClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public OrderServiceClient(RestTemplate restTemplate, String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  public CreateOrderResponse createOrder(CreateOrderRequest request) {
    return execute(() -> restTemplate.postForObject(url("/orders"), request, CreateOrderResponse.class));
  }

  public Optional<GetOrderResponse> getOrder(long orderId) {
    return executeOptional(() -> restTemplate.getForObject(url("/orders/" + orderId), GetOrderResponse.class));
  }

  public List<GetOrderResponse> getOrdersForConsumer(long consumerId) {
    GetOrderResponse[] orders = execute(() ->
            restTemplate.getForObject(url("/orders?consumerId=" + consumerId), GetOrderResponse[].class));
    return Arrays.asList(orders);
  }

  public Optional<GetOrderResponse> cancel(long orderId) {
    return executeOptional(() -> restTemplate.postForObject(url("/orders/" + orderId + "/cancel"),
            null, GetOrderResponse.class));
  }

  public Optional<GetOrderResponse> revise(long orderId, ReviseOrderRequest request) {
    return executeOptional(() -> restTemplate.postForObject(url("/orders/" + orderId + "/revise"),
            request, GetOrderResponse.class));
  }

  public void accept(long orderId, OrderAcceptance orderAcceptance) {
    post(url("/orders/" + orderId + "/accept"), orderAcceptance);
  }

  public void notePreparing(long orderId) {
    post(url("/orders/" + orderId + "/preparing"), null);
  }

  public void noteReadyForPickup(long orderId) {
    post(url("/orders/" + orderId + "/ready"), null);
  }

  public void notePickedUp(long orderId) {
    post(url("/orders/" + orderId + "/pickedup"), null);
  }

  public void noteDelivered(long orderId) {
    post(url("/orders/" + orderId + "/delivered"), null);
  }

  private void post(String path, Object request) {
    execute(() -> {
      restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(request), String.class);
      return null;
    });
  }

  private <T> T execute(Request<T> request) {
    try {
      return request.execute();
    } catch (HttpStatusCodeException e) {
      throw orderServiceException(e);
    } catch (ResourceAccessException e) {
      throw unavailable(e);
    }
  }

  private <T> Optional<T> executeOptional(Request<T> request) {
    try {
      return Optional.ofNullable(request.execute());
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw orderServiceException(e);
    } catch (HttpStatusCodeException e) {
      throw orderServiceException(e);
    } catch (ResourceAccessException e) {
      throw unavailable(e);
    }
  }

  private OrderServiceException orderServiceException(HttpStatusCodeException e) {
    String body = e.getResponseBodyAsString();
    ErrorResponse errorResponse = parseErrorResponse(body);
    return new OrderServiceException(e.getStatusCode(), errorResponse, e.getMessage(), e);
  }

  private OrderServiceException unavailable(ResourceAccessException e) {
    return new OrderServiceException(HttpStatus.SERVICE_UNAVAILABLE, null, e.getMessage(), e);
  }

  private ErrorResponse parseErrorResponse(String body) {
    if (body == null || body.trim().isEmpty()) {
      return null;
    }
    for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
      if (converter instanceof MappingJackson2HttpMessageConverter) {
        ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) converter).getObjectMapper();
        try {
          return objectMapper.readValue(body, ErrorResponse.class);
        } catch (IOException ignored) {
          return null;
        }
      }
    }
    return null;
  }

  private String url(String path) {
    return baseUrl + path;
  }

  private interface Request<T> {
    T execute();
  }
}
