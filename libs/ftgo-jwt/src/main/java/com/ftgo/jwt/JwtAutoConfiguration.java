package com.ftgo.jwt;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnClass({EnableWebSecurity.class, SecurityFilterChain.class})
@ConditionalOnProperty(prefix = "ftgo.jwt", name = "secret")
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        return new JwtTokenProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenRefreshService jwtTokenRefreshService(JwtTokenProvider tokenProvider, JwtProperties properties) {
        return new JwtTokenRefreshService(tokenProvider, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider, JwtProperties properties) {
        return new JwtAuthenticationFilter(tokenProvider, properties);
    }
}
