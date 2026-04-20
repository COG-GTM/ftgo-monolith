package net.chrisrichardson.ftgo.observability.tracing;

import org.slf4j.MDC;

public class TracingContextProvider {

    private final String serviceName;

    public TracingContextProvider(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getTraceId() {
        return MDC.get("X-B3-TraceId");
    }

    public String getSpanId() {
        return MDC.get("X-B3-SpanId");
    }

    public void setCorrelationId(String correlationId) {
        MDC.put("correlationId", correlationId);
    }

    public String getCorrelationId() {
        return MDC.get("correlationId");
    }
}
