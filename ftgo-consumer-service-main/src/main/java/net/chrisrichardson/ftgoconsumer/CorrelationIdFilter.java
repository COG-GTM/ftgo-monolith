package net.chrisrichardson.ftgoconsumer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
class CorrelationIdFilter extends OncePerRequestFilter {

  private static final String HEADER = "X-Correlation-ID";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
          throws ServletException, IOException {
    String suppliedId = request.getHeader(HEADER);
    String correlationId = suppliedId == null || suppliedId.isEmpty() ? UUID.randomUUID().toString() : suppliedId;
    response.setHeader(HEADER, correlationId);
    try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId)) {
      chain.doFilter(request, response);
    }
  }
}
