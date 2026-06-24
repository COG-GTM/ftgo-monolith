package io.eventuate.util.test.async;

import java.util.concurrent.Callable;

public class Eventually {

    private static final int DEFAULT_ITERATIONS = 20;
    private static final int DEFAULT_TIMEOUT_MILLIS = 500;

    public static void eventually(Runnable runnable) {
        eventually("", DEFAULT_ITERATIONS, DEFAULT_TIMEOUT_MILLIS, runnable);
    }

    public static void eventually(String message, Runnable runnable) {
        eventually(message, DEFAULT_ITERATIONS, DEFAULT_TIMEOUT_MILLIS, runnable);
    }

    public static void eventually(int iterations, int timeout, Runnable runnable) {
        eventually("", iterations, timeout, runnable);
    }

    public static void eventually(String message, int iterations, int timeout, Runnable runnable) {
        AssertionError lastError = null;
        for (int i = 0; i < iterations; i++) {
            try {
                runnable.run();
                return;
            } catch (AssertionError e) {
                lastError = e;
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted", ie);
                }
            }
        }
        throw lastError;
    }

    public static <T> T eventuallyReturning(Callable<T> callable) {
        return eventuallyReturning(DEFAULT_ITERATIONS, DEFAULT_TIMEOUT_MILLIS, callable);
    }

    public static <T> T eventuallyReturning(int iterations, int timeout, Callable<T> callable) {
        Exception lastException = null;
        for (int i = 0; i < iterations; i++) {
            try {
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted", ie);
                }
            }
        }
        throw new RuntimeException("eventuallyReturning failed after " + iterations + " iterations", lastException);
    }
}
