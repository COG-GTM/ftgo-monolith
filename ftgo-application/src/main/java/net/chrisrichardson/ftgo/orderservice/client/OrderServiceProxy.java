package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.orderservice.api.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class OrderServiceProxy {

  private static final Logger logger = LoggerFactory.getLogger(OrderServiceProxy.class);

  private final RestTemplate restTemplate;
  private final String orderServiceUrl;

  public OrderServiceProxy(RestTemplate restTemplate, String orderServiceUrl) {
    this.restTemplate = restTemplate;
    this.orderServiceUrl = orderServiceUrl;
  }

  public CreateOrderResponse createOrder(CreateOrderRequest request) {
    logger.debug("Proxying createOrder request to {}", orderServiceUrl);
    ResponseEntity<CreateOrderResponse> response = restTemplate.postForEntity(
            orderServiceUrl + "/orders", request, CreateOrderResponse.class);
    return response.getBody();
  }

  public Map getOrder(long orderId) {
    logger.debug("Proxying getOrder request for orderId={} to {}", orderId, orderServiceUrl);
    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(
              orderServiceUrl + "/orders/{orderId}", Map.class, orderId);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new OrderNotFoundException(orderId);
      }
      throw e;
    }
  }

  public List getOrders(long consumerId) {
    logger.debug("Proxying getOrders request for consumerId={} to {}", consumerId, orderServiceUrl);
    ResponseEntity<List> response = restTemplate.getForEntity(
            orderServiceUrl + "/orders?consumerId={consumerId}", List.class, consumerId);
    return response.getBody();
  }

  public Map cancelOrder(long orderId) {
    logger.debug("Proxying cancelOrder request for orderId={} to {}", orderId, orderServiceUrl);
    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(
              orderServiceUrl + "/orders/{orderId}/cancel", "{}", Map.class, orderId);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new OrderNotFoundException(orderId);
      }
      throw e;
    }
  }

  public Map reviseOrder(long orderId, ReviseOrderRequest request) {
    logger.debug("Proxying reviseOrder request for orderId={} to {}", orderId, orderServiceUrl);
    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(
              orderServiceUrl + "/orders/{orderId}/revise", request, Map.class, orderId);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new OrderNotFoundException(orderId);
      }
      throw e;
    }
  }

  public void acceptOrder(long orderId, OrderAcceptance orderAcceptance) {
    logger.debug("Proxying acceptOrder request for orderId={} to {}", orderId, orderServiceUrl);
    try {
      restTemplate.postForEntity(
              orderServiceUrl + "/orders/{orderId}/accept", orderAcceptance, String.class, orderId);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new OrderNotFoundException(orderId);
      }
      throw e;
    }
  }

  public void notePreparing(long orderId) {
    logger.debug("Proxying notePreparing request for orderId={} to {}", orderId, orderServiceUrl);
    try {
      restTemplate.postForEntity(
              orderServiceUrl + "/orders/{orderId}/preparing", null, String.class, orderId);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new OrderNotFoundException(orderId);
      }
      throw e;
    }
  }

  public void noteReadyForPickup(long orderId) {
    logger.debug("Proxying noteReadyForPickup request for orderId={} to {}", orderId, orderServiceUrl);
    try {
      restTemplate.postForEntity(
              orderServiceUrl + "/orders/{orderId}/ready", null, String.class, orderId);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new OrderNotFoundException(orderId);
      }
      throw e;
    }
  }

  public void notePickedUp(long orderId) {
    logger.debug("Proxying notePickedUp request for orderId={} to {}", orderId, orderServiceUrl);
    try {
      restTemplate.postForEntity(
              orderServiceUrl + "/orders/{orderId}/pickedup", null, String.class, orderId);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new OrderNotFoundException(orderId);
      }
      throw e;
    }
  }

  public void noteDelivered(long orderId) {
    logger.debug("Proxying noteDelivered request for orderId={} to {}", orderId, orderServiceUrl);
    try {
      restTemplate.postForEntity(
              orderServiceUrl + "/orders/{orderId}/delivered", null, String.class, orderId);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new OrderNotFoundException(orderId);
      }
      throw e;
    }
  }
}
