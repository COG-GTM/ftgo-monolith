package net.chrisrichardson.ftgo;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SecurityHeadersFilterTest {

  @Test
  public void shouldAddSecurityHeadersToResponse() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    new SecurityHeadersFilter().doFilter(request, response, chain);

    assertEquals("DENY", response.getHeader("X-Frame-Options"));
    assertEquals("frame-ancestors 'none'; object-src 'none'; base-uri 'self'", response.getHeader("Content-Security-Policy"));
    assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    assertEquals("strict-origin-when-cross-origin", response.getHeader("Referrer-Policy"));
    assertNotNull("filter chain must continue", chain.getRequest());
  }
}
