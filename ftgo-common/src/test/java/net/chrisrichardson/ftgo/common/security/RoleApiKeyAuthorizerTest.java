package net.chrisrichardson.ftgo.common.security;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RoleApiKeyAuthorizerTest {

  private RoleApiKeyAuthorizer authorizer;

  @Before
  public void setUp() {
    Map<ApiRole, String> keys = new EnumMap<>(ApiRole.class);
    keys.put(ApiRole.RESTAURANT, "restaurant-key");
    keys.put(ApiRole.COURIER, "courier-key");
    keys.put(ApiRole.OPERATOR, "   ");
    authorizer = new RoleApiKeyAuthorizer(keys);
  }

  private MockHttpServletRequest requestWithAuthorization(String value) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    if (value != null) {
      request.addHeader(RoleApiKeyAuthorizer.AUTHORIZATION_HEADER, value);
    }
    return request;
  }

  @Test
  public void shouldResolveRoleFromKey() {
    assertEquals(ApiRole.RESTAURANT, authorizer.requireRole(requestWithAuthorization("Bearer restaurant-key"), ApiRole.RESTAURANT, ApiRole.OPERATOR));
    assertEquals(ApiRole.COURIER, authorizer.authenticate(requestWithAuthorization("Bearer courier-key")));
  }

  @Test(expected = ApiAuthenticationException.class)
  public void shouldRejectMissingHeader() {
    authorizer.requireRole(requestWithAuthorization(null), ApiRole.RESTAURANT);
  }

  @Test(expected = ApiAuthenticationException.class)
  public void shouldRejectNonBearerScheme() {
    authorizer.requireRole(requestWithAuthorization("Basic restaurant-key"), ApiRole.RESTAURANT);
  }

  @Test(expected = ApiAuthenticationException.class)
  public void shouldRejectUnknownKey() {
    authorizer.requireRole(requestWithAuthorization("Bearer nope"), ApiRole.RESTAURANT);
  }

  @Test(expected = ApiAuthenticationException.class)
  public void shouldNeverAuthenticateRoleWithBlankConfiguredKey() {
    authorizer.requireRole(requestWithAuthorization("Bearer    "), ApiRole.OPERATOR);
  }

  @Test(expected = ApiAccessDeniedException.class)
  public void shouldRejectRoleNotPermittedForAction() {
    authorizer.requireRole(requestWithAuthorization("Bearer courier-key"), ApiRole.RESTAURANT);
  }
}
