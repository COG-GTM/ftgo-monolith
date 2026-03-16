package net.chrisrichardson.ftgo.logging;

import org.slf4j.MDC;

import java.util.Map;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) in multi-threaded
 * environments. Provides helpers to capture and restore MDC context when
 * dispatching work to thread pools or async executors.
 */
public final class MdcContextHolder {

    private MdcContextHolder() {
    }

    /**
     * Captures the current MDC context map for later restoration.
     *
     * @return the current MDC context map, or null if empty
     */
    public static Map<String, String> captureContext() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Restores a previously captured MDC context map.
     *
     * @param contextMap the context map to restore, or null to clear
     */
    public static void restoreContext(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        } else {
            MDC.clear();
        }
    }

    /**
     * Wraps a Runnable so that the current MDC context is propagated
     * to the executing thread.
     *
     * @param runnable the runnable to wrap
     * @return a new runnable that restores MDC context before execution
     */
    public static Runnable wrapWithContext(Runnable runnable) {
        Map<String, String> contextMap = captureContext();
        return () -> {
            Map<String, String> previousContext = captureContext();
            try {
                restoreContext(contextMap);
                runnable.run();
            } finally {
                restoreContext(previousContext);
            }
        };
    }

    /**
     * Sets a value in the MDC context.
     *
     * @param key   the MDC key
     * @param value the value to set
     */
    public static void put(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * Removes a value from the MDC context.
     *
     * @param key the MDC key to remove
     */
    public static void remove(String key) {
        MDC.remove(key);
    }

    /**
     * Gets a value from the MDC context.
     *
     * @param key the MDC key
     * @return the value, or null if not set
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * Clears the entire MDC context.
     */
    public static void clear() {
        MDC.clear();
    }
}
