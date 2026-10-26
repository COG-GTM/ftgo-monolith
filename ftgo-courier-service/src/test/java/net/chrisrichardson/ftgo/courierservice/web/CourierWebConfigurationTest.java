package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = CourierWebConfigurationTest.Config.class)
public class CourierWebConfigurationTest {

  @Configuration
  @EnableWebMvc
  public static class Config {

    @Bean
    public CourierService courierService() {
      return mock(CourierService.class);
    }

    @Bean
    public CourierController courierController(CourierService courierService) {
      return new CourierController(courierService);
    }

    @Bean
    public WebMvcConfigurer courierWebConfiguration(CourierService courierService) {
      return new CourierWebConfiguration(courierService);
    }
  }

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private CourierService courierService;

  @Before
  public void setUp() {
    when(courierService.authenticate(anyLong(), anyString())).thenReturn(false);
    when(courierService.authenticate(1L, "courier-1-token")).thenReturn(true);
  }

  @Test
  public void shouldRejectAnonymousRequestsThroughRegisteredInterceptor() {
    given().
            webAppContextSetup(webApplicationContext).
            contentType("application/json").
            body(new CourierAvailability(true)).
    when().
            post("/couriers/1/availability").
    then().
            statusCode(401);

    given().
            webAppContextSetup(webApplicationContext).
    when().
            get("/couriers/1").
    then().
            statusCode(401);

    verify(courierService, never()).updateAvailability(anyLong(), anyBoolean());
    verify(courierService, never()).findCourierById(anyLong());
  }

  @Test
  public void shouldAllowOwnCourierThroughRegisteredInterceptor() {
    given().
            webAppContextSetup(webApplicationContext).
            header("Authorization", "Bearer courier-1-token").
            contentType("application/json").
            body(new CourierAvailability(true)).
    when().
            post("/couriers/1/availability").
    then().
            statusCode(200);

    verify(courierService).updateAvailability(1L, true);
  }
}
