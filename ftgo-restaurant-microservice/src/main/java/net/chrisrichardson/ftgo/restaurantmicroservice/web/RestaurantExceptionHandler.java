package net.chrisrichardson.ftgo.restaurantmicroservice.web;

import net.chrisrichardson.ftgo.restaurantmicroservice.domain.RestaurantNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestaurantExceptionHandler {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(RestaurantNotFoundException.class)
  public void handleNotFound() {
  }
}
