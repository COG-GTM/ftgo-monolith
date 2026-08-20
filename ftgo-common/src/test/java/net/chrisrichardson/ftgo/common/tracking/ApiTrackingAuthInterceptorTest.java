package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiTrackingAuthInterceptorTest {

  private static final String TOKEN = "0123456789abcdef0123456789abcdef";

  private final MockHttpServletResponse response = new MockHttpServletResponse();

  @Test
  public void shouldHideEndpointsWhenNoTokenIsConfigured() {
    assertFalse(preHandle(null, TOKEN));
    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldIgnoreTokenShorterThanMinimumLength() {
    assertFalse(preHandle("tooshort", "tooshort"));
    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldRejectMissingToken() {
    assertFalse(preHandle(TOKEN, null));
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void shouldRejectWrongToken() {
    assertFalse(preHandle(TOKEN, TOKEN.replace('0', '1')));
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void shouldAcceptConfiguredToken() {
    assertTrue(preHandle(TOKEN, TOKEN));
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
  }

  @Test
  public void shouldAcceptConfiguredTokenSurroundedByWhitespace() {
    assertTrue(preHandle(" " + TOKEN + " ", "\t" + TOKEN + "\n"));
  }

  private boolean preHandle(String configuredToken, String presentedToken) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tracking/logs");
    if (presentedToken != null) {
      request.addHeader(ApiTrackingAuthInterceptor.ADMIN_TOKEN_HEADER, presentedToken);
    }
    return new ApiTrackingAuthInterceptor(configuredToken).preHandle(request, response, new Object());
  }
}
