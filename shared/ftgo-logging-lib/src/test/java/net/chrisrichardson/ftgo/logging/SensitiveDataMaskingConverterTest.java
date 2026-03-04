package net.chrisrichardson.ftgo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import net.chrisrichardson.ftgo.logging.masking.SensitiveDataMaskingConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SensitiveDataMaskingConverter}.
 */
class SensitiveDataMaskingConverterTest {

    private final SensitiveDataMaskingConverter converter = new SensitiveDataMaskingConverter();

    @Test
    void shouldMaskValidCreditCardNumber() {
        // Visa test card number (passes Luhn)
        String message = "Payment processed with card 4111111111111111";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertEquals("Payment processed with card ****-****-****-1111", masked);
    }

    @Test
    void shouldMaskCreditCardWithDashes() {
        String message = "Card: 4111-1111-1111-1111";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertEquals("Card: ****-****-****-1111", masked);
    }

    @Test
    void shouldMaskCreditCardWithSpaces() {
        String message = "Card: 4111 1111 1111 1111";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertEquals("Card: ****-****-****-1111", masked);
    }

    @Test
    void shouldNotMaskRandomDigitSequences() {
        // This number does not pass Luhn check
        String message = "Order ID: 1234567890123";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        // Should not be masked if it doesn't pass Luhn
        assertFalse(SensitiveDataMaskingConverter.isValidLuhn("1234567890123"));
        assertEquals(message, masked);
    }

    @Test
    void shouldMaskPasswordInKeyValueFormat() {
        String message = "Login attempt with password=secret123 for user admin";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertEquals("Login attempt with password=******** for user admin", masked);
    }

    @Test
    void shouldMaskPasswordInJsonFormat() {
        String message = "Request body: {\"username\":\"admin\",\"password\":\"myP@ss!\"}";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertTrue(masked.contains("password\":\"********"));
        assertFalse(masked.contains("myP@ss!"));
    }

    @Test
    void shouldMaskPasswordCaseInsensitive() {
        String message = "Config: PASSWORD=supersecret";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertEquals("Config: PASSWORD=********", masked);
    }

    @Test
    void shouldMaskBearerToken() {
        String message = "Authorization header: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.abc.xyz";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertTrue(masked.contains("Bearer ****"));
        assertFalse(masked.contains("eyJhbGci"));
    }

    @Test
    void shouldMaskSsn() {
        String message = "Customer SSN: 123-45-6789";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertEquals("Customer SSN: ***-**-6789", masked);
    }

    @Test
    void shouldMaskAuthorizationHeader() {
        String message = "Header Authorization=Basic dXNlcjpwYXNz";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertTrue(masked.contains("Authorization=********"));
        assertFalse(masked.contains("dXNlcjpwYXNz"));
    }

    @Test
    void shouldHandleNullMessage() {
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        // Formatted message will be null
        String result = converter.convert(event);
        // Should return null without throwing
        assertEquals(null, result);
    }

    @Test
    void shouldNotModifyCleanMessage() {
        String message = "Order 12345 created for customer 678";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertEquals(message, masked);
    }

    @Test
    void shouldMaskMultipleSensitiveValues() {
        String message = "User with SSN 123-45-6789 used card 4111111111111111 and password=secret";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertTrue(masked.contains("***-**-6789"));
        assertTrue(masked.contains("****-****-****-1111"));
        assertTrue(masked.contains("password=********"));
        assertFalse(masked.contains("123-45-6789"));
        assertFalse(masked.contains("4111111111111111"));
        assertFalse(masked.contains("secret"));
    }

    @Test
    void shouldValidateLuhnAlgorithm() {
        // Known valid card numbers
        assertTrue(SensitiveDataMaskingConverter.isValidLuhn("4111111111111111"));  // Visa
        assertTrue(SensitiveDataMaskingConverter.isValidLuhn("5500000000000004"));  // Mastercard
        assertTrue(SensitiveDataMaskingConverter.isValidLuhn("340000000000009"));   // Amex

        // Invalid numbers
        assertFalse(SensitiveDataMaskingConverter.isValidLuhn("1234567890123456"));
        assertFalse(SensitiveDataMaskingConverter.isValidLuhn("0000000000000001"));
    }

    @Test
    void shouldMaskSecretField() {
        String message = "Config secret=mysecretvalue loaded";
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
        assertEquals("Config secret=******** loaded", masked);
    }
}
