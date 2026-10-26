package net.chrisrichardson.ftgo.consumerservice.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ConsumerAccessDeniedException extends RuntimeException {

  public ConsumerAccessDeniedException(String message) {
    super(message);
  }
}
