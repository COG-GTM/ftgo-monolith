package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.api.web.SlaBreachReport;
import net.chrisrichardson.ftgo.orderservice.api.web.SlaBreachedOrder;
import net.chrisrichardson.ftgo.orderservice.domain.SlaBreachService;
import net.chrisrichardson.ftgo.orderservice.domain.TerminalOrderStateException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlaBreachControllerTest {

  private SlaBreachService slaBreachService;
  private SlaBreachController slaBreachController;

  @Before
  public void setUp() {
    slaBreachService = mock(SlaBreachService.class);
    slaBreachController = new SlaBreachController(slaBreachService);
  }

  @Test
  public void shouldReturnBreachesAndCounts() {
    when(slaBreachService.findBreaches()).thenReturn(report());

    given().
            standaloneSetup(configureControllers(slaBreachController)).
    when().
            get("/orders/sla-breaches").
    then().
            statusCode(200).
            body("totalBreaches", equalTo(1)).
            body("breachCountsByState.PREPARING", equalTo(1)).
            body("thresholdMinutesByState.PREPARING", equalTo(30)).
            body("breaches[0].orderId", equalTo(7)).
            body("breaches[0].state", equalTo(OrderState.PREPARING.name())).
            body("breaches[0].ageMinutes", equalTo(45)).
            body("breaches[0].minutesOverSla", equalTo(15))
    ;
  }

  @Test
  public void shouldFilterByState() {
    when(slaBreachService.findBreaches(OrderState.PREPARING)).thenReturn(report());

    given().
            standaloneSetup(configureControllers(slaBreachController)).
    when().
            get("/orders/sla-breaches?state=preparing").
    then().
            statusCode(200).
            body("totalBreaches", equalTo(1))
    ;
  }

  @Test
  public void shouldRejectUnknownState() {
    given().
            standaloneSetup(configureControllers(slaBreachController)).
    when().
            get("/orders/sla-breaches?state=NOT_A_STATE").
    then().
            statusCode(400)
    ;
  }

  @Test
  public void shouldRejectTerminalState() {
    when(slaBreachService.findBreaches(OrderState.DELIVERED)).thenThrow(new TerminalOrderStateException(OrderState.DELIVERED));

    given().
            standaloneSetup(configureControllers(slaBreachController)).
    when().
            get("/orders/sla-breaches?state=DELIVERED").
    then().
            statusCode(400)
    ;
  }

  private SlaBreachReport report() {
    Map<String, Integer> counts = new LinkedHashMap<>();
    counts.put(OrderState.PREPARING.name(), 1);
    Map<String, Integer> thresholds = new LinkedHashMap<>();
    thresholds.put(OrderState.PREPARING.name(), 30);
    SlaBreachedOrder breach = new SlaBreachedOrder(7L, OrderState.PREPARING.name(), "Ajanta",
            LocalDateTime.now().minusMinutes(45), 45, 30);
    return new SlaBreachReport(counts, thresholds, Collections.singletonList(breach));
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }
}
