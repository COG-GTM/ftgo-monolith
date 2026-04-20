package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerVerificationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ConsumerExceptionHandler {

  @ExceptionHandler(ConsumerNotFoundException.class)
  public ResponseEntity<String> handleConsumerNotFound(ConsumerNotFoundException ex) {
    return new ResponseEntity<>("Consumer not found", HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ConsumerVerificationFailedException.class)
  public ResponseEntity<String> handleVerificationFailed(ConsumerVerificationFailedException ex) {
    return new ResponseEntity<>("Consumer verification failed", HttpStatus.UNPROCESSABLE_ENTITY);
  }
}
