# Resilience Patterns

## Overview

The `ftgo-resilience-lib` shared library provides standardized resilience patterns for inter-service communication using [Resilience4j](https://resilience4j.readme.io/) (v0.13.x). It implements four key patterns: **Circuit Breaker**, **Retry with Exponential Backoff**, **Bulkhead (Thread Pool Isolation)**, and **Rate Limiter**.

## Library Structure

```
shared-libraries/ftgo-resilience-lib/
└── src/main/java/net/chrisrichardson/ftgo/resilience/
    ├── ResilientServiceCall.java                  # Combined resilience decorator
    ├── config/
    │   └── ResilienceAutoConfiguration.java       # Spring auto-configuration
    ├── circuitbreaker/
    │   ├── CircuitBreakerConfigurationProperties.java
    │   └── CircuitBreakerFactory.java
    ├── retry/
    │   ├── RetryConfigurationProperties.java
    │   └── RetryFactory.java
    ├── bulkhead/
    │   ├── BulkheadConfigurationProperties.java
    │   └── BulkheadFactory.java
    └── ratelimiter/
        ├── RateLimiterConfigurationProperties.java
        └── RateLimiterFactory.java
```

## Adding the Dependency

In your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-libraries:ftgo-resilience-lib')
}
```

Import the configuration in your service's Spring configuration:

```java
@Configuration
@Import(ResilienceAutoConfiguration.class)
public class MyServiceConfiguration {
    // ...
}
```

## Circuit Breaker

### How It Works

The circuit breaker monitors the success/failure rate of calls to a downstream service and prevents cascading failures by short-circuiting calls when the failure rate exceeds a threshold.

```
CLOSED ──(failure rate > threshold)──> OPEN
  ^                                       |
  |                                       v
  └──(success in half-open)── HALF_OPEN <─┘
                                (after wait duration)
```

### Default Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.resilience.circuitbreaker.failure-rate-threshold` | 50% | Failure rate to trip the circuit |
| `ftgo.resilience.circuitbreaker.ring-buffer-size-closed` | 10 | Calls tracked in CLOSED state |
| `ftgo.resilience.circuitbreaker.ring-buffer-size-half-open` | 5 | Calls allowed in HALF_OPEN state |
| `ftgo.resilience.circuitbreaker.wait-duration-open-ms` | 30000 | Time to wait before HALF_OPEN |

### Usage

```java
@Service
public class OrderServiceClient {

    private final CircuitBreakerFactory circuitBreakerFactory;

    public OrderServiceClient(CircuitBreakerFactory circuitBreakerFactory) {
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public OrderResponse getOrder(long orderId) {
        CircuitBreaker cb = circuitBreakerFactory.getCircuitBreaker("order-service");
        Supplier<OrderResponse> decorated = CircuitBreaker.decorateSupplier(cb,
            () -> restTemplate.getForObject("/orders/" + orderId, OrderResponse.class));
        return Try.ofSupplier(decorated)
            .recover(throwable -> OrderResponse.fallback())
            .get();
    }
}
```

### Per-Service Overrides

```java
CircuitBreakerConfigurationProperties props = new CircuitBreakerConfigurationProperties();
props.setFailureRateThreshold(70.0f);
props.setWaitDurationInOpenStateMillis(60000L);
CircuitBreaker cb = circuitBreakerFactory.getCircuitBreaker("payment-service", props);
```

## Retry with Exponential Backoff

### How It Works

The retry pattern automatically retries failed operations with increasing wait times between attempts, preventing thundering herd problems during transient failures.

```
Attempt 1 ──(fail)──> wait 500ms ──> Attempt 2 ──(fail)──> wait 1000ms ──> Attempt 3
```

### Default Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.resilience.retry.max-attempts` | 3 | Maximum number of attempts |
| `ftgo.resilience.retry.initial-interval-ms` | 500 | Initial wait between retries |
| `ftgo.resilience.retry.multiplier` | 2.0 | Backoff multiplier |

With defaults, retry intervals are: 500ms, 1000ms (then give up).

### Usage

```java
Retry retry = retryFactory.getRetry("consumer-service");
Supplier<ConsumerResponse> decorated = Retry.decorateSupplier(retry,
    () -> restTemplate.getForObject("/consumers/" + id, ConsumerResponse.class));
ConsumerResponse result = Try.ofSupplier(decorated).get();
```

## Bulkhead (Thread Pool Isolation)

### How It Works

The bulkhead pattern limits the number of concurrent calls to a downstream service, preventing one slow service from consuming all available threads.

```
Incoming requests ──> Bulkhead (max 25 concurrent) ──> Downstream Service
                          |
                          └── Excess requests wait (max 500ms) or are rejected
```

### Default Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.resilience.bulkhead.max-concurrent-calls` | 25 | Max parallel calls |
| `ftgo.resilience.bulkhead.max-wait-time-ms` | 500 | Max time to wait for a permit |

### Usage

```java
Bulkhead bulkhead = bulkheadFactory.getBulkhead("restaurant-service");
Supplier<RestaurantResponse> decorated = Bulkhead.decorateSupplier(bulkhead,
    () -> restTemplate.getForObject("/restaurants/" + id, RestaurantResponse.class));
RestaurantResponse result = Try.ofSupplier(decorated).get();
```

## Rate Limiter

### How It Works

The rate limiter controls the rate of outgoing calls to downstream services, preventing services from being overwhelmed.

```
Requests ──> Rate Limiter (50 per second) ──> Downstream Service
                 |
                 └── Excess requests wait (max 500ms) or are rejected
```

### Default Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.resilience.ratelimiter.limit-for-period` | 50 | Calls per refresh period |
| `ftgo.resilience.ratelimiter.limit-refresh-period-ms` | 1000 | Refresh period |
| `ftgo.resilience.ratelimiter.timeout-duration-ms` | 500 | Max wait for permission |

### Usage

```java
RateLimiter rl = rateLimiterFactory.getRateLimiter("courier-service");
Supplier<CourierResponse> decorated = RateLimiter.decorateSupplier(rl,
    () -> restTemplate.getForObject("/couriers/" + id, CourierResponse.class));
CourierResponse result = Try.ofSupplier(decorated).get();
```

## Combined Resilience Pattern (ResilientServiceCall)

The `ResilientServiceCall` class combines all four patterns into a single decorated call. This is the recommended approach for inter-service communication.

### Decoration Order

```
RateLimiter -> Bulkhead -> CircuitBreaker -> Retry -> Actual Call
```

This order ensures:
1. **Rate limiter** controls overall throughput
2. **Bulkhead** limits concurrency
3. **Circuit breaker** prevents calls to failing services
4. **Retry** handles transient failures

### Usage

```java
@Service
public class ConsumerServiceClient {

    private final ResilientServiceCall resilientCall;
    private final RestTemplate restTemplate;

    @Value("${ftgo.services.consumer-service.url}")
    private String consumerServiceUrl;

    public ConsumerServiceClient(CircuitBreakerFactory cbFactory,
                                  RetryFactory retryFactory,
                                  BulkheadFactory bhFactory,
                                  RateLimiterFactory rlFactory,
                                  RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.resilientCall = new ResilientServiceCall(
            cbFactory.getCircuitBreaker("consumer-service"),
            retryFactory.getRetry("consumer-service"),
            bhFactory.getBulkhead("consumer-service"),
            rlFactory.getRateLimiter("consumer-service")
        );
    }

    public ConsumerResponse getConsumer(long consumerId) {
        return resilientCall.executeWithFallback(
            () -> restTemplate.getForObject(
                consumerServiceUrl + "/consumers/" + consumerId,
                ConsumerResponse.class),
            ConsumerResponse.defaultFallback()
        );
    }
}
```

## Recommended Settings by Service

| Downstream Service | Circuit Breaker Threshold | Retry Attempts | Bulkhead Max Concurrent | Rate Limit |
|-------------------|--------------------------|----------------|------------------------|------------|
| order-service | 50% | 3 | 25 | 50/s |
| consumer-service | 50% | 3 | 20 | 40/s |
| restaurant-service | 50% | 3 | 20 | 40/s |
| courier-service | 50% | 2 | 15 | 30/s |
| api-gateway | 60% | 2 | 50 | 100/s |

## Monitoring

### Resilience4j Metrics

Each Resilience4j component publishes metrics that can be monitored. Log events are automatically emitted at the following levels:

| Event | Log Level |
|-------|-----------|
| Circuit breaker state change | INFO |
| Retry attempt | DEBUG |
| Bulkhead rejection | WARN |
| Rate limiter rejection | WARN |
| Custom config creation | INFO |

### Key Metrics to Monitor

- Circuit breaker state transitions (CLOSED -> OPEN -> HALF_OPEN)
- Retry attempt counts per service
- Bulkhead rejection rate
- Rate limiter wait times

## Troubleshooting

| Symptom | Likely Cause | Resolution |
|---------|-------------|------------|
| All calls failing immediately | Circuit breaker in OPEN state | Wait for `waitDurationInOpenState` or check downstream service health |
| High latency on some calls | Bulkhead contention (waiting for permit) | Increase `maxConcurrentCalls` or decrease call duration |
| Requests rejected with 429 | Rate limiter throttling | Increase `limitForPeriod` or `limitRefreshPeriod` |
| Excessive retries causing load | Retry storm on downstream service | Reduce `maxAttempts` or increase backoff `multiplier` |
