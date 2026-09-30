package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApiTrackingControllerTest {

  private ApiRequestLogRepository repository;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new ApiTrackingController(repository)).build();
  }

  @Test
  public void shouldOmitSensitiveFieldsFromLogs() throws Exception {
    ApiRequestLog log = new ApiRequestLog("corr-1", "GET", "/orders", "consumerId=42", "10.0.0.7", "curl/7");
    log.complete(200, 12);
    when(repository.findRecentLogs(any(LocalDateTime.class), any(Pageable.class)))
            .thenReturn(Collections.singletonList(log));

    mockMvc.perform(get("/api/tracking/logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].correlationId").value("corr-1"))
            .andExpect(jsonPath("$[0].requestUri").value("/orders"))
            .andExpect(jsonPath("$[0]", not(hasKey("queryString"))))
            .andExpect(jsonPath("$[0]", not(hasKey("remoteAddr"))))
            .andExpect(jsonPath("$[0]", not(hasKey("userAgent"))));
  }

  @Test
  public void shouldOmitSensitiveFieldsFromSingleLog() throws Exception {
    ApiRequestLog log = new ApiRequestLog("corr-1", "GET", "/orders", "consumerId=42", "10.0.0.7", "curl/7");
    when(repository.findByCorrelationId("corr-1")).thenReturn(log);

    mockMvc.perform(get("/api/tracking/logs/corr-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correlationId").value("corr-1"))
            .andExpect(jsonPath("$", not(hasKey("queryString"))))
            .andExpect(jsonPath("$", not(hasKey("remoteAddr"))))
            .andExpect(jsonPath("$", not(hasKey("userAgent"))));
  }

  @Test
  public void shouldRejectMinutesBackAboveMaximum() throws Exception {
    mockMvc.perform(get("/api/tracking/logs").param("minutesBack", "2147483647"))
            .andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/tracking/stats").param("minutesBack", "2147483647"))
            .andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/tracking/logs/errors").param("minutesBack", "0"))
            .andExpect(status().isBadRequest());

    verify(repository, never()).findRecentLogs(any(LocalDateTime.class));
    verify(repository, never()).findRecentLogs(any(LocalDateTime.class), any(Pageable.class));
    verify(repository, never()).findErrorsSince(any(LocalDateTime.class), any(Pageable.class));
  }

  @Test
  public void shouldRejectLimitAboveMaximum() throws Exception {
    mockMvc.perform(get("/api/tracking/logs").param("limit", "5000"))
            .andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/tracking/logs/search").param("uri", "/orders").param("limit", "0"))
            .andExpect(status().isBadRequest());

    verify(repository, never()).findRecentLogs(any(LocalDateTime.class), any(Pageable.class));
    verify(repository, never()).findByRequestUri(anyString(), any(Pageable.class));
  }

  @Test
  public void shouldPageResultsWithDefaultLimit() throws Exception {
    when(repository.findRecentLogs(any(LocalDateTime.class), any(Pageable.class)))
            .thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/tracking/logs"))
            .andExpect(status().isOk());

    verify(repository).findRecentLogs(any(LocalDateTime.class),
            argThat(p -> p.getPageSize() == ApiTrackingController.DEFAULT_LIMIT && p.getPageNumber() == 0));
  }

  @Test
  public void shouldRejectBlankSearchUri() throws Exception {
    mockMvc.perform(get("/api/tracking/logs/search").param("uri", "  "))
            .andExpect(status().isBadRequest());

    verify(repository, never()).findByRequestUri(eq("  "), any(Pageable.class));
  }
}
