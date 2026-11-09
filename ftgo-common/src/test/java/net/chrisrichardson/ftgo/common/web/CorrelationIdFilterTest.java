package net.chrisrichardson.ftgo.common.web;

import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  private String runAndCaptureMdc(MockHttpServletRequest request, MockHttpServletResponse response) throws Exception {
    AtomicReference<String> seen = new AtomicReference<>();
    MockFilterChain chain = new MockFilterChain(new HttpServlet() {
      @Override
      protected void service(HttpServletRequest req, HttpServletResponse resp) {
        seen.set(MDC.get(CorrelationIdFilter.MDC_KEY));
      }
    });
    filter.doFilter(request, response, chain);
    return seen.get();
  }

  @Test
  public void shouldPropagateIncomingCorrelationIdIntoMdcAndResponse() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/restaurants/1");
    request.addHeader(CorrelationIdFilter.HEADER, "abc-123");
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertEquals("abc-123", runAndCaptureMdc(request, response));
    assertEquals("abc-123", response.getHeader(CorrelationIdFilter.HEADER));
    assertNull("MDC must be cleared after the request", MDC.get(CorrelationIdFilter.MDC_KEY));
  }

  @Test
  public void shouldGenerateCorrelationIdWhenMissing() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    String id = runAndCaptureMdc(new MockHttpServletRequest("GET", "/"), response);

    assertNotNull(id);
    assertEquals(id, response.getHeader(CorrelationIdFilter.HEADER));
  }

  @Test
  public void shouldReplaceUnsafeCorrelationId() {
    assertNotEquals("bad\nvalue", CorrelationIdFilter.resolve("bad\nvalue"));
    assertEquals("ok.value_1-2", CorrelationIdFilter.resolve("ok.value_1-2"));
  }
}
