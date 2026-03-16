/**
 * Centralized dependency coordinate definitions for FTGO microservices.
 *
 * Provides pre-built dependency strings using versions from FtgoVersions.
 * Convention plugins and service build files can reference these constants
 * instead of hard-coding group:artifact:version strings.
 */
class FtgoDependencies {

    // --- Spring Boot Starters ---
    static final String SPRING_BOOT_STARTER_WEB = "org.springframework.boot:spring-boot-starter-web:${FtgoVersions.SPRING_BOOT}"
    static final String SPRING_BOOT_STARTER_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa:${FtgoVersions.SPRING_BOOT}"
    static final String SPRING_BOOT_STARTER_ACTUATOR = "org.springframework.boot:spring-boot-starter-actuator:${FtgoVersions.SPRING_BOOT}"
    static final String SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test:${FtgoVersions.SPRING_BOOT}"

    // --- Observability ---
    static final String MICROMETER_PROMETHEUS = "io.micrometer:micrometer-registry-prometheus:${FtgoVersions.MICROMETER}"

    // --- Database ---
    static final String FLYWAY_CORE = "org.flywaydb:flyway-core:${FtgoVersions.FLYWAY}"
    static final String MYSQL_CONNECTOR = "mysql:mysql-connector-java:${FtgoVersions.MYSQL_CONNECTOR}"

    // --- Serialization ---
    static final String JACKSON_CORE = "com.fasterxml.jackson.core:jackson-core:${FtgoVersions.JACKSON}"
    static final String JACKSON_DATABIND = "com.fasterxml.jackson.core:jackson-databind:${FtgoVersions.JACKSON}"
    static final String JACKSON_JSR310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${FtgoVersions.JACKSON}"

    // --- API Documentation ---
    static final String SPRINGFOX_SWAGGER2 = "io.springfox:springfox-swagger2:${FtgoVersions.SPRINGFOX_SWAGGER}"
    static final String SPRINGFOX_SWAGGER_UI = "io.springfox:springfox-swagger-ui:${FtgoVersions.SPRINGFOX_SWAGGER}"

    // --- Testing ---
    static final String JUNIT_JUPITER_API = "org.junit.jupiter:junit-jupiter-api:${FtgoVersions.JUNIT5}"
    static final String JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${FtgoVersions.JUNIT5}"
    static final String JUNIT_JUPITER_PARAMS = "org.junit.jupiter:junit-jupiter-params:${FtgoVersions.JUNIT5}"
    static final String REST_ASSURED = "io.rest-assured:rest-assured:${FtgoVersions.REST_ASSURED}"
    static final String REST_ASSURED_SPRING_MOCK_MVC = "io.rest-assured:spring-mock-mvc:${FtgoVersions.REST_ASSURED}"
    static final String REST_ASSURED_JSON_PATH = "io.rest-assured:json-path:${FtgoVersions.REST_ASSURED}"

    // --- Utilities ---
    static final String COMMONS_LANG = "commons-lang:commons-lang:${FtgoVersions.COMMONS_LANG}"
    static final String JAVAX_EL_API = "javax.el:javax.el-api:${FtgoVersions.JAVAX_EL}"
}
