package com.ftgo.logging.correlation;

import org.slf4j.MDC;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class TraceMdcLifecycleHandler {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (MDC.get(TRACE_ID_KEY) == null) {
            MDC.put(TRACE_ID_KEY, "");
        }
        if (MDC.get(SPAN_ID_KEY) == null) {
            MDC.put(SPAN_ID_KEY, "");
        }
    }

    public static void setTraceContext(String traceId, String spanId) {
        if (traceId != null) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
        if (spanId != null) {
            MDC.put(SPAN_ID_KEY, spanId);
        }
    }

    public static void clearTraceContext() {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(SPAN_ID_KEY);
    }
}
