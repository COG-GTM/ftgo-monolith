package net.chrisrichardson.ftgo.consumerservice;

import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerVerificationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ConsumerNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleConsumerNotFound(ConsumerNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Collections.singletonMap("error", "Consumer not found"));
  }

  @ExceptionHandler(ConsumerVerificationFailedException.class)
  public ResponseEntity<Map<String, String>> handleVerificationFailed(ConsumerVerificationFailedException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(Collections.singletonMap("error", "Consumer verification failed"));
  }
}
