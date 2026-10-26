package net.chrisrichardson.ftgo.consumerservice.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ConsumerAuthenticationException extends RuntimeException {

  public ConsumerAuthenticationException(String message) {
    super(message);
  }
}
