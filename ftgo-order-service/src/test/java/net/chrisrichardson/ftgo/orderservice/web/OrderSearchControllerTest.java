package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Order;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderSearchControllerTest {

  private EntityManager entityManager;
  private OrderSearchController controller;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    entityManager = mock(EntityManager.class);
    TypedQuery<Order> query = mock(TypedQuery.class);
    when(entityManager.createQuery(anyString(), eq(Order.class))).thenReturn(query);
    when(query.getResultList()).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));
    controller = new OrderSearchController(entityManager);
  }

  @Test
  public void shouldSearchOrdersForConsumer() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());

    given().
            standaloneSetup(MockMvcBuilders.standaloneSetup(controller)
                    .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))).
    when().
            get("/orders/search?consumerId=1511300065921&sortBy=id").
    then().
            statusCode(200).
            body("[0].orderTotal", equalTo(CHICKEN_VINDALOO_ORDER_TOTAL.asString()));
  }
}
