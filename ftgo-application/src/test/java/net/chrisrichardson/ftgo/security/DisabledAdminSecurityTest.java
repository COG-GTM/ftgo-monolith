package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.web.OrderController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OrderController.class)
@Import(FtgoSecurityConfiguration.class)
@ContextConfiguration(classes = {OrderController.class, FtgoSecurityConfiguration.class})
@TestPropertySource(properties = {
        "ftgo.security.admin.username=admin",
        "ftgo.security.admin.password="
})
public class DisabledAdminSecurityTest {

  @Autowired
  private WebApplicationContext context;

  @MockBean
  private OrderService orderService;

  @MockBean
  private OrderRepository orderRepository;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters(context.getBean(FilterChainProxy.class))
            .build();
  }

  @Test
  public void adminIsRejectedWhenPasswordIsUnset() throws Exception {
    assertUnauthorized("");
    assertUnauthorized("any-password");
  }

  private void assertUnauthorized(String password) throws Exception {
    mockMvc.perform(get("/orders/1")
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", password)))
            .andExpect(status().isUnauthorized());
  }
}
