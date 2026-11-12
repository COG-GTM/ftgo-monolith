package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.security.FtgoUser;
import net.chrisrichardson.ftgo.security.Role;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CourierAccessPolicyTest {

  private static final long COURIER_ID = 123;
  private final CourierAccessPolicy policy = new CourierAccessPolicy();

  @Test
  public void onlyTheCourierOrOperatorCanAccessCourierResources() {
    policy.checkSelfOrOperator(user(Role.COURIER, COURIER_ID), COURIER_ID);
    policy.checkSelfOrOperator(user(Role.OPERATOR, null), COURIER_ID);
    assertDenied(() -> policy.checkSelfOrOperator(user(Role.COURIER, COURIER_ID + 1), COURIER_ID));
    assertDenied(() -> policy.checkSelfOrOperator(user(Role.CONSUMER, COURIER_ID), COURIER_ID));
    assertDenied(() -> policy.checkSelfOrOperator(user(Role.RESTAURANT, COURIER_ID), COURIER_ID));
  }

  @Test
  public void onlyAnOperatorCanCreateCouriers() {
    policy.checkOperator(user(Role.OPERATOR, null));
    assertDenied(() -> policy.checkOperator(user(Role.COURIER, COURIER_ID)));
    assertDenied(() -> policy.checkOperator(user(Role.CONSUMER, COURIER_ID)));
    assertDenied(() -> policy.checkOperator(user(Role.RESTAURANT, COURIER_ID)));
  }

  private FtgoUser user(Role role, Long entityId) {
    return new FtgoUser(role.name().toLowerCase(), "hash", role, entityId);
  }

  private void assertDenied(Runnable action) {
    assertThatThrownBy(action::run).isInstanceOf(AccessDeniedException.class);
  }
}
