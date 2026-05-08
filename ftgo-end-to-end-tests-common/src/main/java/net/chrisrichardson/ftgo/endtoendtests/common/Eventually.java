package net.chrisrichardson.ftgo.endtoendtests.common;

import java.util.function.Supplier;

public class Eventually {

  private static final int DEFAULT_ITERATIONS = 30;
  private static final int DEFAULT_DELAY_MS = 1000;

  public static void eventually(Runnable assertion) {
    eventually(null, assertion);
  }

  public static void eventually(String label, Runnable assertion) {
    eventually(label, DEFAULT_ITERATIONS, DEFAULT_DELAY_MS, assertion);
  }

  public static void eventually(int iterations, int delayMs, Runnable assertion) {
    eventually(null, iterations, delayMs, assertion);
  }

  public static void eventually(String label, int iterations, int delayMs, Runnable assertion) {
    Throwable last = null;
    for (int i = 0; i < iterations; i++) {
      try {
        assertion.run();
        return;
      } catch (Throwable t) {
        last = t;
        sleep(delayMs);
      }
    }
    throw new AssertionError(label == null ? "eventually condition not met" : label, last);
  }

  public static <T> T eventuallyReturning(Supplier<T> supplier) {
    return eventuallyReturning(DEFAULT_ITERATIONS, DEFAULT_DELAY_MS, supplier);
  }

  public static <T> T eventuallyReturning(int iterations, int delayMs, Supplier<T> supplier) {
    Throwable last = null;
    for (int i = 0; i < iterations; i++) {
      try {
        T value = supplier.get();
        if (value != null) {
          return value;
        }
      } catch (Throwable t) {
        last = t;
      }
      sleep(delayMs);
    }
    throw new AssertionError("eventuallyReturning failed to produce a non-null value", last);
  }

  private static void sleep(int delayMs) {
    try {
      Thread.sleep(delayMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }
}
