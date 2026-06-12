package net.chrisrichardson.ftgo.endtoendtests.common;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.awaitility.Awaitility.await;

/**
 * Drop-in replacement for {@code io.eventuate.util.test.async.Eventually}, backed by Awaitility.
 *
 * <p>The original dependency ({@code io.eventuate.util:eventuate-util-test}) was only available
 * from now-defunct Bintray/jcenter repositories and no longer resolves, so the small polling
 * helper it provided is reimplemented here on top of Awaitility (managed by the Spring Boot BOM).
 */
public class Eventually {

  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
  private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(500);

  public static void eventually(Runnable runnable) {
    eventually(null, runnable);
  }

  public static void eventually(String message, Runnable runnable) {
    await(message)
            .atMost(DEFAULT_TIMEOUT)
            .pollInterval(DEFAULT_POLL_INTERVAL)
            .ignoreExceptions()
            .untilAsserted(runnable::run);
  }

  public static <T> T eventuallyReturning(Supplier<T> supplier) {
    AtomicReference<T> result = new AtomicReference<>();
    await()
            .atMost(DEFAULT_TIMEOUT)
            .pollInterval(DEFAULT_POLL_INTERVAL)
            .ignoreExceptions()
            .untilAsserted(() -> result.set(supplier.get()));
    return result.get();
  }
}
