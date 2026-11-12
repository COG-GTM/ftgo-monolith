package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.GlobalExceptionHandler;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.consumerservice.web.ConsumerController;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.web.RestaurantController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FtgoSecurityConfigurationTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ftgo.security.users[0].username=operator",
        "ftgo.security.users[0].password-hash=$2a$04$MujfuO4VqzMgeLfEbTg92uqwnrFuXaVu3tpnZ.l4jMnJJTaG1.Cya",
        "ftgo.security.users[0].role=OPERATOR",
        "ftgo.security.users[1].username=owner",
        "ftgo.security.users[1].password-hash=$2a$04$MujfuO4VqzMgeLfEbTg92uqwnrFuXaVu3tpnZ.l4jMnJJTaG1.Cya",
        "ftgo.security.users[1].role=RESTAURANT_OWNER",
        "ftgo.security.users[2].username=consumer5",
        "ftgo.security.users[2].password-hash=$2a$04$MujfuO4VqzMgeLfEbTg92uqwnrFuXaVu3tpnZ.l4jMnJJTaG1.Cya",
        "ftgo.security.users[2].role=CONSUMER",
        "ftgo.security.users[2].consumer-id=5",
        "ftgo.security.users[3].username=consumer6",
        "ftgo.security.users[3].password-hash=$2a$04$MujfuO4VqzMgeLfEbTg92uqwnrFuXaVu3tpnZ.l4jMnJJTaG1.Cya",
        "ftgo.security.users[3].role=CONSUMER",
        "ftgo.security.users[3].consumer-id=6"
})
public class FtgoSecurityConfigurationTest {

  private static final String PASSWORD = "test-password";
  private static final String VALID_MENU = menu(item("item-1", "Meal", "1.50"));
  private static final String VALID_ADDRESS = address(null, null);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FtgoUserDetailsService userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockBean
  private ConsumerService consumerService;

  @MockBean
  private RestaurantService restaurantService;

  @Before
  public void setUp() {
    reset(consumerService, restaurantService);
    when(consumerService.findById(5L)).thenReturn(Optional.of(new Consumer(new PersonName("Alice", "Consumer"))));
    Consumer createdConsumer = org.mockito.Mockito.mock(Consumer.class);
    when(createdConsumer.getId()).thenReturn(1L);
    when(consumerService.create(any())).thenReturn(createdConsumer);

    Restaurant restaurant = org.mockito.Mockito.mock(Restaurant.class);
    when(restaurant.getId()).thenReturn(1L);
    when(restaurant.getName()).thenReturn("Diner");
    when(restaurantService.findById(1L)).thenReturn(Optional.of(restaurant));
    when(restaurantService.create(any())).thenReturn(restaurant);
  }

  @Test
  public void configuredUserPasswordsAreStoredAsHashes() {
    String hash = userDetailsService.loadUserByUsername("operator").getPassword();
    assertEquals("$2a$04$MujfuO4VqzMgeLfEbTg92uqwnrFuXaVu3tpnZ.l4jMnJJTaG1.Cya", hash);
    assertTrue(passwordEncoder.matches(PASSWORD, hash));
  }

  @Test
  public void consumerRecordIsPrivateToConsumerAndOperator() throws Exception {
    mockMvc.perform(get("/consumers/5"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", containsString("ftgo")));
    mockMvc.perform(get("/consumers/5").with(httpBasic("consumer5", "wrong-password")))
            .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/consumers/5").with(httpBasic("consumer6", PASSWORD)))
            .andExpect(status().isForbidden());
    mockMvc.perform(get("/consumers/5").with(httpBasic("consumer5", PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName").value("Alice"));
    mockMvc.perform(get("/consumers/5").with(httpBasic("operator", PASSWORD)))
            .andExpect(status().isOk());
    mockMvc.perform(get("/consumers/5").with(httpBasic("owner", PASSWORD)))
            .andExpect(status().isForbidden());
  }

  @Test
  public void consumerRulesCoverTrailingSlashAndInvalidIds() throws Exception {
    mockMvc.perform(get("/consumers/5/").with(httpBasic("consumer6", PASSWORD)))
            .andExpect(status().isForbidden());
    mockMvc.perform(get("/consumers/5/"))
            .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/consumers/abc").with(httpBasic("consumer5", PASSWORD)))
            .andExpect(status().isForbidden());
  }

  @Test
  public void onlyOperatorCanCreateConsumers() throws Exception {
    String request = "{\"name\":{\"firstName\":\"Jane\",\"lastName\":\"Doe\"}}";
    mockMvc.perform(post("/consumers").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isUnauthorized());
    mockMvc.perform(post("/consumers/").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isUnauthorized());
    mockMvc.perform(post("/consumers").with(httpBasic("consumer5", PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isForbidden());
    mockMvc.perform(post("/consumers").with(httpBasic("operator", PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk());
  }

  @Test
  public void restaurantCreationRequiresOperatorOrOwner() throws Exception {
    String request = restaurantBody("\"Diner\"", VALID_MENU, VALID_ADDRESS);
    mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isUnauthorized());
    mockMvc.perform(post("/restaurants").with(httpBasic("consumer5", PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isForbidden());
    mockMvc.perform(post("/restaurants").with(httpBasic("owner", PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk());
    mockMvc.perform(post("/restaurants").with(httpBasic("operator", PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk());
    mockMvc.perform(post("/restaurants/").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isUnauthorized());
  }

  @Test
  public void otherRestaurantRoutesRemainPublic() throws Exception {
    mockMvc.perform(get("/restaurants/1"))
            .andExpect(status().isOk());
  }

  @Test
  public void invalidRestaurantBodiesReturnBadRequestWithoutCreatingRestaurant() throws Exception {
    String validItem = item("item-1", "Meal", "1.50");
    String duplicateItem = item("item-1", "Another meal", "2.00");
    String[] invalidBodies = {
            restaurantBody("\"   \"", VALID_MENU, VALID_ADDRESS),
            restaurantBody("\"Diner\"", null, VALID_ADDRESS),
            restaurantBody("\"Diner\"", menu(), VALID_ADDRESS),
            restaurantBody("\"Diner\"", menu(validItem, duplicateItem), VALID_ADDRESS),
            restaurantBody("\"Diner\"", menu(item(" ", "Meal", "1.50")), VALID_ADDRESS),
            restaurantBody("\"Diner\"", menu(item("item-1", " ", "1.50")), VALID_ADDRESS),
            restaurantBody("\"Diner\"", menu(item("item-1", "Meal", null)), VALID_ADDRESS),
            restaurantBody("\"Diner\"", menu(item("item-1", "Meal", "-1")), VALID_ADDRESS),
            restaurantBody("\"Diner\"", menu(item("item-1", "Meal", "1.234")), VALID_ADDRESS),
            restaurantBody("\"Diner\"", VALID_MENU, address("91", null)),
            restaurantBody("\"Diner\"", VALID_MENU, address(null, "-181")),
            restaurantBody("\"Diner\"", VALID_MENU, null),
            "{ invalid json"
    };

    for (String body : invalidBodies) {
      mockMvc.perform(post("/restaurants").with(httpBasic("operator", PASSWORD))
                      .contentType(MediaType.APPLICATION_JSON).content(body))
              .andExpect(status().isBadRequest());
    }
    verify(restaurantService, org.mockito.Mockito.never()).create(any());
  }

  @Test
  public void validRestaurantPriceWithTrailingZeroIsAccepted() throws Exception {
    mockMvc.perform(post("/restaurants").with(httpBasic("operator", PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(restaurantBody("\"Diner\"", VALID_MENU, VALID_ADDRESS)))
            .andExpect(status().isOk());
    verify(restaurantService).create(any());
  }

  @Configuration
  @EnableAutoConfiguration(exclude = {
          DataSourceAutoConfiguration.class,
          DataSourceTransactionManagerAutoConfiguration.class,
          HibernateJpaAutoConfiguration.class
  })
  @Import({
          FtgoSecurityConfiguration.class,
          ConsumerAccess.class,
          FtgoUserDetailsService.class,
          ConsumerController.class,
          RestaurantController.class,
          GlobalExceptionHandler.class,
          MoneyJsonConfiguration.class
  })
  public static class Config {
  }

  @Configuration
  public static class MoneyJsonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new MoneyModule());
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      return objectMapper;
    }
  }

  private static String restaurantBody(String name, String menu, String address) {
    return "{\"name\":" + name +
            (menu == null ? "" : ",\"menu\":" + menu) +
            (address == null ? "" : ",\"address\":" + address) + "}";
  }

  private static String menu(String... items) {
    return "{\"menuItemDTOs\":[" + String.join(",", items) + "]}";
  }

  private static String item(String id, String name, String price) {
    return "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"price\":" +
            (price == null ? "null" : "\"" + price + "\"") + "}";
  }

  private static String address(String latitude, String longitude) {
    return "{\"street1\":\"1 Main Street\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94619\"" +
            (latitude == null ? "" : ",\"latitude\":" + latitude) +
            (longitude == null ? "" : ",\"longitude\":" + longitude) + "}";
  }
}
