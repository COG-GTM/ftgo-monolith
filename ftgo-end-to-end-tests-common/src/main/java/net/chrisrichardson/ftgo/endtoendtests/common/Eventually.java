package net.chrisrichardson.ftgo.endtoendtests.common;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Polling helpers for asserting on asynchronous state transitions in the end-to-end tests.
 * <p>
 * Replaces {@code io.eventuate.util.test.async.Eventually} (Bintray-only artifact) with an
 * Awaitility-backed implementation that keeps the same call sites and semantics: the body is
 * re-run until it stops throwing (an {@link AssertionError} or any other exception counts as
 * "not yet"), or until the overall timeout expires, in which case an
 * {@link org.awaitility.core.ConditionTimeoutException} is thrown with the last failure as its cause.
 * <p>
 * Like the Eventuate original, the polling budget can be tuned via environment variables:
 * {@code EVENTUATE_TEST_ITERATIONS} (default 200) and {@code EVENTUATE_TEST_TIMEOUT_MILLIS}
 * (the pause between attempts, default 500ms).
 */
public final class Eventually {

  /** Number of attempts before giving up. */
  private static final int DEFAULT_ITERATIONS = envInt("EVENTUATE_TEST_ITERATIONS", 200);

  /** Pause between attempts, in milliseconds. */
  private static final int DEFAULT_TIMEOUT_MILLIS = envInt("EVENTUATE_TEST_TIMEOUT_MILLIS", 500);

  private Eventually() {
  }

  /**
   * Re-runs {@code body} until it completes without throwing, using the default iterations/pause.
   *
   * @param message description used in the timeout failure message
   * @param body    assertion block to retry
   */
  public static void eventually(String message, Runnable body) {
    eventually(DEFAULT_ITERATIONS, DEFAULT_TIMEOUT_MILLIS, message, body);
  }

  /**
   * Re-runs {@code body} until it completes without throwing.
   *
   * @param iterations  maximum number of attempts
   * @param pauseMillis pause between attempts, in milliseconds
   * @param message     description used in the timeout failure message
   * @param body        assertion block to retry
   */
  public static void eventually(int iterations, int pauseMillis, String message, Runnable body) {
    condition(iterations, pauseMillis, message).untilAsserted(body::run);
  }

  /**
   * Re-runs {@code body} until it completes without throwing and returns its result,
   * using the default iterations/pause.
   *
   * @param body supplier that asserts and returns a value
   * @return the value produced by the first successful attempt
   */
  public static <T> T eventuallyReturning(Supplier<T> body) {
    return eventuallyReturning(DEFAULT_ITERATIONS, DEFAULT_TIMEOUT_MILLIS, body);
  }

  /**
   * Re-runs {@code body} until it completes without throwing and returns its result.
   *
   * @param iterations  maximum number of attempts
   * @param pauseMillis pause between attempts, in milliseconds
   * @param body        supplier that asserts and returns a value
   * @return the value produced by the first successful attempt
   */
  public static <T> T eventuallyReturning(int iterations, int pauseMillis, Supplier<T> body) {
    // Awaitility's untilAsserted only accepts a void block, so capture the result on success.
    AtomicReference<T> result = new AtomicReference<>();
    condition(iterations, pauseMillis, "eventuallyReturning").untilAsserted(() -> result.set(body.get()));
    return result.get();
  }

  /**
   * Builds the Awaitility condition: total wait = iterations * pause, polling every pause,
   * starting immediately, and treating any exception from the body as a retryable failure.
   */
  private static ConditionFactory condition(int iterations, int pauseMillis, String message) {
    Duration pause = Duration.ofMillis(pauseMillis);
    return Awaitility.await(message)
            .pollDelay(Duration.ZERO)
            .pollInterval(pause)
            .atMost(pause.multipliedBy(iterations))
            .ignoreExceptions();
  }

  /** Reads an integer environment variable, falling back to {@code defaultValue} when unset or malformed. */
  private static int envInt(String name, int defaultValue) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
