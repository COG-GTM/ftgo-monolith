package net.chrisrichardson.ftgo.common.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationRequiredException extends RuntimeException {

  public AuthenticationRequiredException() {
    super("Authentication required");
  }
}
