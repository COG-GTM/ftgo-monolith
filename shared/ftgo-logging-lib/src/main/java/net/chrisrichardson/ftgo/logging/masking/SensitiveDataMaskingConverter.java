package net.chrisrichardson.ftgo.logging.masking;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Logback converter that masks sensitive data patterns in log messages.
 *
 * <p>Automatically detects and masks:
 * <ul>
 *   <li>Credit card numbers (13-19 digit sequences)</li>
 *   <li>Password values in key-value patterns</li>
 *   <li>Bearer tokens</li>
 *   <li>Social Security Numbers (SSN)</li>
 *   <li>Authorization header values</li>
 * </ul>
 *
 * <p>Usage in logback configuration:
 * <pre>
 * &lt;conversionRule conversionWord="maskedMsg"
 *     converterClass="net.chrisrichardson.ftgo.logging.masking.SensitiveDataMaskingConverter"/&gt;
 * &lt;pattern&gt;%d %p %c - %maskedMsg%n&lt;/pattern&gt;
 * </pre>
 */
public class SensitiveDataMaskingConverter extends ClassicConverter {

    /**
     * Matches credit card numbers: 13-19 digit sequences (with optional spaces or dashes).
     * Captures the last 4 digits for partial display.
     */
    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b(\\d[ -]*?){9,15}(\\d{4})\\b");

    /**
     * Matches password-like fields in key=value or key:value or key":" patterns.
     * Supports JSON, query string, and log message formats.
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "((?:password|passwd|pwd|secret|credential)[\"']?\\s*[=:]\\s*[\"']?)([^\",\\s}']+)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches Bearer tokens (typically JWT or OAuth tokens).
     */
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "(Bearer\\s+)[A-Za-z0-9\\-._~+/]+=*",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches Social Security Numbers in XXX-XX-XXXX format.
     */
    private static final Pattern SSN_PATTERN = Pattern.compile(
            "\\b(\\d{3})-(\\d{2})-(\\d{4})\\b");

    /**
     * Matches Authorization header values.
     */
    private static final Pattern AUTH_HEADER_PATTERN = Pattern.compile(
            "(Authorization\\s*[=:]\\s*)[\"']?([^\",}']+)",
            Pattern.CASE_INSENSITIVE);

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
     * @param message the log message to mask
     * @return the message with sensitive data masked
     */
    public static String maskSensitiveData(String message) {
        String masked = message;
        masked = maskCreditCards(masked);
        masked = maskPasswords(masked);
        masked = maskBearerTokens(masked);
        masked = maskSsn(masked);
        masked = maskAuthHeaders(masked);
        return masked;
    }

    private static String maskCreditCards(String message) {
        Matcher matcher = CREDIT_CARD_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String fullMatch = matcher.group();
            String digitsOnly = fullMatch.replaceAll("[\\s-]", "");
            if (digitsOnly.length() >= 13 && digitsOnly.length() <= 19 && isValidLuhn(digitsOnly)) {
                String lastFour = digitsOnly.substring(digitsOnly.length() - 4);
                matcher.appendReplacement(sb, Matcher.quoteReplacement("****-****-****-" + lastFour));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskPasswords(String message) {
        Matcher matcher = PASSWORD_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + "********"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskBearerTokens(String message) {
        Matcher matcher = BEARER_TOKEN_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + "****"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskSsn(String message) {
        Matcher matcher = SSN_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement("***-**-" + matcher.group(3)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskAuthHeaders(String message) {
        Matcher matcher = AUTH_HEADER_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + "********"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Validates a number string using the Luhn algorithm to confirm it's
     * likely a credit card number rather than an arbitrary digit sequence.
     */
    public static boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
