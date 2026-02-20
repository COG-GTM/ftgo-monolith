package com.ftgo.api.standards.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void generatesCorrelationIdWhenNotPresent() throws ServletException, IOException {
        String[] capturedId = new String[1];
        FilterChain chain = (req, res) -> {
            capturedId[0] = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedId[0]).isNotNull().isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(capturedId[0]);
    }

    @Test
    void usesExistingCorrelationIdFromHeader() throws ServletException, IOException {
        String existingId = "existing-correlation-id-123";
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingId);

        String[] capturedId = new String[1];
        FilterChain chain = (req, res) -> {
            capturedId[0] = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedId[0]).isEqualTo(existingId);
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(existingId);
    }

    @Test
    void generatesNewIdWhenHeaderIsBlank() throws ServletException, IOException {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ");

        String[] capturedId = new String[1];
        FilterChain chain = (req, res) -> {
            capturedId[0] = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedId[0]).isNotNull().isNotBlank().isNotEqualTo("   ");
    }

    @Test
    void cleansMdcAfterRequest() throws ServletException, IOException {
        FilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void cleansMdcEvenOnException() throws ServletException, IOException {
        FilterChain chain = (req, res) -> {
            throw new ServletException("test error");
        };

        try {
            filter.doFilterInternal(request, response, chain);
        } catch (ServletException ignored) {
        }

        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void setsResponseHeaderWithCorrelationId() throws ServletException, IOException {
        FilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        String headerValue = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(headerValue).isNotNull().isNotBlank();
    }

    @Test
    void correlationIdHeaderConstant() {
        assertThat(CorrelationIdFilter.CORRELATION_ID_HEADER).isEqualTo("X-Correlation-ID");
    }

    @Test
    void correlationIdMdcKeyConstant() {
        assertThat(CorrelationIdFilter.CORRELATION_ID_MDC_KEY).isEqualTo("correlationId");
    }
}
