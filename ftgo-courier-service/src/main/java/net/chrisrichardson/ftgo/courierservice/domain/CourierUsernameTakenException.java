package net.chrisrichardson.ftgo.courierservice.domain;

public class CourierUsernameTakenException extends RuntimeException {

  public CourierUsernameTakenException(String username) {
    super("Courier username is already in use: " + username);
  }
}
