package com.ftgo.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        JwtProperties properties = new JwtProperties();

        assertThat(properties.getSecret()).isEmpty();
        assertThat(properties.getExpiration()).isEqualTo(3600000);
        assertThat(properties.getRefreshThreshold()).isEqualTo(300000);
        assertThat(properties.getIssuer()).isEqualTo("ftgo-platform");
        assertThat(properties.getHeader()).isEqualTo("Authorization");
        assertThat(properties.getPrefix()).isEqualTo("Bearer ");
    }

    @Test
    void shouldSetAndGetSecret() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("my-secret-key-that-is-long-enough-for-hmac-sha256");

        assertThat(properties.getSecret()).isEqualTo("my-secret-key-that-is-long-enough-for-hmac-sha256");
    }

    @Test
    void shouldSetAndGetExpiration() {
        JwtProperties properties = new JwtProperties();
        properties.setExpiration(7200000);

        assertThat(properties.getExpiration()).isEqualTo(7200000);
    }

    @Test
    void shouldSetAndGetRefreshThreshold() {
        JwtProperties properties = new JwtProperties();
        properties.setRefreshThreshold(600000);

        assertThat(properties.getRefreshThreshold()).isEqualTo(600000);
    }

    @Test
    void shouldSetAndGetIssuer() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("custom-issuer");

        assertThat(properties.getIssuer()).isEqualTo("custom-issuer");
    }

    @Test
    void shouldSetAndGetHeader() {
        JwtProperties properties = new JwtProperties();
        properties.setHeader("X-Auth-Token");

        assertThat(properties.getHeader()).isEqualTo("X-Auth-Token");
    }

    @Test
    void shouldSetAndGetPrefix() {
        JwtProperties properties = new JwtProperties();
        properties.setPrefix("Token ");

        assertThat(properties.getPrefix()).isEqualTo("Token ");
    }
}
