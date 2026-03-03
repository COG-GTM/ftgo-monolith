package com.ftgo.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AOP aspect that provides automatic method entry/exit logging for FTGO services.
 *
 * <p>This aspect intercepts public methods in classes annotated with {@code @Loggable},
 * or methods themselves annotated with {@code @Loggable}, and logs:</p>
 * <ul>
 *   <li><strong>Entry</strong>: Method name and parameter types (not values, to avoid PII)</li>
 *   <li><strong>Exit</strong>: Method name, return type, and execution duration in milliseconds</li>
 *   <li><strong>Exception</strong>: Method name, exception type, and message at WARN level</li>
 * </ul>
 *
 * <p>All entry/exit logging is done at {@code DEBUG} level to minimize production overhead.
 * Exception logging is done at {@code WARN} level.</p>
 *
 * <p>Parameter values are intentionally NOT logged to prevent accidental PII/sensitive
 * data exposure. Only parameter types are included in entry logs.</p>
 */
@Aspect
public class LoggingAspect {

    /**
     * Pointcut matching all public methods in classes annotated with @Loggable.
     */
    @Pointcut("@within(com.ftgo.logging.annotation.Loggable) && execution(public * *(..))")
    public void loggableClass() {
        // Pointcut definition - no implementation needed
    }

    /**
     * Pointcut matching methods directly annotated with @Loggable.
     */
    @Pointcut("@annotation(com.ftgo.logging.annotation.Loggable) && execution(public * *(..))")
    public void loggableMethod() {
        // Pointcut definition - no implementation needed
    }

    /**
     * Around advice that logs method entry, exit, and exceptions.
     *
     * @param joinPoint the join point representing the intercepted method
     * @return the method return value
     * @throws Throwable if the method throws an exception
     */
    @Around("loggableClass() || loggableMethod()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        String methodName = signature.getName();
        String parameterTypes = buildParameterTypeString(signature);

        if (log.isDebugEnabled()) {
            log.debug("Entering {}.{}({})", signature.getDeclaringType().getSimpleName(),
                    methodName, parameterTypes);
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (log.isDebugEnabled()) {
                String returnType = signature.getReturnType().getSimpleName();
                log.debug("Exiting {}.{}() returned [{}] in {}ms",
                        signature.getDeclaringType().getSimpleName(),
                        methodName, returnType, duration);
            }

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("Exception in {}.{}() after {}ms: {} - {}",
                    signature.getDeclaringType().getSimpleName(),
                    methodName, duration, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    /**
     * Builds a string of parameter types for the method signature.
     * Only types are included (not values) to avoid logging sensitive data.
     */
    private String buildParameterTypeString(MethodSignature signature) {
        Class<?>[] paramTypes = signature.getParameterTypes();
        if (paramTypes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(paramTypes[i].getSimpleName());
        }
        return sb.toString();
    }
}
