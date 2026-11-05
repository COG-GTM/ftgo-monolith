package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ApiTrackingControllerSecurityTest {

  private static final String TOKEN = "operator-secret";

  private ApiRequestLogRepository repository;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    mockMvc = mockMvcFor(TOKEN);
  }

  @Test
  public void requiresBearerTokenAndDoesNotQueryRepository() throws Exception {
    mockMvc.perform(get("/api/tracking/logs"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"api-tracking\""));
    verifyZeroInteractions(repository);
  }

  @Test
  public void rejectsWrongBearerAndBasicAuthorization() throws Exception {
    mockMvc.perform(get("/api/tracking/logs").header("Authorization", "Bearer wrong"))
            .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/api/tracking/logs").header("Authorization", "Basic xxx"))
            .andExpect(status().isUnauthorized());
    verifyZeroInteractions(repository);
  }

  @Test
  public void acceptsConfiguredBearerToken() throws Exception {
    ApiRequestLog log = new ApiRequestLog("correlation-id", "GET", "/consumers/1",
            null, "192.0.2.1", "test-agent");
    when(repository.findRecentLogs(any(LocalDateTime.class)))
            .thenReturn(Collections.singletonList(log));

    mockMvc.perform(authorized(get("/api/tracking/logs")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].requestUri").value("/consumers/1"))
            .andExpect(jsonPath("$[0].remoteAddr").value("192.0.2.1"));
    verify(repository).findRecentLogs(any(LocalDateTime.class));
  }

  @Test
  public void disablesEndpointsWhenTokenIsBlank() throws Exception {
    MockMvc disabledMockMvc = mockMvcFor("");

    disabledMockMvc.perform(get("/api/tracking/logs").header("Authorization", "Bearer "))
            .andExpect(status().isForbidden());
    disabledMockMvc.perform(get("/api/tracking/logs").header("Authorization", "Bearer anything"))
            .andExpect(status().isForbidden());
    verifyZeroInteractions(repository);
  }

  @Test
  public void requiresAuthorizationForEveryTrackingRoute() throws Exception {
    String[] paths = {
            "/api/tracking/logs/errors",
            "/api/tracking/logs/search?uri=x",
            "/api/tracking/logs/abc",
            "/api/tracking/stats"
    };

    for (String path : paths) {
      mockMvc.perform(get(path))
              .andExpect(status().isUnauthorized())
              .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"api-tracking\""));
    }
    verifyZeroInteractions(repository);
  }

  @Test
  public void clampsMinutesBackToOneDay() throws Exception {
    when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
    LocalDateTime beforeRequest = LocalDateTime.now();

    mockMvc.perform(authorized(get("/api/tracking/logs").param("minutesBack", "2147483647")))
            .andExpect(status().isOk());

    ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(repository).findRecentLogs(sinceCaptor.capture());
    assertFalse(sinceCaptor.getValue().isBefore(beforeRequest.minusMinutes(1441)));
    assertTrue(sinceCaptor.getValue().isBefore(beforeRequest.minusMinutes(1439)));
  }

  @Test
  public void rejectsBlankSearchUri() throws Exception {
    mockMvc.perform(authorized(get("/api/tracking/logs/search").param("uri", "")))
            .andExpect(status().isBadRequest());
    verifyZeroInteractions(repository);
  }

  private MockMvc mockMvcFor(String token) {
    return MockMvcBuilders.standaloneSetup(new ApiTrackingController(repository))
            .addMappedInterceptors(new String[]{"/api/tracking", "/api/tracking/**"},
                    new ApiTrackingAccessInterceptor(token))
            .build();
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authorized(
          org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
    return request.header("Authorization", "Bearer " + TOKEN);
  }
}
