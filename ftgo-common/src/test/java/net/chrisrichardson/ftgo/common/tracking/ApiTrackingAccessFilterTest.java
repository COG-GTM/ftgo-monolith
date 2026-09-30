package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ApiTrackingAccessFilterTest {

  private static final String KEY = "s3cr3t-operator-key";

  @Test
  public void shouldReturn404WhenNoKeyConfigured() throws Exception {
    ApiTrackingAccessFilter filter = new ApiTrackingAccessFilter("");
    MockHttpServletRequest request = request();
    request.addHeader("Authorization", "Bearer anything");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(404, response.getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  public void shouldReturn401WhenNoCredentialsPresented() throws Exception {
    ApiTrackingAccessFilter filter = new ApiTrackingAccessFilter(KEY);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request(), response, chain);

    assertEquals(401, response.getStatus());
    assertNotNull(response.getHeader("WWW-Authenticate"));
    assertNull(chain.getRequest());
  }

  @Test
  public void shouldReturn403WhenKeyDoesNotMatch() throws Exception {
    ApiTrackingAccessFilter filter = new ApiTrackingAccessFilter(KEY);
    MockHttpServletRequest request = request();
    request.addHeader("Authorization", "Bearer wrong-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(403, response.getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  public void shouldPassThroughWithValidBearerToken() throws Exception {
    ApiTrackingAccessFilter filter = new ApiTrackingAccessFilter(KEY);
    MockHttpServletRequest request = request();
    request.addHeader("Authorization", "Bearer " + KEY);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    assertNotNull(chain.getRequest());
  }

  @Test
  public void shouldPassThroughWithValidApiKeyHeader() throws Exception {
    ApiTrackingAccessFilter filter = new ApiTrackingAccessFilter(KEY);
    MockHttpServletRequest request = request();
    request.addHeader(ApiTrackingAccessFilter.API_KEY_HEADER, KEY);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    assertNotNull(chain.getRequest());
    assertFalse(response.isCommitted());
  }

  private static MockHttpServletRequest request() {
    return new MockHttpServletRequest("GET", "/api/tracking/logs");
  }
}
