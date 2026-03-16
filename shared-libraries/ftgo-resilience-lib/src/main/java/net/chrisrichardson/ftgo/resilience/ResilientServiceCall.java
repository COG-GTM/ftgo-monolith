package net.chrisrichardson.ftgo.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Combines multiple resilience patterns (circuit breaker, retry, bulkhead, rate limiter)
 * into a single decorated service call. This is the primary entry point for services
 * that need resilient inter-service communication.
 *
 * <p>The decoration order follows Resilience4j best practices:
 * RateLimiter -> Bulkhead -> CircuitBreaker -> Retry -> Supplier</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * ResilientServiceCall resilientCall = new ResilientServiceCall(
 *     circuitBreakerFactory.getCircuitBreaker("order-service"),
 *     retryFactory.getRetry("order-service"),
 *     bulkheadFactory.getBulkhead("order-service"),
 *     rateLimiterFactory.getRateLimiter("order-service")
 * );
 *
 * Try&lt;OrderResponse&gt; result = resilientCall.execute(() -&gt; orderClient.getOrder(orderId));
 * </pre>
 */
public class ResilientServiceCall {

    private static final Logger logger = LoggerFactory.getLogger(ResilientServiceCall.class);

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final Bulkhead bulkhead;
    private final RateLimiter rateLimiter;

    /**
     * Creates a resilient service call with all four resilience patterns.
     */
    public ResilientServiceCall(CircuitBreaker circuitBreaker,
                                Retry retry,
                                Bulkhead bulkhead,
                                RateLimiter rateLimiter) {
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
        this.bulkhead = bulkhead;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Creates a resilient service call with circuit breaker and retry only.
     */
    public ResilientServiceCall(CircuitBreaker circuitBreaker, Retry retry) {
        this(circuitBreaker, retry, null, null);
    }

    /**
     * Executes a supplier with all configured resilience patterns applied.
     *
     * @param supplier the operation to execute
     * @param <T>      the return type
     * @return a Try containing the result or the failure
     */
    public <T> Try<T> execute(Supplier<T> supplier) {
        Supplier<T> decorated = supplier;

        if (retry != null) {
            decorated = Retry.decorateSupplier(retry, decorated);
        }
        if (circuitBreaker != null) {
            decorated = CircuitBreaker.decorateSupplier(circuitBreaker, decorated);
        }
        if (bulkhead != null) {
            decorated = Bulkhead.decorateSupplier(bulkhead, decorated);
        }
        if (rateLimiter != null) {
            decorated = RateLimiter.decorateSupplier(rateLimiter, decorated);
        }

        Try<T> result = Try.ofSupplier(decorated);

        if (result.isFailure()) {
            logger.warn("Resilient service call failed: {}", result.getCause().getMessage());
        }

        return result;
    }

    /**
     * Executes a supplier with all configured resilience patterns applied,
     * returning a fallback value on failure.
     *
     * @param supplier the operation to execute
     * @param fallback the fallback value on failure
     * @param <T>      the return type
     * @return the result or the fallback value
     */
    public <T> T executeWithFallback(Supplier<T> supplier, T fallback) {
        Try<T> result = execute(supplier);
        if (result.isFailure()) {
            logger.info("Using fallback value due to: {}", result.getCause().getMessage());
            return fallback;
        }
        return result.get();
    }

    /**
     * Executes a runnable with all configured resilience patterns applied.
     *
     * @param runnable the operation to execute
     * @return a Try representing success or failure
     */
    public Try<Void> executeRunnable(Runnable runnable) {
        return execute(() -> {
            runnable.run();
            return null;
        });
    }
}
