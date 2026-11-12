package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.security.FtgoUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class CourierAccessPolicy {

  public void checkSelfOrOperator(FtgoUser caller, long courierId) {
    if (!caller.isOperator() && !caller.isCourier(courierId)) {
      throw new AccessDeniedException("Only the courier or an operator can access this courier");
    }
  }

  public void checkOperator(FtgoUser caller) {
    if (!caller.isOperator()) {
      throw new AccessDeniedException("Only operators can create couriers");
    }
  }
}
