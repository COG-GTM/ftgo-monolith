package net.chrisrichardson.ftgo.common.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApiTrackingInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(ApiTrackingInterceptor.class);
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String START_TIME_ATTR = "apiTracking.startTime";
  private static final String LOG_ENTRY_ATTR = "apiTracking.logEntry";
  private static final String UNKNOWN_IPV6 = "::";
  private static final int IPV6_GROUPS = 8;
  private static final int IPV6_PREFIX_BYTES = 8;

  private final ApiRequestLogRepository apiRequestLogRepository;

  public ApiTrackingInterceptor(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    long startTime = System.currentTimeMillis();
    request.setAttribute(START_TIME_ATTR, startTime);

    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }

    MDC.put("correlationId", correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    ApiRequestLog logEntry = new ApiRequestLog(
            correlationId,
            request.getMethod(),
            request.getRequestURI(),
            redactQueryString(request.getQueryString()),
            anonymizeAddress(request.getRemoteAddr()),
            request.getHeader("User-Agent")
    );

    request.setAttribute(LOG_ENTRY_ATTR, logEntry);

    logger.info("[{}] {} {} started", correlationId, request.getMethod(), request.getRequestURI());

    return true;
  }

  /** Keeps parameter names, drops their values. */
  static String redactQueryString(String queryString) {
    if (queryString == null || queryString.isEmpty()) {
      return queryString;
    }
    StringBuilder redacted = new StringBuilder();
    for (String parameter : queryString.split("&")) {
      if (redacted.length() > 0) {
        redacted.append('&');
      }
      int separator = parameter.indexOf('=');
      redacted.append(separator < 0 ? "REDACTED" : parameter.substring(0, separator + 1) + "REDACTED");
    }
    return redacted.toString();
  }

  /** Zeroes the host part of the client address, keeping only network-level granularity. */
  static String anonymizeAddress(String remoteAddr) {
    if (remoteAddr == null || remoteAddr.isEmpty()) {
      return remoteAddr;
    }
    if (remoteAddr.indexOf(':') >= 0) {
      return anonymizeIpv6(remoteAddr);
    }
    int lastDot = remoteAddr.lastIndexOf('.');
    return lastDot < 0 ? remoteAddr : remoteAddr.substring(0, lastDot) + ".0";
  }

  /** Keeps the /64 network prefix of an IPv6 address and zeroes the interface identifier. */
  private static String anonymizeIpv6(String address) {
    byte[] bytes = parseIpv6(address);
    if (bytes == null) {
      return UNKNOWN_IPV6;
    }
    if (isIpv4Mapped(bytes)) {
      byte[] ipv4 = new byte[] {bytes[12], bytes[13], bytes[14], 0};
      try {
        return InetAddress.getByAddress(ipv4).getHostAddress();
      } catch (UnknownHostException e) {
        return UNKNOWN_IPV6;
      }
    }
    for (int i = IPV6_PREFIX_BYTES; i < bytes.length; i++) {
      bytes[i] = 0;
    }
    try {
      return InetAddress.getByAddress(bytes).getHostAddress();
    } catch (UnknownHostException e) {
      return UNKNOWN_IPV6;
    }
  }

  private static boolean isIpv4Mapped(byte[] bytes) {
    for (int i = 0; i < 10; i++) {
      if (bytes[i] != 0) {
        return false;
      }
    }
    return bytes[10] == (byte) 0xff && bytes[11] == (byte) 0xff;
  }

  /** Returns the 16 address bytes of a literal IPv6 address, or null when it cannot be parsed. */
  private static byte[] parseIpv6(String address) {
    String literal = address;
    int zone = literal.indexOf('%');
    if (zone >= 0) {
      literal = literal.substring(0, zone);
    }
    int lastColon = literal.lastIndexOf(':');
    if (literal.indexOf('.', lastColon) >= 0) {
      String embedded = embeddedIpv4AsGroups(literal.substring(lastColon + 1));
      if (embedded == null) {
        return null;
      }
      literal = literal.substring(0, lastColon + 1) + embedded;
    }

    String[] halves = literal.split("::", -1);
    if (halves.length > 2) {
      return null;
    }
    List<String> head = groupsOf(halves[0]);
    List<String> tail = halves.length == 2 ? groupsOf(halves[1]) : new ArrayList<>();
    if (head == null || tail == null) {
      return null;
    }
    int total = head.size() + tail.size();
    if (total > IPV6_GROUPS || (halves.length == 1 && total != IPV6_GROUPS)) {
      return null;
    }

    byte[] bytes = new byte[2 * IPV6_GROUPS];
    int index = 0;
    for (String group : head) {
      if (!writeGroup(bytes, index, group)) {
        return null;
      }
      index += 2;
    }
    index = 2 * (IPV6_GROUPS - tail.size());
    for (String group : tail) {
      if (!writeGroup(bytes, index, group)) {
        return null;
      }
      index += 2;
    }
    return bytes;
  }

  private static String embeddedIpv4AsGroups(String ipv4) {
    String[] octets = ipv4.split("\\.", -1);
    if (octets.length != 4) {
      return null;
    }
    int[] values = new int[4];
    for (int i = 0; i < 4; i++) {
      try {
        values[i] = Integer.parseInt(octets[i]);
      } catch (NumberFormatException e) {
        return null;
      }
      if (values[i] < 0 || values[i] > 255) {
        return null;
      }
    }
    return String.format("%x:%x", (values[0] << 8) | values[1], (values[2] << 8) | values[3]);
  }

  private static List<String> groupsOf(String half) {
    List<String> groups = new ArrayList<>();
    if (half.isEmpty()) {
      return groups;
    }
    for (String group : half.split(":", -1)) {
      if (group.isEmpty()) {
        return null;
      }
      groups.add(group);
    }
    return groups;
  }

  private static boolean writeGroup(byte[] bytes, int index, String group) {
    int value;
    try {
      value = Integer.parseInt(group, 16);
    } catch (NumberFormatException e) {
      return false;
    }
    if (value < 0 || value > 0xffff) {
      return false;
    }
    bytes[index] = (byte) (value >> 8);
    bytes[index + 1] = (byte) value;
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
    ApiRequestLog logEntry = (ApiRequestLog) request.getAttribute(LOG_ENTRY_ATTR);

    if (logEntry != null && startTime != null) {
      long durationMs = System.currentTimeMillis() - startTime;

      if (ex != null) {
        logEntry.complete(response.getStatus(), durationMs, ex.getMessage());
      } else {
        logEntry.complete(response.getStatus(), durationMs);
      }

      try {
        apiRequestLogRepository.save(logEntry);
      } catch (Exception saveEx) {
        logger.warn("Failed to persist API request log: {}", saveEx.getMessage());
      }

      String correlationId = logEntry.getCorrelationId();
      logger.info("[{}] {} {} completed: status={} duration={}ms",
              correlationId, request.getMethod(), request.getRequestURI(),
              response.getStatus(), durationMs);
    }

    MDC.remove("correlationId");
  }
}
