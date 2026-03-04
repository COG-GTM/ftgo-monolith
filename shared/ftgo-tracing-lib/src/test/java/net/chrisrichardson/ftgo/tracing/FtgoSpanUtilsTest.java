package net.chrisrichardson.ftgo.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.test.simple.SimpleSpan;
import io.micrometer.tracing.test.simple.SimpleTracer;
import net.chrisrichardson.ftgo.tracing.span.FtgoSpanUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FtgoSpanUtils}.
 */
class FtgoSpanUtilsTest {

    private SimpleTracer tracer;

    @BeforeEach
    void setUp() {
        tracer = new SimpleTracer();
    }

    @Test
    void shouldExecuteRunnableInSpan() {
        AtomicBoolean executed = new AtomicBoolean(false);

        FtgoSpanUtils.executeInSpan(tracer, "test.operation", () -> {
            executed.set(true);
        });

        assertTrue(executed.get());
        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        assertEquals(1, spans.size());
        assertEquals("test.operation", spans.get(0).getName());
    }

    @Test
    void shouldExecuteSupplierInSpan() {
        String result = FtgoSpanUtils.executeInSpan(tracer, "test.supplier", () -> "hello");

        assertEquals("hello", result);
        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        assertEquals(1, spans.size());
        assertEquals("test.supplier", spans.get(0).getName());
    }

    @Test
    void shouldRecordErrorInSpanOnException() {
        assertThrows(RuntimeException.class, () -> {
            FtgoSpanUtils.executeInSpan(tracer, "test.error", () -> {
                throw new RuntimeException("test error");
            });
        });

        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        assertEquals(1, spans.size());
        SimpleSpan span = spans.get(0);
        assertEquals("test.error", span.getName());
        assertTrue(span.getError() instanceof RuntimeException);
    }

    @Test
    void shouldExecuteRunnableInSpanWithTags() {
        AtomicBoolean executed = new AtomicBoolean(false);

        FtgoSpanUtils.executeInSpan(tracer, "test.tagged", "order.id", "123", () -> {
            executed.set(true);
        });

        assertTrue(executed.get());
        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        assertEquals(1, spans.size());
        SimpleSpan span = spans.get(0);
        assertEquals("test.tagged", span.getName());
        assertEquals("123", span.getTags().get("order.id"));
    }

    @Test
    void shouldExecuteSupplierInSpanWithTags() {
        String result = FtgoSpanUtils.executeInSpan(tracer, "test.tagged-supplier",
                "delivery.id", "456", () -> "world");

        assertEquals("world", result);
        List<SimpleSpan> spans = new ArrayList<>(tracer.getSpans());
        assertEquals(1, spans.size());
        SimpleSpan span = spans.get(0);
        assertEquals("test.tagged-supplier", span.getName());
        assertEquals("456", span.getTags().get("delivery.id"));
    }
}
