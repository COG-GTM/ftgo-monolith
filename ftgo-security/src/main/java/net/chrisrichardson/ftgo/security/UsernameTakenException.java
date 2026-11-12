package net.chrisrichardson.ftgo.security;

public class UsernameTakenException extends RuntimeException {
  public UsernameTakenException(String username) {
    super("Username is already taken: " + username);
  }
}
