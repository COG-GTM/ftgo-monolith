package net.chrisrichardson.ftgo.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.test.simple.SimpleSpan;
import io.micrometer.tracing.test.simple.SimpleTracer;
import net.chrisrichardson.ftgo.tracing.span.OrderTracingOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link OrderTracingOperations}.
 */
class OrderTracingOperationsTest {

    private SimpleTracer tracer;
    private OrderTracingOperations ops;

    @BeforeEach
    void setUp() {
        tracer = new SimpleTracer();
        ops = new OrderTracingOperations(tracer);
    }

    @Test
    void shouldStartOrderCreationSpan() {
        Span span = ops.startOrderCreation();
        assertNotNull(span);
        ops.endSpan(span);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        assertEquals(1, spans.size());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("order.create", simpleSpan.getName());
        assertEquals("create", simpleSpan.getTags().get("order.operation"));
    }

    @Test
    void shouldStartOrderCreationSpanWithId() {
        Span span = ops.startOrderCreation("ORD-001");
        ops.endSpan(span);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("order.create", simpleSpan.getName());
        assertEquals("ORD-001", simpleSpan.getTags().get("order.id"));
    }

    @Test
    void shouldStartOrderCancellationSpan() {
        Span span = ops.startOrderCancellation("ORD-002");
        ops.endSpan(span);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("order.cancel", simpleSpan.getName());
        assertEquals("cancel", simpleSpan.getTags().get("order.operation"));
        assertEquals("ORD-002", simpleSpan.getTags().get("order.id"));
    }

    @Test
    void shouldStartOrderApprovalSpan() {
        Span span = ops.startOrderApproval("ORD-003");
        ops.endSpan(span);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("order.approve", simpleSpan.getName());
        assertEquals("approve", simpleSpan.getTags().get("order.operation"));
    }

    @Test
    void shouldRecordErrorOnSpan() {
        Span span = ops.startOrderCreation("ORD-ERR");
        RuntimeException error = new RuntimeException("order failed");
        ops.endSpanWithError(span, error);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("order.create", simpleSpan.getName());
        assertTrue(simpleSpan.getError() instanceof RuntimeException);
    }
}
