package net.chrisrichardson.ftgo.endtoendtests.common;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the Awaitility-backed {@link Eventually} keeps the retry semantics of the
 * Eventuate helper it replaces: assertion failures and exceptions are retried until success,
 * results are returned from the first successful attempt, and exhausting the budget fails.
 */
class EventuallyTest {

  @Test
  void eventuallyRetriesUntilAssertionPasses() {
    AtomicInteger attempts = new AtomicInteger();
    // Fails on the first two attempts, passes on the third.
    Eventually.eventually(10, 10, "retries", () -> assertEquals(3, attempts.incrementAndGet()));
    assertThat(attempts.get()).isEqualTo(3);
  }

  @Test
  void eventuallyRetriesOnExceptionsToo() {
    AtomicInteger attempts = new AtomicInteger();
    // Runtime exceptions (e.g. connection refused before the app is up) count as "not yet".
    Eventually.eventually(10, 10, "exceptions", () -> {
      if (attempts.incrementAndGet() < 2) {
        throw new IllegalStateException("not ready");
      }
    });
    assertThat(attempts.get()).isEqualTo(2);
  }

  @Test
  void eventuallyReturningReturnsValueFromFirstSuccessfulAttempt() {
    AtomicInteger attempts = new AtomicInteger();
    int result = Eventually.eventuallyReturning(10, 10, () -> {
      int n = attempts.incrementAndGet();
      assertThat(n).isGreaterThanOrEqualTo(2);
      return n * 100;
    });
    assertThat(result).isEqualTo(200);
  }

  @Test
  void eventuallyFailsOnceTheBudgetIsExhausted() {
    // A body that never passes must surface the timeout rather than spin forever.
    assertThatThrownBy(() -> Eventually.eventually(3, 10, "never", () -> assertEquals(1, 2)))
            .isInstanceOf(ConditionTimeoutException.class)
            .hasMessageContaining("never");
  }
}
