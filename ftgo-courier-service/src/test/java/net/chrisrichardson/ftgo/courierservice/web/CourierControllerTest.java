package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.lang.reflect.Field;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CourierControllerTest {

  private CourierService courierService;
  private CourierController courierController;

  @Before
  public void setUp() {
    courierService = mock(CourierService.class);
    courierController = new CourierController(courierService);
  }

  @Test
  public void shouldCreateCourier() {
    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    setCourierId(courier, 1L);
    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);

    given().
            standaloneSetup(configureControllers(courierController)).
            contentType("application/json").
            body("{\"name\": {\"firstName\": \"Jane\", \"lastName\": \"Smith\"}, \"address\": {\"street1\": \"1 Main St\", \"city\": \"Oakland\", \"state\": \"CA\", \"zip\": \"94612\"}}").
    when().
            post("/couriers").
    then().
            statusCode(200).
            body("id", equalTo(1));
  }

  @Test
  public void shouldGetCourierById() {
    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    setCourierId(courier, 1L);
    when(courierService.findCourierById(1L)).thenReturn(courier);

    given().
            standaloneSetup(configureControllers(courierController)).
    when().
            get("/couriers/1").
    then().
            statusCode(200);
  }

  @Test
  public void shouldUpdateCourierAvailability() {
    doNothing().when(courierService).updateAvailability(eq(1L), eq(true));

    given().
            standaloneSetup(configureControllers(courierController)).
            contentType("application/json").
            body("{\"available\": true}").
    when().
            post("/couriers/1/availability").
    then().
            statusCode(200);

    verify(courierService).updateAvailability(1L, true);
  }

  @Test
  public void shouldUpdateCourierLocation() {
    doNothing().when(courierService).updateLocation(eq(1L), eq(40.7128), eq(-74.0060));

    given().
            standaloneSetup(configureControllers(courierController)).
            contentType("application/json").
            body("{\"latitude\": 40.7128, \"longitude\": -74.0060}").
    when().
            post("/couriers/1/location").
    then().
            statusCode(200);

    verify(courierService).updateLocation(1L, 40.7128, -74.0060);
  }

  @Test
  public void shouldGetCourierWorkload() {
    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712));
    setCourierId(courier, 1L);
    courier.noteAvailable();
    when(courierService.findCourierById(1L)).thenReturn(courier);

    given().
            standaloneSetup(configureControllers(courierController)).
    when().
            get("/couriers/1/workload").
    then().
            statusCode(200).
            body("courierId", equalTo(1)).
            body("activeDeliveries", equalTo(0)).
            body("available", equalTo(true));
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

  private void setCourierId(Courier courier, long id) {
    try {
      Field idField = Courier.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(courier, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
