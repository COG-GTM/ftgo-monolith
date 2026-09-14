package net.chrisrichardson.ftgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.GetOrderResponse;
import net.chrisrichardson.ftgo.orderservice.main.OrderServiceMain;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import net.chrisrichardson.ftgo.restaurantservice.web.CreateRestaurantResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CreateOrderFlowIntegrationTest {

  private static ConfigurableApplicationContext orderServiceContext;
  private static ConfigurableApplicationContext applicationContext;
  private static RestTemplate restTemplate;
  private static int orderServicePort;
  private static int applicationPort;

  @BeforeClass
  public static void startApplications() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    restTemplate = new RestTemplateBuilder()
            .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();

    String[] databaseProperties = {
            "spring.datasource.url=jdbc:h2:mem:ftgo;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=update",
            "spring.jpa.generate-ddl=true",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "spring.jmx.enabled=false"
    };

    orderServiceContext = new SpringApplicationBuilder(OrderServiceMain.class)
            .properties(databaseProperties)
            .run(commandLineProperties(databaseProperties, "server.port=0"));
    orderServicePort = orderServiceContext.getEnvironment().getProperty("local.server.port", Integer.class);

    applicationContext = new SpringApplicationBuilder(FtgoApplicationMain.class)
            .properties(databaseProperties)
            .run(commandLineProperties(databaseProperties, "server.port=0",
                    "order.service.url=http://localhost:" + orderServicePort));
    applicationPort = applicationContext.getEnvironment().getProperty("local.server.port", Integer.class);
  }

  @AfterClass
  public static void stopApplications() {
    if (applicationContext != null) {
      applicationContext.close();
    }
    if (orderServiceContext != null) {
      orderServiceContext.close();
    }
  }

  @Test
  public void shouldCreateOrderThroughMonolithAndOrderService() {
    CreateConsumerResponse consumer = restTemplate.postForObject(
            applicationUrl("/consumers"),
            new CreateConsumerRequest(new PersonName("John", "Doe")),
            CreateConsumerResponse.class);
    assertNotNull(consumer);

    CreateRestaurantResponse restaurant = restTemplate.postForObject(
            applicationUrl("/restaurants"),
            new CreateRestaurantRequest("My Restaurant",
                    new Address("1 High Street", null, "Oakland", "CA", "94619"),
                    new RestaurantMenuDTO(Collections.singletonList(
                            new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"))))),
            CreateRestaurantResponse.class);
    assertNotNull(restaurant);

    CreateOrderResponse order = restTemplate.postForObject(
            applicationUrl("/orders"),
            new CreateOrderRequest(consumer.getConsumerId(), restaurant.getId(),
                    Collections.singletonList(new CreateOrderRequest.LineItem("1", 2))),
            CreateOrderResponse.class);
    assertNotNull(order);

    ResponseEntity<GetOrderResponse> applicationOrder = restTemplate.getForEntity(
            applicationUrl("/orders/" + order.getOrderId()), GetOrderResponse.class);
    assertEquals(HttpStatus.OK, applicationOrder.getStatusCode());
    assertEquals(order.getOrderId(), applicationOrder.getBody().getOrderId());
    assertEquals("APPROVED", applicationOrder.getBody().getState());
    assertEquals("24.68", applicationOrder.getBody().getOrderTotal().asString());
    assertEquals("My Restaurant", applicationOrder.getBody().getRestaurantName());

    ResponseEntity<GetOrderResponse> serviceOrder = restTemplate.getForEntity(
            orderServiceUrl("/orders/" + order.getOrderId()), GetOrderResponse.class);
    assertEquals(HttpStatus.OK, serviceOrder.getStatusCode());
    assertEquals(order.getOrderId(), serviceOrder.getBody().getOrderId());
    assertEquals("APPROVED", serviceOrder.getBody().getState());

    ResponseEntity<GetOrderResponse[]> ordersForConsumer = restTemplate.getForEntity(
            applicationUrl("/orders?consumerId=" + consumer.getConsumerId()), GetOrderResponse[].class);
    assertEquals(HttpStatus.OK, ordersForConsumer.getStatusCode());
    assertEquals(1, ordersForConsumer.getBody().length);
    assertEquals(order.getOrderId(), ordersForConsumer.getBody()[0].getOrderId());

    try {
      restTemplate.getForEntity(applicationUrl("/orders/999999"), GetOrderResponse.class);
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
      return;
    }
    throw new AssertionError("Expected GET /orders/999999 to return 404");
  }

  private static String applicationUrl(String path) {
    return "http://localhost:" + applicationPort + path;
  }

  private static String orderServiceUrl(String path) {
    return "http://localhost:" + orderServicePort + path;
  }

  private static String[] commandLineProperties(String[] properties, String... additionalProperties) {
    String[] allProperties = new String[properties.length + additionalProperties.length];
    System.arraycopy(properties, 0, allProperties, 0, properties.length);
    System.arraycopy(additionalProperties, 0, allProperties, properties.length, additionalProperties.length);
    for (int i = 0; i < allProperties.length; i++) {
      allProperties[i] = "--" + (allProperties[i].endsWith("=")
              ? allProperties[i].substring(0, allProperties[i].length() - 1)
              : allProperties[i]);
    }
    return allProperties;
  }
}
