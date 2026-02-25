package com.ftgo.common.logging.masking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PiiMaskingConverter}.
 */
class PiiMaskingConverterTest {

    private static final String MASK = "***MASKED***";

    @Test
    @DisplayName("Masks credit card numbers (16 digits, no separators)")
    void masksCreditCardNumberNoSeparators() {
        String input = "Payment with card 4111111111111111 processed";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("4111111111111111");
        assertThat(result).contains(MASK);
    }

    @Test
    @DisplayName("Masks credit card numbers (with dashes)")
    void masksCreditCardNumberWithDashes() {
        String input = "Card number: 4111-1111-1111-1111";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("4111-1111-1111-1111");
        assertThat(result).contains(MASK);
    }

    @Test
    @DisplayName("Masks credit card numbers (with spaces)")
    void masksCreditCardNumberWithSpaces() {
        String input = "Card: 4111 1111 1111 1111";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("4111 1111 1111 1111");
        assertThat(result).contains(MASK);
    }

    @Test
    @DisplayName("Masks password in key=value format")
    void masksPasswordKeyValue() {
        String input = "User login: password=myS3cretP@ss";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("myS3cretP@ss");
        assertThat(result).contains(MASK);
    }

    @Test
    @DisplayName("Masks password in JSON format")
    void masksPasswordJson() {
        String input = "Request body: {\"password\":\"secret123\"}";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("secret123");
        assertThat(result).contains(MASK);
    }

    @Test
    @DisplayName("Masks Bearer tokens")
    void masksBearerToken() {
        String input = "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.signature";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiJ9");
        assertThat(result).contains("Bearer " + MASK);
    }

    @Test
    @DisplayName("Masks API keys")
    void masksApiKey() {
        String input = "api_key=sk_live_abc123xyz789";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("sk_live_abc123xyz789");
        assertThat(result).contains(MASK);
    }

    @Test
    @DisplayName("Masks SSN")
    void masksSsn() {
        String input = "User SSN: 123-45-6789";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("123-45-6789");
        assertThat(result).contains(MASK);
    }

    @Test
    @DisplayName("Partially masks email addresses")
    void masksEmailPartially() {
        String input = "User email: john.doe@example.com";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("john.doe@example.com");
        assertThat(result).contains("j***@example.com");
    }

    @Test
    @DisplayName("Does not modify messages without sensitive data")
    void doesNotModifyCleanMessages() {
        String input = "Order ORD-123 created successfully for 3 items";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    @DisplayName("Handles null message gracefully")
    void handlesNullMessage() {
        String result = PiiMaskingConverter.maskSensitiveData(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Handles empty message gracefully")
    void handlesEmptyMessage() {
        String result = PiiMaskingConverter.maskSensitiveData("");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Masks multiple sensitive values in single message")
    void masksMultipleSensitiveValues() {
        String input = "User john@example.com with SSN 123-45-6789 paid with card 4111111111111111";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("john@example.com");
        assertThat(result).doesNotContain("123-45-6789");
        assertThat(result).doesNotContain("4111111111111111");
    }

    @ParameterizedTest
    @DisplayName("Masks various password key formats")
    @CsvSource({
            "password=secret123, secret123",
            "passwd=secret123, secret123",
            "pwd=secret123, secret123",
            "secret=mytoken, mytoken",
            "credential=myvalue, myvalue"
    })
    void masksVariousPasswordFormats(String input, String sensitiveValue) {
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain(sensitiveValue);
        assertThat(result).contains(MASK);
    }
}
