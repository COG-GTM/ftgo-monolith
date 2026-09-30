package net.chrisrichardson.ftgo.orderservice.security;

import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.web.OrderController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real Spring Security filter chain (HTTP Basic + matchers) in front of OrderController.
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = OrderSecurityConfigurationTest.TestConfig.class)
@TestPropertySource(properties = {
        "ftgo.security.users[0].username=ops",
        "ftgo.security.users[0].password=ops-pw",
        "ftgo.security.users[0].roles=OPERATIONS",
        "ftgo.security.users[1].username=alice",
        "ftgo.security.users[1].password=alice-pw",
        "ftgo.security.users[1].roles=CONSUMER",
        "ftgo.security.users[1].consumerId=1511300065921",
        "ftgo.security.users[2].username=bob",
        "ftgo.security.users[2].password=bob-pw",
        "ftgo.security.users[2].roles=CONSUMER",
        "ftgo.security.users[2].consumerId=1511300065922"
})
public class OrderSecurityConfigurationTest {

  @Configuration
  @EnableWebMvc
  @Import(OrderSecurityConfiguration.class)
  static class TestConfig {

    @Bean
    public OrderRepository orderRepository() {
      OrderRepository orderRepository = mock(OrderRepository.class);
      when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));
      return orderRepository;
    }

    @Bean
    public OrderService orderService() {
      return mock(OrderService.class);
    }

    @Bean
    public OrderController orderController(OrderService orderService, OrderRepository orderRepository,
                                           OrderAccessPolicy orderAccessPolicy) {
      return new OrderController(orderService, orderRepository, orderAccessPolicy);
    }
  }

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  public void anonymousGetOrderIsUnauthorized() throws Exception {
    mockMvc.perform(get("/orders/1")).andExpect(status().isUnauthorized());
    mockMvc.perform(get("/orders").param("consumerId", String.valueOf(CONSUMER_ID)))
            .andExpect(status().isUnauthorized());
  }

  @Test
  public void wrongPasswordIsUnauthorized() throws Exception {
    mockMvc.perform(get("/orders/1").with(httpBasic("alice", "nope"))).andExpect(status().isUnauthorized());
  }

  @Test
  public void ownerCanReadOrder() throws Exception {
    mockMvc.perform(get("/orders/1").with(httpBasic("alice", "alice-pw"))).andExpect(status().isOk());
    mockMvc.perform(get("/orders").with(httpBasic("alice", "alice-pw"))).andExpect(status().isOk());
  }

  @Test
  public void otherConsumerIsForbidden() throws Exception {
    mockMvc.perform(get("/orders/1").with(httpBasic("bob", "bob-pw"))).andExpect(status().isForbidden());
    mockMvc.perform(get("/orders").param("consumerId", String.valueOf(CONSUMER_ID)).with(httpBasic("bob", "bob-pw")))
            .andExpect(status().isForbidden());
  }

  @Test
  public void operationsCanReadAnyOrder() throws Exception {
    mockMvc.perform(get("/orders/1").with(httpBasic("ops", "ops-pw"))).andExpect(status().isOk());
    mockMvc.perform(get("/orders").param("consumerId", String.valueOf(CONSUMER_ID)).with(httpBasic("ops", "ops-pw")))
            .andExpect(status().isOk());
    mockMvc.perform(get("/orders").with(httpBasic("ops", "ops-pw"))).andExpect(status().isBadRequest());
  }

  @Test
  public void statusTransitionsRequireOperationsRole() throws Exception {
    mockMvc.perform(post("/orders/1/accept").contentType("application/json").content("{\"readyBy\":\"2026-10-01T10:00:00\"}")
            .with(httpBasic("alice", "alice-pw")))
            .andExpect(status().isForbidden());
    mockMvc.perform(post("/orders/1/accept").contentType("application/json").content("{\"readyBy\":\"2026-10-01T10:00:00\"}")
            .with(httpBasic("ops", "ops-pw")))
            .andExpect(status().isOk());
  }
}
