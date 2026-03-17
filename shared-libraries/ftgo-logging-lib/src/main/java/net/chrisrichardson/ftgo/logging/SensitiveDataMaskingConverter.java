package net.chrisrichardson.ftgo.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Logback converter that masks sensitive data patterns in log messages.
 * Handles credit card numbers, SSNs, passwords, bearer tokens, and email addresses.
 *
 * <p>Register in logback-spring.xml with:
 * <pre>{@code
 * <conversionRule conversionWord="maskedMsg"
 *     converterClass="net.chrisrichardson.ftgo.logging.SensitiveDataMaskingConverter"/>
 * }</pre>
 *
 * <p>Then use {@code %maskedMsg} in place of {@code %msg} in pattern layouts.
 */
public class SensitiveDataMaskingConverter extends ClassicConverter {

    /**
     * Matches 13-19 digit sequences that look like credit card numbers,
     * with optional dashes or spaces between groups of 4.
     */
    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b(\\d{4})[- ]?(\\d{2})\\d{2}[- ]?\\d{4}[- ]?(\\d{4})\\b");

    /**
     * Matches US Social Security Numbers in XXX-XX-XXXX format.
     */
    private static final Pattern SSN_PATTERN =
            Pattern.compile("\\b\\d{3}-\\d{2}-(\\d{4})\\b");

    /**
     * Matches password-like key=value pairs (case-insensitive keys).
     * Handles: password, passwd, pwd, secret, credential, apiKey, api_key, token (as key names).
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("((?:password|passwd|pwd|secret|credential|apiKey|api_key|token)\\s*[=:]\\s*)(\\S+)",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Matches Bearer tokens in authorization headers.
     */
    private static final Pattern BEARER_TOKEN_PATTERN =
            Pattern.compile("(Bearer\\s+)(\\S+)", Pattern.CASE_INSENSITIVE);

    /**
     * Matches email addresses.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b([a-zA-Z0-9._%+-])[a-zA-Z0-9._%+-]*(@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\b");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isEmpty()) {
            return message;
        }
        return maskSensitiveData(message);
    }

    /**
     * Applies all masking patterns to the given message.
     *
     * @param message the original log message
     * @return the message with sensitive data masked
     */
    static String maskSensitiveData(String message) {
        String masked = message;
        masked = maskCreditCards(masked);
        masked = maskSsn(masked);
        masked = maskPasswords(masked);
        masked = maskBearerTokens(masked);
        masked = maskEmails(masked);
        return masked;
    }

    private static String maskCreditCards(String message) {
        Matcher matcher = CREDIT_CARD_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = matcher.group(1) + matcher.group(2) + "******" + matcher.group(3);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskSsn(String message) {
        Matcher matcher = SSN_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "***-**-" + matcher.group(1));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskPasswords(String message) {
        Matcher matcher = PASSWORD_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1)) + "********");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskBearerTokens(String message) {
        Matcher matcher = BEARER_TOKEN_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1)) + "[MASKED]");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskEmails(String message) {
        Matcher matcher = EMAIL_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "***" + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
