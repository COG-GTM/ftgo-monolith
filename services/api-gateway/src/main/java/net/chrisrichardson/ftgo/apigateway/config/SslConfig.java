package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SSL/TLS termination configuration for the API Gateway.
 * Activated when {@code ftgo.gateway.ssl.enabled=true} in configuration.
 *
 * In production, configure the following properties:
 * <ul>
 *   <li>{@code ftgo.gateway.ssl.key-store} - Path to keystore file</li>
 *   <li>{@code ftgo.gateway.ssl.key-store-password} - Keystore password</li>
 *   <li>{@code ftgo.gateway.ssl.key-store-type} - Keystore type (default: PKCS12)</li>
 *   <li>{@code ftgo.gateway.ssl.key-alias} - Key alias in keystore</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(prefix = "ftgo.gateway.ssl", name = "enabled", havingValue = "true")
public class SslConfig {

    @Value("${ftgo.gateway.ssl.key-store:}")
    private String keyStore;

    @Value("${ftgo.gateway.ssl.key-store-password:}")
    private String keyStorePassword;

    @Value("${ftgo.gateway.ssl.key-store-type:PKCS12}")
    private String keyStoreType;

    @Value("${ftgo.gateway.ssl.key-alias:ftgo-gateway}")
    private String keyAlias;

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> sslCustomizer() {
        return factory -> {
            Ssl ssl = new Ssl();
            ssl.setEnabled(true);
            ssl.setKeyStore(keyStore);
            ssl.setKeyStorePassword(keyStorePassword);
            ssl.setKeyStoreType(keyStoreType);
            ssl.setKeyAlias(keyAlias);
            factory.setSsl(ssl);
        };
    }
}
