package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrderApiDocsTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderApiDocsTest {

  @Configuration
  @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class,
          ManagementContextAutoConfiguration.class})
  @Import({CommonSwaggerConfiguration.class, OrderController.class})
  public static class Config {
  }

  @MockBean
  private OrderService orderService;

  @MockBean
  private OrderRepository orderRepository;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void shouldDocumentOrderEndpointsWithExamples() {
    ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    DocumentContext spec = JsonPath.parse(response.getBody());
    assertEquals("3.0.1", spec.read("$.openapi"));
    assertEquals("FTGO Application API", spec.read("$.info.title"));
    assertEquals("Orders", spec.read("$.paths['/orders'].post.tags[0]"));
    assertEquals(Integer.valueOf(1), spec.read("$.paths['/orders'].post.requestBody.content['application/json'].examples['Two line items'].value.restaurantId"));
    assertEquals(Integer.valueOf(1), spec.read("$.paths['/orders'].post.responses['200'].content['application/json'].examples['Created'].value.orderId"));
    assertEquals("65.20", spec.read("$.paths['/orders/{orderId}'].get.responses['200'].content['application/json'].examples['Accepted order with courier'].value.orderTotal"));
    assertEquals(Integer.valueOf(503), spec.read("$.paths['/orders/{orderId}/accept'].post.responses['503'].content['application/json'].examples['No courier'].value.status"));
    assertEquals("string", spec.read("$.components.schemas.GetOrderResponse.properties.orderTotal.type"));
    Map<String, Object> paths = spec.read("$.paths");
    assertTrue(paths.keySet().containsAll(Arrays.asList("/orders", "/orders/{orderId}", "/orders/{orderId}/revise",
            "/orders/{orderId}/cancel", "/orders/{orderId}/accept", "/orders/{orderId}/delivered")));
  }

  @Test
  public void shouldServeSwaggerUi() {
    ResponseEntity<String> response = restTemplate.getForEntity("/swagger-ui/index.html", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("swagger-ui"));
  }
}
