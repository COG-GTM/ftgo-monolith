package net.chrisrichardson.ftgo.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * AOP aspect that automatically logs method entry and exit for service methods.
 *
 * <p>This aspect intercepts methods in configured packages and logs:
 * <ul>
 *   <li>Method entry with arguments (if configured)</li>
 *   <li>Method exit with execution time</li>
 *   <li>Slow execution warnings when exceeding the configured threshold</li>
 *   <li>Exceptions at ERROR level</li>
 * </ul>
 *
 * <p>The aspect is activated by the presence of {@code spring-boot-starter-aop}
 * on the classpath and the property {@code ftgo.logging.aspect.enabled=true}.
 *
 * <p>Configuration properties:
 * <ul>
 *   <li>{@code ftgo.logging.aspect.enabled} — enable/disable (default: false)</li>
 *   <li>{@code ftgo.logging.aspect.include-args} — log method arguments (default: true)</li>
 *   <li>{@code ftgo.logging.aspect.include-result} — log return value (default: false)</li>
 *   <li>{@code ftgo.logging.aspect.slow-threshold-ms} — slow execution threshold in ms (default: 1000)</li>
 * </ul>
 */
@Aspect
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    private final boolean includeArgs;
    private final boolean includeResult;
    private final long slowThresholdMs;

    /**
     * Creates a new LoggingAspect with the specified configuration.
     *
     * @param includeArgs     whether to include method arguments in entry log
     * @param includeResult   whether to include return value in exit log
     * @param slowThresholdMs threshold in milliseconds for slow execution warning
     */
    public LoggingAspect(boolean includeArgs, boolean includeResult, long slowThresholdMs) {
        this.includeArgs = includeArgs;
        this.includeResult = includeResult;
        this.slowThresholdMs = slowThresholdMs;
    }

    /**
     * Around advice for all public methods in {@code net.chrisrichardson.ftgo} packages
     * annotated with Spring's {@code @Service}, {@code @Component}, or {@code @RestController},
     * or any public method within the ftgo package hierarchy.
     *
     * <p>The pointcut matches all public methods in the {@code net.chrisrichardson.ftgo} package
     * and its sub-packages.
     */
    @Around("execution(public * net.chrisrichardson.ftgo..*.*(..)) "
            + "&& !within(net.chrisrichardson.ftgo.logging..*)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Logger targetLog = LoggerFactory.getLogger(signature.getDeclaringType());

        // Log entry
        if (targetLog.isDebugEnabled()) {
            String entryMessage = buildEntryMessage(className, methodName, joinPoint.getArgs(),
                    signature.getParameterNames());
            targetLog.debug(entryMessage);
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Log exit
            logExit(targetLog, className, methodName, duration, result);

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            targetLog.error("<-- {}.{}() threw {} after {}ms: {}",
                    className, methodName, ex.getClass().getSimpleName(), duration, ex.getMessage());
            throw ex;
        }
    }

    private String buildEntryMessage(String className, String methodName, Object[] args,
                                     String[] paramNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("--> ").append(className).append('.').append(methodName).append('(');

        if (includeArgs && args != null && args.length > 0) {
            StringJoiner joiner = new StringJoiner(", ");
            for (int i = 0; i < args.length; i++) {
                String paramName = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
                joiner.add(paramName + "=" + summarize(args[i]));
            }
            sb.append(joiner);
        }

        sb.append(')');
        return sb.toString();
    }

    private void logExit(Logger targetLog, String className, String methodName,
                         long duration, Object result) {
        if (duration >= slowThresholdMs) {
            targetLog.warn("<-- {}.{}() SLOW execution: {}ms (threshold: {}ms)",
                    className, methodName, duration, slowThresholdMs);
        } else if (targetLog.isDebugEnabled()) {
            if (includeResult && result != null) {
                targetLog.debug("<-- {}.{}() returned [{}] in {}ms",
                        className, methodName, summarize(result), duration);
            } else {
                targetLog.debug("<-- {}.{}() returned in {}ms",
                        className, methodName, duration);
            }
        }
    }

    /**
     * Produces a short string representation of an object for logging.
     * Truncates long strings and arrays to avoid verbose log output.
     */
    private static String summarize(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            String s = (String) obj;
            return s.length() > 100 ? s.substring(0, 100) + "..." : s;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                Object[] arr = (Object[]) obj;
                return arr.length > 5
                        ? Arrays.toString(Arrays.copyOf(arr, 5)) + "...(" + arr.length + " total)"
                        : Arrays.toString(arr);
            }
            return obj.getClass().getSimpleName() + "[...]";
        }
        String str = obj.toString();
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }

    public boolean isIncludeArgs() {
        return includeArgs;
    }

    public boolean isIncludeResult() {
        return includeResult;
    }

    public long getSlowThresholdMs() {
        return slowThresholdMs;
    }
}
