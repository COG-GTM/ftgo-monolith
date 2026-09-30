package net.chrisrichardson.ftgo.courierservice.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CourierAccessDeniedException extends RuntimeException {
  public CourierAccessDeniedException(long courierId) {
    super("Not authorized to access courier " + courierId);
  }
}
