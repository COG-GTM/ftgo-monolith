package net.chrisrichardson.ftgo.common.tracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApiTrackingControllerTest {
  private final ApiRequestLogRepository repository = mock(ApiRequestLogRepository.class);
  private final HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
  private final ApiTrackingController controller = new ApiTrackingController(repository, resolver);

  @Test
  public void rejectsOutOfRangePeriodsOnEveryEndpoint() {
    for (int minutes : new int[]{0, -5, 20000}) {
      for (Runnable call : Arrays.<Runnable>asList(() -> controller.getRecentLogs(minutes),
              () -> controller.getErrors(minutes), () -> controller.getStats(minutes))) {
        try {
          call.run();
          fail("Expected invalid period");
        } catch (IllegalArgumentException ex) {
          assertEquals("minutesBack must be an integer between 1 and 10080", ex.getMessage());
        }
      }
    }
    verifyZeroInteractions(repository);
  }

  @Test
  public void acceptsPeriodBoundariesAndEmptyResults() {
    when(repository.findRecentLogs(any())).thenReturn(Collections.emptyList());
    when(repository.findErrorsSince(any())).thenReturn(Collections.emptyList());
    when(repository.findByRequestUri(anyString())).thenReturn(Collections.emptyList());
    for (int minutes : new int[]{1, 60, 10080}) {
      assertTrue(controller.getRecentLogs(minutes).getBody().isEmpty());
      assertTrue(controller.getErrors(minutes).getBody().isEmpty());
      assertEquals(0L, controller.getStats(minutes).getBody().get("p95DurationMs"));
    }
    assertTrue(controller.searchByUri("missing").getBody().isEmpty());
    assertEquals(404, controller.getByCorrelationId("missing").getStatusCodeValue());
  }

  @Test
  public void computesPercentileUsingOnlyNonNullDurations() {
    ApiRequestLog complete = log();
    complete.complete(200, 42);
    when(repository.findRecentLogs(any())).thenReturn(Arrays.asList(log(), complete, log()));
    assertEquals(42L, controller.getStats(60).getBody().get("p95DurationMs"));
    assertEquals(42.0, controller.getStats(60).getBody().get("avgDurationMs"));
    when(repository.findRecentLogs(any())).thenReturn(Collections.singletonList(log()));
    assertEquals(0L, controller.getStats(60).getBody().get("p95DurationMs"));
  }

  @Test
  public void serializesDtosExactlyLikeEntitiesForAllLogEndpoints() throws Exception {
    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    ApiRequestLog log = log();
    for (boolean completed : new boolean[]{false, true}) {
      if (completed) log.complete(500, 123, "failure");
      when(repository.findRecentLogs(any())).thenReturn(Collections.singletonList(log));
      when(repository.findErrorsSince(any())).thenReturn(Collections.singletonList(log));
      when(repository.findByRequestUri(anyString())).thenReturn(Collections.singletonList(log));
      when(repository.findByCorrelationId(anyString())).thenReturn(log);
      Object[] results = {controller.getRecentLogs(60).getBody().get(0),
              controller.getErrors(60).getBody().get(0), controller.searchByUri("/test").getBody().get(0),
              controller.getByCorrelationId("id").getBody()};
      for (Object result : results) {
        assertFalse(result instanceof ApiRequestLog);
        assertEquals(mapper.valueToTree(log), mapper.valueToTree(result));
      }
    }
  }

  @Test
  public void delegatesFixedParameterErrorsToGlobalResolverWithoutHandler() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    ModelAndView resolved = new ModelAndView();
    when(resolver.resolveException(eq(request), eq(response), isNull(), any())).thenReturn(resolved);
    assertSame(resolved, controller.handleParameterException(
            new MethodArgumentTypeMismatchException("unsafe", int.class, "minutesBack", null, null), request, response));
    assertSame(resolved, controller.handleParameterException(
            new MissingServletRequestParameterException("uri", "String"), request, response));
    verify(resolver).resolveException(eq(request), eq(response), isNull(),
            argThat(ex -> ex instanceof IllegalArgumentException &&
                    "minutesBack must be an integer between 1 and 10080".equals(ex.getMessage())));
    verify(resolver).resolveException(eq(request), eq(response), isNull(),
            argThat(ex -> ex instanceof IllegalArgumentException && "uri is required".equals(ex.getMessage())));
  }

  @Test
  public void doesNotRemapOtherParameters() throws Exception {
    MissingServletRequestParameterException exception = new MissingServletRequestParameterException("other", "String");
    try {
      controller.handleParameterException(exception, new MockHttpServletRequest(), new MockHttpServletResponse());
      fail("Expected original exception");
    } catch (MissingServletRequestParameterException ex) {
      assertSame(exception, ex);
    }
    verifyZeroInteractions(resolver);
  }

  private ApiRequestLog log() {
    return new ApiRequestLog("id", "GET", "/test", "x=1", "127.0.0.1", "test");
  }
}
