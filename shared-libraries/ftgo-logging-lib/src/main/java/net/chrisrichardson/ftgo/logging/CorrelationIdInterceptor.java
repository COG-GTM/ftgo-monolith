package net.chrisrichardson.ftgo.logging;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * RestTemplate interceptor that propagates the correlation ID from the current
 * MDC context to outgoing HTTP requests via the X-Correlation-ID header.
 */
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        String correlationId = MDC.get(LoggingConstants.MDC_CORRELATION_ID);
        if (correlationId != null) {
            request.getHeaders().set(LoggingConstants.CORRELATION_ID_HEADER, correlationId);
        }
        return execution.execute(request, body);
    }
}
