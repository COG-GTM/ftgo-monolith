package net.chrisrichardson.ftgo.common.tracking;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ApiTrackingControllerTest {

  private ApiRequestLogRepository repository;
  private ApiTrackingController controller;
  private MockMvc mvc;

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
    when(repository.findErrorsSince(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
    when(repository.findByRequestUri(any(String.class))).thenReturn(Collections.emptyList());
    controller = new ApiTrackingController(repository);
    mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new TestAdvice()).build();
    ReflectionTestUtils.setField(controller, "handlerExceptionResolver",
            ((DispatcherServlet) ReflectionTestUtils.getField(mvc, "servlet"))
                    .getWebApplicationContext().getBean("handlerExceptionResolver"));
  }

  @Test
  public void preservesDefaultAndLegacyMinutesOnEveryEndpoint() throws Exception {
    String[] inputs = {null, "", "30", " 30", "+30", "030", "0x1E", "#1E", "-5"};
    int[] expected = {60, 60, 30, 30, 30, 30, 30, 30, -5};
    for (String endpoint : new String[]{"/logs", "/logs/errors", "/stats"}) {
      for (int i = 0; i < inputs.length; i++) {
        reset(repository);
        when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(repository.findErrorsSince(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        LocalDateTime before = LocalDateTime.now().minusMinutes(expected[i]);
        MockHttpServletRequestBuilder request = get("/api/tracking" + endpoint);
        if (inputs[i] != null) {
          request.param("minutesBack", inputs[i]);
        }
        mvc.perform(request).andExpect(status().isOk());
        LocalDateTime after = LocalDateTime.now().minusMinutes(expected[i]);
        ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
        if (endpoint.equals("/logs/errors")) {
          verify(repository).findErrorsSince(since.capture());
        } else {
          verify(repository).findRecentLogs(since.capture());
        }
        assertFalse(since.getValue().isBefore(before));
        assertFalse(since.getValue().isAfter(after));
      }
    }
  }

  @Test
  public void repeatedMinutesUseFirstValue() throws Exception {
    mvc.perform(get("/api/tracking/stats").param("minutesBack", "30", "abc"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.periodMinutes").value(30));
    assertEquals(Integer.valueOf(30), controller.getStats(30).getBody().get("periodMinutes"));
  }

  @Test
  public void rejectsInvalidAndOverflowingMinutesBeforeQuerying() throws Exception {
    for (String endpoint : new String[]{"/logs", "/logs/errors", "/stats"}) {
      for (String input : new String[]{"abc", "1.5", "99999999999", "30,40"}) {
        assertBadMinutes(get("/api/tracking" + endpoint).param("minutesBack", input));
      }
    }
    verifyZeroInteractions(repository);
  }

  @Test
  public void repeatedEmptyFirstMinutesAreRejectedBeforeQuerying() throws Exception {
    for (String endpoint : new String[]{"/logs", "/logs/errors", "/stats"}) {
      assertBadMinutes(get("/api/tracking" + endpoint).param("minutesBack", "", "2"));
    }
    verifyZeroInteractions(repository);
  }

  @Test
  public void missingUriIsRejectedBeforeQuerying() throws Exception {
    mvc.perform(get("/api/tracking/logs/search")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("uri is required"));
    verifyZeroInteractions(repository);
  }

  @Test
  public void emptyAndKnownUriPassThroughUnchanged() throws Exception {
    mvc.perform(get("/api/tracking/logs/search").param("uri", ""))
            .andExpect(status().isOk());
    verify(repository).findByRequestUri("");
    mvc.perform(get("/api/tracking/logs/search").param("uri", "/api/tracking"))
            .andExpect(status().isOk());
    verify(repository).findByRequestUri("/api/tracking");
  }

  @Test
  public void correlationIdLookupIsUnchanged() {
    ApiRequestLog log = new ApiRequestLog();
    when(repository.findByCorrelationId("known")).thenReturn(log);
    assertSame(log, controller.getByCorrelationId("known").getBody());
    assertEquals(HttpStatus.OK, controller.getByCorrelationId("known").getStatusCode());
    assertEquals(HttpStatus.NOT_FOUND, controller.getByCorrelationId("unknown").getStatusCode());
  }

  private void assertBadMinutes(MockHttpServletRequestBuilder request) throws Exception {
    mvc.perform(request).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("minutesBack must be an integer"));
  }

  @Test
  public void unhandledOrUnrelatedMismatchIsRethrown() {
    HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
    ReflectionTestUtils.setField(controller, "handlerExceptionResolver", resolver);
    for (String name : new String[]{"minutesBack", "other"}) {
      MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
              "abc", int.class, name, null, new NumberFormatException());
      try {
        controller.handleTypeMismatch(ex, new MockHttpServletRequest(), new MockHttpServletResponse());
        fail("Expected original exception");
      } catch (MethodArgumentTypeMismatchException actual) {
        assertSame(ex, actual);
      }
    }
    verify(resolver).resolveException(any(), any(), isNull(), any(IllegalArgumentException.class));
  }

  @ControllerAdvice
  public static class TestAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
      return new ResponseEntity<>(new ErrorResponse(400, "Bad Request", ex.getMessage(),
              request.getRequestURI(), null), HttpStatus.BAD_REQUEST);
    }
  }
}
