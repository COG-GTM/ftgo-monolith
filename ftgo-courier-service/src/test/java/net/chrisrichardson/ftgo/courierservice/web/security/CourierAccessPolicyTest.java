package net.chrisrichardson.ftgo.courierservice.web.security;

import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CourierAccessPolicyTest {

  private final CourierAccessPolicy policy = new CourierAccessPolicy();

  @Test
  public void shouldDenyUnauthenticatedCallers() {
    assertFalse(policy.canAccessCourier(null, 1L));
    Authentication anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    assertFalse(policy.canAccessCourier(anonymous, 1L));
  }

  @Test
  public void shouldAllowDispatcherToAccessAnyCourier() {
    assertTrue(policy.canAccessCourier(dispatcher(), 42L));
  }

  @Test
  public void shouldAllowCourierToAccessOnlyItsOwnRecord() {
    assertTrue(policy.canAccessCourier(courier(42L), 42L));
    assertFalse(policy.canAccessCourier(courier(42L), 43L));
  }

  @Test
  public void shouldDenyCourierAccountWithoutCourierId() {
    assertFalse(policy.canAccessCourier(courier(null), 42L));
  }

  @Test
  public void shouldDenyAuthenticatedNonCourierPrincipal() {
    Authentication other = new UsernamePasswordAuthenticationToken(
            new User("someone", "password", AuthorityUtils.createAuthorityList("ROLE_COURIER")),
            "password", AuthorityUtils.createAuthorityList("ROLE_COURIER"));
    assertFalse(policy.canAccessCourier(other, 42L));
  }

  private Authentication dispatcher() {
    CourierUserDetails principal = new CourierUserDetails("dispatcher", "password", null,
            AuthorityUtils.createAuthorityList("ROLE_DISPATCHER"));
    return new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
  }

  private Authentication courier(Long courierId) {
    CourierUserDetails principal = new CourierUserDetails("courier", "password", courierId,
            AuthorityUtils.createAuthorityList("ROLE_COURIER"));
    return new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
  }
}
