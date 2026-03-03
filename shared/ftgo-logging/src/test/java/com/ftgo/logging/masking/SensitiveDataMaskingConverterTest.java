package com.ftgo.logging.masking;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SensitiveDataMaskingConverter}.
 * Verifies that sensitive data patterns are correctly masked in log output.
 */
public class SensitiveDataMaskingConverterTest {

    // -------------------------------------------------------------------------
    // Credit Card Masking
    // -------------------------------------------------------------------------

    @Test
    public void shouldMaskCreditCardNumber() {
        String input = "Payment with card 4111111111111111 processed";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should mask credit card digits", result.contains("************1111"));
        assertFalse("Should not contain full card number", result.contains("4111111111111111"));
    }

    @Test
    public void shouldMaskCreditCardWithDashes() {
        String input = "Card: 4111-1111-1111-1111";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertFalse("Should not contain card with dashes", result.contains("4111-1111-1111-1111"));
    }

    @Test
    public void shouldMaskCreditCardWithSpaces() {
        String input = "Card: 4111 1111 1111 1111";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertFalse("Should not contain card with spaces", result.contains("4111 1111 1111 1111"));
    }

    // -------------------------------------------------------------------------
    // Password Masking
    // -------------------------------------------------------------------------

    @Test
    public void shouldMaskPasswordEqualsValue() {
        String input = "Login attempt with password=secret123 for user admin";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should contain masked password", result.contains("password=********"));
        assertFalse("Should not contain actual password", result.contains("secret123"));
    }

    @Test
    public void shouldMaskPasswordColonValue() {
        String input = "Config: password: mySecret";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertFalse("Should not contain actual password", result.contains("mySecret"));
    }

    @Test
    public void shouldMaskPwdField() {
        String input = "pwd=hunter2";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should mask pwd field", result.contains("pwd=********"));
        assertFalse("Should not contain password value", result.contains("hunter2"));
    }

    @Test
    public void shouldMaskSecretField() {
        String input = "secret=abc123xyz";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should mask secret field", result.contains("secret=********"));
    }

    // -------------------------------------------------------------------------
    // Bearer Token Masking
    // -------------------------------------------------------------------------

    @Test
    public void shouldMaskBearerToken() {
        String input = "Authorization header: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should contain Bearer [MASKED]", result.contains("Bearer [MASKED]"));
        assertFalse("Should not contain JWT", result.contains("eyJhbGciOiJIUzI1NiI"));
    }

    // -------------------------------------------------------------------------
    // API Key Masking
    // -------------------------------------------------------------------------

    @Test
    public void shouldMaskApiKey() {
        String input = "Request with api_key=sk_live_abc123def456";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should mask api_key", result.contains("api_key=[MASKED]"));
        assertFalse("Should not contain API key value", result.contains("sk_live_abc123def456"));
    }

    @Test
    public void shouldMaskApiKeyVariant() {
        String input = "apikey=mySecretKey123";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should mask apikey", result.contains("apikey=[MASKED]"));
    }

    // -------------------------------------------------------------------------
    // Token Masking
    // -------------------------------------------------------------------------

    @Test
    public void shouldMaskAccessToken() {
        String input = "access_token=ya29.a0AfH6SMBx_token_value";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should mask access_token", result.contains("access_token=[MASKED]"));
    }

    @Test
    public void shouldMaskRefreshToken() {
        String input = "refresh_token=1//0dx_refresh_value";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertTrue("Should mask refresh_token", result.contains("refresh_token=[MASKED]"));
    }

    // -------------------------------------------------------------------------
    // Non-sensitive Data (should NOT be masked)
    // -------------------------------------------------------------------------

    @Test
    public void shouldNotMaskNormalMessage() {
        String input = "Order 12345 created successfully for consumer 67890";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertEquals("Normal message should not be modified", input, result);
    }

    @Test
    public void shouldHandleNullMessage() {
        String result = SensitiveDataMaskingConverter.maskSensitiveData(null);
        assertEquals("Null input should return null", null, result);
    }

    @Test
    public void shouldHandleEmptyMessage() {
        String result = SensitiveDataMaskingConverter.maskSensitiveData("");
        assertEquals("Empty input should return empty", "", result);
    }
}
