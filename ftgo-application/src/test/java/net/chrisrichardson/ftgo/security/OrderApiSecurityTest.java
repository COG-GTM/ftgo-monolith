package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.security.FtgoUser;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.web.OrderController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OrderController.class)
@Import(FtgoSecurityConfiguration.class)
@ContextConfiguration(classes = {OrderController.class, FtgoSecurityConfiguration.class})
@TestPropertySource(properties = {
        "ftgo.security.admin.username=admin",
        "ftgo.security.admin.password=admin-password",
        "ftgo.security.users.owner.password={noop}owner-password",
        "ftgo.security.users.owner.consumer-id=1",
        "ftgo.security.users.other.password={noop}other-password",
        "ftgo.security.users.other.consumer-id=2"
})
public class OrderApiSecurityTest {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockBean
  private OrderService orderService;

  @MockBean
  private OrderRepository orderRepository;

  private Order order;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters(context.getBean(FilterChainProxy.class))
            .build();
    Restaurant restaurant = new Restaurant(10L, "Restaurant", new RestaurantMenu(Collections.emptyList()));
    order = new Order(1L, restaurant, Collections.singletonList(new OrderLineItem("1", "Item", new Money("1.00"), 1)));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
  }

  @Test
  public void everyOrderRouteRequiresAuthenticationWithoutBasicChallenge() throws Exception {
    assertUnauthorized("POST", "/orders");
    assertUnauthorized("GET", "/orders?consumerId=1");
    assertUnauthorized("GET", "/orders/1");
    assertUnauthorized("POST", "/orders/1/cancel");
    assertUnauthorized("POST", "/orders/1/revise");
    assertUnauthorized("POST", "/orders/1/accept");
    assertUnauthorized("POST", "/orders/1/preparing");
    assertUnauthorized("POST", "/orders/1/ready");
    assertUnauthorized("POST", "/orders/1/pickedup");
    assertUnauthorized("POST", "/orders/1/delivered");
  }

  @Test
  public void wrongPasswordIsRejected() throws Exception {
    mockMvc.perform(get("/orders/1").with(SecurityMockMvcRequestPostProcessors.httpBasic("owner", "wrong-password")))
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist("WWW-Authenticate"));
  }

  @Test
  public void configuredUsersAreAvailableInTheUserStore() {
    assertTrue(userDetailsService.loadUserByUsername("owner") instanceof FtgoUser);
    assertTrue(passwordEncoder.matches("owner-password", userDetailsService.loadUserByUsername("owner").getPassword()));
    assertTrue(passwordEncoder.matches("admin-password", userDetailsService.loadUserByUsername("admin").getPassword()));
  }

  @Test
  public void nonOwningConsumerCannotViewOrder() throws Exception {
    mockMvc.perform(get("/orders/1").with(SecurityMockMvcRequestPostProcessors.httpBasic("other", "other-password")))
            .andExpect(status().isNotFound());
  }

  @Test
  public void owningConsumerCanViewOrder() throws Exception {
    mockMvc.perform(get("/orders/1").with(SecurityMockMvcRequestPostProcessors.httpBasic("owner", "owner-password")))
            .andExpect(status().isOk());
  }

  @Test
  public void adminCanViewOrder() throws Exception {
    mockMvc.perform(get("/orders/1").with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "admin-password")))
            .andExpect(status().isOk());
  }

  @Test
  public void publicIndexDoesNotRequireAuthentication() throws Exception {
    mockMvc.perform(get("/index.html"))
            .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus()));
  }

  private void assertUnauthorized(String method, String path) throws Exception {
    MockHttpServletRequestBuilder request = request(HttpMethod.valueOf(method), path);
    if (HttpMethod.POST.matches(method)) {
      request.contentType("application/json").content("{}");
    }
    mockMvc.perform(request)
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist("WWW-Authenticate"));
  }
}
