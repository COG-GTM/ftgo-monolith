package net.chrisrichardson.ftgo.orderservice.security;

import net.chrisrichardson.ftgo.domain.Order;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderAccessPolicyTest {

  private final OrderAccessPolicy policy = new OrderAccessPolicy();

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void operationsCanAccessOrderWithoutConsumer() {
    authenticate(FtgoRoles.OPERATIONS, null);

    assertTrue(policy.canAccess(orderOwnedBy(null)));
    assertTrue(policy.canAccess(orderOwnedBy(42L)));
  }

  @Test
  public void consumerCannotAccessOrderWithoutConsumer() {
    authenticate(FtgoRoles.CONSUMER, 1L);

    assertFalse(policy.canAccess(orderOwnedBy(null)));
    assertFalse(policy.canAccess(orderOwnedBy(2L)));
    assertTrue(policy.canAccess(orderOwnedBy(1L)));
  }

  @Test
  public void anonymousCannotAccessAnything() {
    assertFalse(policy.canAccess(orderOwnedBy(1L)));
    assertFalse(policy.canActForConsumer(1L));
    assertFalse(policy.isOperations());
  }

  private static Order orderOwnedBy(Long consumerId) {
    Order order = mock(Order.class);
    when(order.getConsumerId()).thenReturn(consumerId);
    return order;
  }

  private static void authenticate(String role, Long consumerId) {
    FtgoUser user = new FtgoUser("u", "p", AuthorityUtils.createAuthorityList("ROLE_" + role), consumerId);
    SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, "p", user.getAuthorities()));
  }
}
