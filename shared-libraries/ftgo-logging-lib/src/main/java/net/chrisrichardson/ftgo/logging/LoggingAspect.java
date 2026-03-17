package net.chrisrichardson.ftgo.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * AOP aspect that provides automatic method entry/exit logging for
 * FTGO service and controller classes. Logs at TRACE level to avoid
 * noise in production while being available for detailed debugging.
 *
 * <p>Covers:
 * <ul>
 *   <li>All public methods in {@code net.chrisrichardson.ftgo..*.service} packages</li>
 *   <li>All public methods in {@code net.chrisrichardson.ftgo..*.web} packages</li>
 * </ul>
 *
 * <p>Entry logs include method name and parameter values.
 * Exit logs include method name and execution time in milliseconds.
 * Exception logs include method name and exception class/message.
 */
@Aspect
public class LoggingAspect {

    /**
     * Around advice for public methods in FTGO service packages.
     *
     * @param joinPoint the join point
     * @return the method return value
     * @throws Throwable if the target method throws
     */
    @Around("execution(public * net.chrisrichardson.ftgo..service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint);
    }

    /**
     * Around advice for public methods in FTGO web/controller packages.
     *
     * @param joinPoint the join point
     * @return the method return value
     * @throws Throwable if the target method throws
     */
    @Around("execution(public * net.chrisrichardson.ftgo..web..*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint);
    }

    private Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();

        if (logger.isTraceEnabled()) {
            logger.trace("Entering {}.{}() with arguments={}",
                    signature.getDeclaringType().getSimpleName(),
                    methodName,
                    Arrays.toString(joinPoint.getArgs()));
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (logger.isTraceEnabled()) {
                logger.trace("Exiting {}.{}() [{}ms]",
                        signature.getDeclaringType().getSimpleName(),
                        methodName,
                        duration);
            }

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Exception in {}.{}() [{}ms]: {}",
                    signature.getDeclaringType().getSimpleName(),
                    methodName,
                    duration,
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }
}
