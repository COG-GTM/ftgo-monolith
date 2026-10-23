package net.chrisrichardson.ftgo.courierservice.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CourierNotFoundException extends RuntimeException {

  public CourierNotFoundException(long courierId) {
    super("Courier not found: " + courierId);
  }

  public CourierNotFoundException(String message) {
    super(message);
  }
}
