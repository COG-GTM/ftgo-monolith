package com.ftgo.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.jwt")
public class JwtProperties {

    private String secret = "";
    private long expiration = 3600000;
    private long refreshThreshold = 300000;
    private String issuer = "ftgo-platform";
    private String header = "Authorization";
    private String prefix = "Bearer ";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public long getRefreshThreshold() {
        return refreshThreshold;
    }

    public void setRefreshThreshold(long refreshThreshold) {
        this.refreshThreshold = refreshThreshold;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
