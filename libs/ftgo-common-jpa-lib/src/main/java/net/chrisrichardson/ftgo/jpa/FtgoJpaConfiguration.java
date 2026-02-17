package net.chrisrichardson.ftgo.jpa;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Shared JPA configuration for FTGO microservices.
 * Auto-scans for JPA entities in the ftgo.jpa package.
 */
@AutoConfiguration
@EntityScan(basePackages = "net.chrisrichardson.ftgo.jpa")
public class FtgoJpaConfiguration {
}
