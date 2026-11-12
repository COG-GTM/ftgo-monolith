package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.web.RequestBodyTooLargeException;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.setMessageInterpolator(new ParameterMessageInterpolator());
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  @Test
  public void mapsInvalidQuantityToBadRequestWithValidationDetails() throws Exception {
    mockMvc.perform(post("/test/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"consumerId\":1,\"restaurantId\":1,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":0}]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message", containsString("lineItems[0].quantity")));
  }

  @Test
  public void mapsEmptyLineItemsToBadRequestWithValidationDetails() throws Exception {
    mockMvc.perform(post("/test/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"consumerId\":1,\"restaurantId\":1,\"lineItems\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message", containsString("lineItems")));
  }

  @Test
  public void mapsMalformedJsonToBadRequest() throws Exception {
    mockMvc.perform(post("/test/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"consumerId\":"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Malformed request body"));
  }

  @Test
  public void mapsUnwrappedRequestBodyTooLargeExceptionToPayloadTooLarge() throws Exception {
    mockMvc.perform(post("/test/too-large"))
            .andExpect(status().isPayloadTooLarge())
            .andExpect(jsonPath("$.error").value("Payload Too Large"))
            .andExpect(jsonPath("$.message", containsString("65536")));
  }

  @Test
  public void mapsWrappedRequestBodyTooLargeExceptionToPayloadTooLarge() throws Exception {
    mockMvc.perform(post("/test/too-large-wrapped"))
            .andExpect(status().isPayloadTooLarge())
            .andExpect(jsonPath("$.error").value("Payload Too Large"))
            .andExpect(jsonPath("$.message", containsString("65536")));
  }

  @Test
  public void acceptsValidOrderRequest() throws Exception {
    mockMvc.perform(post("/test/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"consumerId\":1,\"restaurantId\":1,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":1}]}"))
            .andExpect(status().isOk());
  }

  @RestController
  public static class TestController {

    @PostMapping("/test/orders")
    public void createOrder(@Valid @RequestBody CreateOrderRequest request) {
    }

    @PostMapping("/test/too-large")
    public void throwRequestBodyTooLarge() throws RequestBodyTooLargeException {
      throw new RequestBodyTooLargeException(65536);
    }

    @PostMapping("/test/too-large-wrapped")
    public void throwWrappedRequestBodyTooLarge() {
      throw new HttpMessageNotReadableException(
              "unreadable", new RequestBodyTooLargeException(65536));
    }
  }
}
