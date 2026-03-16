/**
 * Centralized version constants for FTGO microservices.
 *
 * These versions mirror gradle/libs.versions.toml and are used by
 * convention plugins since Gradle 4.10.2 does not support native
 * version catalogs. When upgrading to Gradle 7+, replace usage of
 * this class with the native version catalog.
 */
class FtgoVersions {

    // --- Core Platform ---
    static final String SPRING_BOOT = '2.0.3.RELEASE'
    static final String SPRING_DEPENDENCY_MANAGEMENT = '1.0.3.RELEASE'

    // --- Observability ---
    static final String MICROMETER = '1.0.4'

    // --- Database ---
    static final String FLYWAY = '6.0.0'
    static final String MYSQL_CONNECTOR = '5.1.39'

    // --- Serialization ---
    static final String JACKSON = '2.9.7'

    // --- API Documentation ---
    static final String SPRINGFOX_SWAGGER = '2.8.0'

    // --- Testing ---
    static final String JUNIT5 = '5.2.0'
    static final String REST_ASSURED = '3.0.6'

    // --- Utilities ---
    static final String COMMONS_LANG = '2.6'
    static final String JAVAX_EL = '2.2.5'

    // --- Build Plugins ---
    static final String DOCKER_COMPOSE_PLUGIN = '0.6.6'
}
