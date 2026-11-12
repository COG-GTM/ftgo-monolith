package net.chrisrichardson.ftgo.web;

import java.io.IOException;

public class RequestBodyTooLargeException extends IOException {

  public RequestBodyTooLargeException(long limit) {
    super("Request body exceeds " + limit + " bytes");
  }
}
