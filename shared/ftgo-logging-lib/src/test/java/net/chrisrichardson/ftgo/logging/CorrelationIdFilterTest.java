package net.chrisrichardson.ftgo.logging;

import net.chrisrichardson.ftgo.logging.filter.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link CorrelationIdFilter}.
 */
class CorrelationIdFilterTest {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Test
    void shouldExtractCorrelationIdFromHeader() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter(CORRELATION_HEADER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String expectedCorrelationId = "test-correlation-123";
        request.addHeader(CORRELATION_HEADER, expectedCorrelationId);

        filter.doFilter(request, response, new MockFilterChain());

        // After filter completes, MDC should be cleared
        assertNull(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID));
        // Response header should contain the correlation ID
        assertEquals(expectedCorrelationId, response.getHeader(CORRELATION_HEADER));
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter(CORRELATION_HEADER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        // Response should have a generated correlation ID
        assertNotNull(response.getHeader(CORRELATION_HEADER));
    }

    @Test
    void shouldPopulateMdcDuringFilterChain() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter(CORRELATION_HEADER);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String expectedCorrelationId = "test-correlation-456";
        request.addHeader(CORRELATION_HEADER, expectedCorrelationId);

        // Capture MDC values during filter chain execution
        final String[] capturedCorrelationId = new String[1];
        final String[] capturedMethod = new String[1];
        final String[] capturedUri = new String[1];

        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                capturedCorrelationId[0] = MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID);
                capturedMethod[0] = MDC.get(CorrelationIdFilter.MDC_REQUEST_METHOD);
                capturedUri[0] = MDC.get(CorrelationIdFilter.MDC_REQUEST_URI);
            }
        };

        filter.doFilter(request, response, chain);

        assertEquals(expectedCorrelationId, capturedCorrelationId[0]);
        assertEquals("GET", capturedMethod[0]);
        assertEquals("/api/orders", capturedUri[0]);
    }

    @Test
    void shouldClearMdcAfterFilterChain() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter(CORRELATION_HEADER);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CORRELATION_HEADER, "test-id");

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID));
        assertNull(MDC.get(CorrelationIdFilter.MDC_REQUEST_METHOD));
        assertNull(MDC.get(CorrelationIdFilter.MDC_REQUEST_URI));
    }

    @Test
    void shouldReturnConfiguredHeaderName() {
        CorrelationIdFilter filter = new CorrelationIdFilter("X-Custom-Header");
        assertEquals("X-Custom-Header", filter.getCorrelationIdHeader());
    }
}
