package net.chrisrichardson.ftgo.common.tracking;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ApiRequestLogRedaction {

  private ApiRequestLogRedaction() {
  }

  public static String anonymizeRemoteAddr(String remoteAddr) {
    if (remoteAddr == null || remoteAddr.isEmpty()) {
      return null;
    }
    if (remoteAddr.contains(":")) {
      String[] groups = remoteAddr.split(":");
      return Arrays.stream(groups).limit(3).collect(Collectors.joining(":")) + "::/48";
    }
    int lastDot = remoteAddr.lastIndexOf('.');
    if (lastDot < 0) {
      return null;
    }
    return remoteAddr.substring(0, lastDot) + ".0/24";
  }

  public static String redactQueryString(String queryString) {
    if (queryString == null || queryString.isEmpty()) {
      return null;
    }
    return Arrays.stream(queryString.split("&"))
            .map(parameter -> {
              int equals = parameter.indexOf('=');
              if (equals < 0) {
                return parameter.isEmpty() ? parameter : "<redacted>";
              }
              String name = parameter.substring(0, equals);
              return name.isEmpty() ? "<redacted>" : name + "=<redacted>";
            })
            .collect(Collectors.joining("&"));
  }

  public static String describeException(Exception ex) {
    return ex == null ? null : ex.getClass().getName();
  }
}
