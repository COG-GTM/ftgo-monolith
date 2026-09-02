package net.chrisrichardson.ftgo.common.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ApiAccessDeniedException extends RuntimeException {

  public ApiAccessDeniedException(String message) {
    super(message);
  }
}
