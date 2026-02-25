package com.ftgo.security.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for the FTGO security library.
 * <p>
 * Import this configuration in any microservice to enable the full
 * FTGO security stack:
 * <pre>
 * &#64;Import(FtgoSecurityAutoConfiguration.class)
 * &#64;SpringBootApplication
 * public class MyServiceApplication { }
 * </pre>
 * Or rely on component scanning if the service scans {@code com.ftgo.security}.
 * </p>
 */
@Configuration
@ComponentScan(basePackages = "com.ftgo.security")
@Import({FtgoBaseSecurityConfiguration.class, FtgoCorsConfiguration.class})
public class FtgoSecurityAutoConfiguration {
}
