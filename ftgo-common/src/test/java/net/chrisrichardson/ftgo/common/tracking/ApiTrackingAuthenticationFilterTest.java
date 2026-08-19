package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ApiTrackingAuthenticationFilterTest {

  private final MockFilterChain chain = new MockFilterChain();

  @Test
  public void shouldRejectAnonymousRequests() throws Exception {
    MockHttpServletResponse response = invoke(filterWithCredentials(), request(null));

    assertEquals(401, response.getStatus());
    assertNotNull(response.getHeader("WWW-Authenticate"));
    assertNull(chain.getRequest());
  }

  @Test
  public void shouldRejectWrongCredentials() throws Exception {
    MockHttpServletResponse response = invoke(filterWithCredentials(), request(basic("operator", "nope")));

    assertEquals(401, response.getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  public void shouldRejectEveryRequestWhenNoCredentialsAreConfigured() throws Exception {
    ApiTrackingAuthenticationFilter filter =
            new ApiTrackingAuthenticationFilter(new ApiTrackingSecurityProperties("", ""));

    MockHttpServletResponse response = invoke(filter, request(basic("operator", "s3cret")));

    assertEquals(403, response.getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  public void shouldAllowConfiguredOperator() throws Exception {
    MockHttpServletResponse response = invoke(filterWithCredentials(), request(basic("operator", "s3cret")));

    assertEquals(200, response.getStatus());
    assertNotNull(chain.getRequest());
  }

  private ApiTrackingAuthenticationFilter filterWithCredentials() {
    return new ApiTrackingAuthenticationFilter(new ApiTrackingSecurityProperties("operator", "s3cret"));
  }

  private MockHttpServletResponse invoke(ApiTrackingAuthenticationFilter filter, MockHttpServletRequest request)
          throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, chain);
    return response;
  }

  private MockHttpServletRequest request(String authorization) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tracking/logs");
    if (authorization != null) {
      request.addHeader("Authorization", authorization);
    }
    return request;
  }

  private String basic(String username, String password) {
    return "Basic " + Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
  }
}
