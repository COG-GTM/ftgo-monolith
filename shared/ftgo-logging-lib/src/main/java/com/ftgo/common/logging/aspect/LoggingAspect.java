package com.ftgo.common.logging.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Utility class providing method entry/exit logging capabilities.
 *
 * <p>This class provides static helper methods that services can use
 * to log method entry and exit points in a consistent format. It is
 * designed to be used in conjunction with Spring AOP aspects configured
 * per-service, or called directly from service methods.</p>
 *
 * <h3>Why a Utility Instead of an Aspect?</h3>
 * <p>Spring AOP aspects require AspectJ on the classpath and specific
 * {@code @EnableAspectJAutoProxy} configuration. To keep the logging
 * library lightweight and avoid mandatory AOP dependencies, this class
 * provides the logging logic as static methods that can be called from
 * service-specific AOP aspects or directly.</p>
 *
 * <h3>Usage in a Service-Specific Aspect</h3>
 * <pre>{@code
 * @Aspect
 * @Component
 * @ConditionalOnProperty(name = "ftgo.logging.aspect.enabled", havingValue = "true")
 * public class ServiceLoggingAspect {
 *
 *     @Around("within(com.ftgo.order..*)")
 *     public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
 *         return LoggingAspect.logMethodExecution(joinPoint.getTarget().getClass(),
 *                 joinPoint.getSignature().getName(),
 *                 joinPoint.getArgs(),
 *                 joinPoint::proceed);
 *     }
 * }
 * }</pre>
 *
 * <h3>Direct Usage</h3>
 * <pre>{@code
 * public Order createOrder(CreateOrderRequest request) {
 *     LoggingAspect.logEntry(getClass(), "createOrder", request.getOrderId());
 *     try {
 *         Order order = // ... business logic
 *         LoggingAspect.logExit(getClass(), "createOrder", order.getId());
 *         return order;
 *     } catch (Exception e) {
 *         LoggingAspect.logException(getClass(), "createOrder", e);
 *         throw e;
 *     }
 * }
 * }</pre>
 *
 * @see com.ftgo.common.logging.context.LogContext
 */
public final class LoggingAspect {

    private LoggingAspect() {
        // Utility class - prevent instantiation
    }

    /**
     * Logs method entry at DEBUG level.
     *
     * @param clazz      the class containing the method
     * @param methodName the method name
     * @param args       the method arguments (will be summarized, not fully printed)
     */
    public static void logEntry(Class<?> clazz, String methodName, Object... args) {
        Logger logger = LoggerFactory.getLogger(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug(">>> Entering {}.{}() with {} argument(s)",
                    clazz.getSimpleName(), methodName, args != null ? args.length : 0);
        }
    }

    /**
     * Logs method exit at DEBUG level.
     *
     * @param clazz      the class containing the method
     * @param methodName the method name
     * @param result     the return value (type name only, not the full value)
     */
    public static void logExit(Class<?> clazz, String methodName, Object result) {
        Logger logger = LoggerFactory.getLogger(clazz);
        if (logger.isDebugEnabled()) {
            String resultType = result != null ? result.getClass().getSimpleName() : "void";
            logger.debug("<<< Exiting {}.{}() with result type: {}",
                    clazz.getSimpleName(), methodName, resultType);
        }
    }

    /**
     * Logs method exit (void methods) at DEBUG level.
     *
     * @param clazz      the class containing the method
     * @param methodName the method name
     */
    public static void logExit(Class<?> clazz, String methodName) {
        Logger logger = LoggerFactory.getLogger(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("<<< Exiting {}.{}()", clazz.getSimpleName(), methodName);
        }
    }

    /**
     * Logs a method exception at ERROR level.
     *
     * @param clazz      the class containing the method
     * @param methodName the method name
     * @param throwable  the exception that occurred
     */
    public static void logException(Class<?> clazz, String methodName, Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.error("!!! Exception in {}.{}(): {} - {}",
                clazz.getSimpleName(), methodName,
                throwable.getClass().getSimpleName(), throwable.getMessage(),
                throwable);
    }

    /**
     * Logs an external service call at INFO level.
     *
     * @param clazz       the class making the call
     * @param serviceName the name of the external service being called
     * @param operation   the operation being performed
     */
    public static void logExternalCall(Class<?> clazz, String serviceName, String operation) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.info("---> Calling external service: {} operation: {}", serviceName, operation);
    }

    /**
     * Logs external service call completion at INFO level.
     *
     * @param clazz       the class that made the call
     * @param serviceName the name of the external service
     * @param operation   the operation performed
     * @param durationMs  the duration in milliseconds
     */
    public static void logExternalCallComplete(Class<?> clazz, String serviceName,
                                                String operation, long durationMs) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.info("<--- External service: {} operation: {} completed in {}ms",
                serviceName, operation, durationMs);
    }

    /**
     * Logs a business event at INFO level.
     *
     * @param clazz     the class raising the event
     * @param eventName the name of the business event
     * @param details   key details about the event
     */
    public static void logBusinessEvent(Class<?> clazz, String eventName, String details) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.info("[BUSINESS_EVENT] {} - {}", eventName, details);
    }

    /**
     * Wraps a method execution with entry/exit/exception logging.
     *
     * <p>This is designed for use with AOP ProceedingJoinPoint or similar patterns.</p>
     *
     * @param clazz      the class containing the method
     * @param methodName the method name
     * @param args       the method arguments
     * @param execution  the method execution (typically {@code joinPoint::proceed})
     * @param <T>        the return type
     * @return the method result
     * @throws Throwable if the method throws an exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T logMethodExecution(Class<?> clazz, String methodName,
                                            Object[] args, ThrowingSupplier<T> execution) throws Throwable {
        logEntry(clazz, methodName, args);
        long startTime = System.currentTimeMillis();
        try {
            T result = execution.get();
            long duration = System.currentTimeMillis() - startTime;
            Logger logger = LoggerFactory.getLogger(clazz);
            if (logger.isDebugEnabled()) {
                String resultType = result != null ? result.getClass().getSimpleName() : "void";
                logger.debug("<<< Exiting {}.{}() [{}ms] with result type: {}",
                        clazz.getSimpleName(), methodName, duration, resultType);
            }
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - startTime;
            Logger logger = LoggerFactory.getLogger(clazz);
            logger.error("!!! Exception in {}.{}() [{}ms]: {} - {}",
                    clazz.getSimpleName(), methodName, duration,
                    t.getClass().getSimpleName(), t.getMessage(), t);
            throw t;
        }
    }

    /**
     * Functional interface for method execution that may throw.
     *
     * @param <T> the return type
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }
}
