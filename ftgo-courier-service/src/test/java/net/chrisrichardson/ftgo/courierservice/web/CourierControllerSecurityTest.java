package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierCredentialsRequest;
import net.chrisrichardson.ftgo.courierservice.domain.CourierCredentials;
import net.chrisrichardson.ftgo.courierservice.domain.CourierCredentialsRepository;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Plan;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(CourierController.class)
@Import({CourierController.class, CourierSecurityConfiguration.class,
        FtgoUserDetailsService.class, CourierAccessPolicy.class})
@TestPropertySource(properties = "ftgo.security.operator.password=test-operator-password")
public class CourierControllerSecurityTest {

  private static final String OPERATOR_PASSWORD = "test-operator-password";
  private static final String CAROL_PASSWORD = "carol-courier-password";
  private static final String DAVE_PASSWORD = "dave-courier-password";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @MockBean
  private CourierService courierService;
  @MockBean
  private CourierCredentialsRepository credentialsRepository;

  @Before
  public void setUp() {
    when(credentialsRepository.findByUsername("carol"))
            .thenReturn(Optional.of(credentials(1L, "carol", CAROL_PASSWORD)));
    when(credentialsRepository.findByUsername("dave"))
            .thenReturn(Optional.of(credentials(2L, "dave", DAVE_PASSWORD)));
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(1L);
    when(courier.getName()).thenReturn(new PersonName("Carol", "Courier"));
    when(courier.isAvailable()).thenReturn(true);
    when(courier.getPlan()).thenReturn(new Plan());
    when(courierService.findCourierById(anyLong())).thenReturn(courier);
    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);
  }

  private CourierCredentials credentials(long id, String username, String password) {
    CourierCredentials credentials = new CourierCredentials(id);
    credentials.update(username, passwordEncoder.encode(password));
    return credentials;
  }

  @Test
  public void shouldRequireAuthenticationForCourierReadsIncludingHeadAndTrailingSlash() throws Exception {
    mockMvc.perform(get("/couriers/1"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", containsString("Basic")));
    mockMvc.perform(head("/couriers/1"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", containsString("Basic")));
    mockMvc.perform(get("/couriers/1/workload"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", containsString("Basic")));
    mockMvc.perform(get("/couriers/1/"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", containsString("Basic")));

    verifyZeroInteractions(courierService);
  }

  @Test
  public void shouldRejectIncorrectOperatorPassword() throws Exception {
    mockMvc.perform(get("/couriers/1").with(httpBasic("operator", "incorrect-password")))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", containsString("Basic")));
    verifyZeroInteractions(courierService);
  }

  @Test
  public void shouldDenyCourierReadingAnotherCouriersRecordsBeforeLookup() throws Exception {
    mockMvc.perform(get("/couriers/1").with(httpBasic("dave", DAVE_PASSWORD)))
            .andExpect(status().isForbidden());
    mockMvc.perform(get("/couriers/1/workload").with(httpBasic("dave", DAVE_PASSWORD)))
            .andExpect(status().isForbidden());
    verifyZeroInteractions(courierService);
  }

  @Test
  public void shouldAllowCourierToReadOwnRecordWithoutAddressOrLocation() throws Exception {
    mockMvc.perform(get("/couriers/1").with(httpBasic("carol", CAROL_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName").value("Carol"))
            .andExpect(jsonPath("$.address").doesNotExist())
            .andExpect(jsonPath("$.currentLatitude").doesNotExist());
    mockMvc.perform(get("/couriers/1/workload").with(httpBasic("carol", CAROL_PASSWORD)))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldAllowOperatorToReadAnyCourier() throws Exception {
    mockMvc.perform(get("/couriers/2").with(httpBasic("operator", OPERATOR_PASSWORD)))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldRestrictCredentialProvisioningToOperator() throws Exception {
    String request = "{\"username\":\"new-courier\",\"password\":\"a-long-password\"}";
    mockMvc.perform(put("/couriers/1/credentials").with(httpBasic("carol", CAROL_PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isForbidden());
    mockMvc.perform(put("/couriers/1/credentials").with(httpBasic("operator", OPERATOR_PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk());
    verify(courierService).setCredentials(1L, "new-courier", "a-long-password");
  }

  @Test
  public void shouldDenyCourierCredentialProvisioningWithTrailingSlash() throws Exception {
    assertCourierCredentialPathDenied("/couriers/2/credentials/");
  }

  @Test
  public void shouldDenyCourierCredentialProvisioningWithSuffix() throws Exception {
    assertCourierCredentialPathDenied("/couriers/2/credentials.json");
  }

  private void assertCourierCredentialPathDenied(String path) throws Exception {
    String request = "{\"username\":\"new-courier\",\"password\":\"a-long-password\"}";
    mockMvc.perform(put(path).with(httpBasic("carol", CAROL_PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isForbidden());
    verifyZeroInteractions(courierService);
  }

  @Test
  public void shouldLeaveCourierPostEndpointsOutsideSecurityFilterChain() throws Exception {
    String createCourier = "{\"name\":{\"firstName\":\"Carol\",\"lastName\":\"Courier\"},"
            + "\"address\":{\"street1\":\"1 Main Street\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94619\"}}";
    for (String authorization : new String[]{null, "Basic Zm9vOmJhcg=="}) {
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder createRequest =
              post("/couriers").contentType(MediaType.APPLICATION_JSON).content(createCourier);
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder availabilityRequest =
              post("/couriers/1/availability").contentType(MediaType.APPLICATION_JSON)
                      .content("{\"available\":true}");
      if (authorization != null) {
        createRequest.header("Authorization", authorization);
        availabilityRequest.header("Authorization", authorization);
      }
      mockMvc.perform(createRequest).andExpect(status().isOk());
      mockMvc.perform(availabilityRequest).andExpect(status().isOk());
    }
  }
}
