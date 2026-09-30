package net.chrisrichardson.ftgo.courierservice.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CourierAuthenticationException extends RuntimeException {
  public CourierAuthenticationException() {
    super("Missing or invalid courier credentials");
  }
}
