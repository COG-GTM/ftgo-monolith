package net.chrisrichardson.ftgo.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.test.simple.SimpleSpan;
import io.micrometer.tracing.test.simple.SimpleTracer;
import net.chrisrichardson.ftgo.tracing.span.DeliveryTracingOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DeliveryTracingOperations}.
 */
class DeliveryTracingOperationsTest {

    private SimpleTracer tracer;
    private DeliveryTracingOperations ops;

    @BeforeEach
    void setUp() {
        tracer = new SimpleTracer();
        ops = new DeliveryTracingOperations(tracer);
    }

    @Test
    void shouldStartDeliveryAssignmentSpan() {
        Span span = ops.startDeliveryAssignment("DEL-001", "COU-100");
        assertNotNull(span);
        ops.endSpan(span);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        assertEquals(1, spans.size());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("delivery.assign", simpleSpan.getName());
        assertEquals("assign", simpleSpan.getTags().get("delivery.operation"));
        assertEquals("DEL-001", simpleSpan.getTags().get("delivery.id"));
        assertEquals("COU-100", simpleSpan.getTags().get("courier.id"));
    }

    @Test
    void shouldStartDeliveryPickupSpan() {
        Span span = ops.startDeliveryPickup("DEL-002");
        ops.endSpan(span);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("delivery.pickup", simpleSpan.getName());
        assertEquals("pickup", simpleSpan.getTags().get("delivery.operation"));
    }

    @Test
    void shouldStartDeliveryCompletionSpan() {
        Span span = ops.startDeliveryCompletion("DEL-003");
        ops.endSpan(span);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("delivery.complete", simpleSpan.getName());
        assertEquals("complete", simpleSpan.getTags().get("delivery.operation"));
    }

    @Test
    void shouldStartCourierAvailabilityUpdateSpan() {
        Span span = ops.startCourierAvailabilityUpdate("COU-200", true);
        ops.endSpan(span);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("courier.availability", simpleSpan.getName());
        assertEquals("COU-200", simpleSpan.getTags().get("courier.id"));
        assertEquals("true", simpleSpan.getTags().get("courier.available"));
    }

    @Test
    void shouldRecordErrorOnDeliverySpan() {
        Span span = ops.startDeliveryAssignment("DEL-ERR", "COU-ERR");
        RuntimeException error = new RuntimeException("assignment failed");
        ops.endSpanWithError(span, error);

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        SimpleSpan simpleSpan = spans.get(0);
        assertEquals("delivery.assign", simpleSpan.getName());
        assertTrue(simpleSpan.getError() instanceof RuntimeException);
    }
}
