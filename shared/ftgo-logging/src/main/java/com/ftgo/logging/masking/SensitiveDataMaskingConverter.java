package com.ftgo.logging.masking;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Logback converter that masks sensitive data in log messages.
 *
 * <p>Automatically detects and masks the following patterns:</p>
 * <ul>
 *   <li>Credit card numbers (13-19 digit sequences matching Luhn-plausible formats)</li>
 *   <li>Password fields (key=value patterns where key contains "password", "passwd", "pwd", "secret")</li>
 *   <li>Bearer tokens (Bearer followed by token string)</li>
 *   <li>Authorization headers (Authorization header values)</li>
 *   <li>API keys (key=value patterns where key contains "apikey", "api_key", "api-key")</li>
 *   <li>Token fields (key=value patterns where key contains "token", "access_token", "refresh_token")</li>
 * </ul>
 *
 * <p>Usage in logback configuration:</p>
 * <pre>
 * &lt;conversionRule conversionWord="maskedMsg"
 *     converterClass="com.ftgo.logging.masking.SensitiveDataMaskingConverter" /&gt;
 *
 * &lt;pattern&gt;%d %-5level %logger - %maskedMsg%n&lt;/pattern&gt;
 * </pre>
 */
public class SensitiveDataMaskingConverter extends ClassicConverter {

    // Credit card pattern: 13-19 digits, optionally separated by spaces or dashes
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b([0-9]{4})[- ]?([0-9]{4})[- ]?([0-9]{4})[- ]?([0-9]{1,7})\\b"
    );

    // Password-like fields in key=value format (case-insensitive key matching)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "((?:password|passwd|pwd|secret)[\\s]*[=:][\\s]*)([^\\s,;\"'}{\\]\\)]+)",
            Pattern.CASE_INSENSITIVE
    );

    // Bearer token pattern
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "(Bearer\\s+)([A-Za-z0-9\\-._~+/]+=*)",
            Pattern.CASE_INSENSITIVE
    );

    // Authorization header pattern
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile(
            "(Authorization[\\s]*[=:]\\s*)([^\\s,;\"'}{\\]\\)]+)",
            Pattern.CASE_INSENSITIVE
    );

    // API key pattern
    private static final Pattern API_KEY_PATTERN = Pattern.compile(
            "((?:api[_-]?key|apikey)[\\s]*[=:][\\s]*)([^\\s,;\"'}{\\]\\)]+)",
            Pattern.CASE_INSENSITIVE
    );

    // Token pattern (generic token fields)
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "((?:access[_-]?token|refresh[_-]?token|token)[\\s]*[=:][\\s]*)([^\\s,;\"'}{\\]\\)]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final String MASKED = "[MASKED]";
    private static final String MASKED_PASSWORD = "********";

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
        if (message == null || message.isEmpty()) {
            return message;
        }
        String masked = message;
        masked = maskCreditCards(masked);
        masked = maskPasswords(masked);
        masked = maskBearerTokens(masked);
        masked = maskAuthorization(masked);
        masked = maskApiKeys(masked);
        masked = maskTokens(masked);
        return masked;
    }

    private static String maskCreditCards(String message) {
        Matcher matcher = CREDIT_CARD_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String fullMatch = matcher.group(0).replaceAll("[- ]", "");
            // Only mask if it looks like a plausible card number (13-19 digits)
            if (fullMatch.length() >= 13 && fullMatch.length() <= 19) {
                String lastFour = fullMatch.substring(fullMatch.length() - 4);
                String mask = repeat("*", fullMatch.length() - 4) + lastFour;
                matcher.appendReplacement(sb, Matcher.quoteReplacement(mask));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskPasswords(String message) {
        return replacePattern(message, PASSWORD_PATTERN, MASKED_PASSWORD);
    }

    private static String maskBearerTokens(String message) {
        Matcher matcher = BEARER_TOKEN_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + MASKED));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskAuthorization(String message) {
        return replacePattern(message, AUTHORIZATION_PATTERN, MASKED);
    }

    private static String maskApiKeys(String message) {
        return replacePattern(message, API_KEY_PATTERN, MASKED);
    }

    private static String maskTokens(String message) {
        return replacePattern(message, TOKEN_PATTERN, MASKED);
    }

    private static String replacePattern(String message, Pattern pattern, String replacement) {
        Matcher matcher = pattern.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
