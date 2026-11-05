package net.chrisrichardson.ftgo.common.tracking;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The request URI as sent by the client, captured before any URL-rewriting filter
 * (e.g. trailing-slash normalization) wraps the request.
 */
public final class OriginalRequestUri {

  public static final String ATTRIBUTE = OriginalRequestUri.class.getName();

  private OriginalRequestUri() {
  }

  public static String of(HttpServletRequest request) {
    Object original = request.getAttribute(ATTRIBUTE);
    return original instanceof String uri ? uri : request.getRequestURI();
  }
}
