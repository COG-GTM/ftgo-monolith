package ftgo.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin: ftgo.spring-boot-conventions
 *
 * Configures Spring Boot 3.x dependency management for FTGO microservices.
 * This plugin:
 * - Applies the java-conventions plugin as a base
 * - Adds Spring Boot BOM for dependency management
 * - Provides standard Spring Boot starter dependencies
 * - Configures Spring Boot Actuator with Micrometer metrics
 *
 * NOTE: The Spring Boot Gradle plugin (bootJar, bootRun tasks) requires
 * Gradle 7.5+. This plugin currently manages dependencies via explicit
 * versions. When Gradle is upgraded, it can apply the Spring Boot plugin
 * directly: apply plugin: 'org.springframework.boot'
 */
class FtgoSpringBootConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Apply java conventions as a base
        project.pluginManager.apply(FtgoJavaConventionsPlugin)

        // Add Spring Boot dependencies
        project.dependencies {
            // Spring Boot Starters
            implementation "org.springframework.boot:spring-boot-starter-web:${FtgoVersions.SPRING_BOOT}"
            implementation "org.springframework.boot:spring-boot-starter-data-jpa:${FtgoVersions.SPRING_BOOT}"
            implementation "org.springframework.boot:spring-boot-starter-actuator:${FtgoVersions.SPRING_BOOT}"
            implementation "org.springframework.boot:spring-boot-starter-validation:${FtgoVersions.SPRING_BOOT}"

            // Observability - Micrometer
            implementation "io.micrometer:micrometer-registry-prometheus:${FtgoVersions.MICROMETER}"

            // Database
            implementation "com.mysql:mysql-connector-j:${FtgoVersions.MYSQL_CONNECTOR}"
            implementation "org.flywaydb:flyway-core:${FtgoVersions.FLYWAY}"
            implementation "org.flywaydb:flyway-mysql:${FtgoVersions.FLYWAY}"

            // Jackson for JSON
            implementation "com.fasterxml.jackson.core:jackson-databind:${FtgoVersions.JACKSON}"
            implementation "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${FtgoVersions.JACKSON}"

            // API Documentation
            implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:${FtgoVersions.SPRINGDOC_OPENAPI}"
        }
    }
}
