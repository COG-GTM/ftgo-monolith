package net.chrisrichardson.ftgo.common.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ApiAuthenticationException extends RuntimeException {

  public ApiAuthenticationException(String message) {
    super(message);
  }
}
