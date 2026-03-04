package ftgo.conventions

/**
 * Centralized version constants for FTGO microservice convention plugins.
 *
 * These versions mirror the definitions in gradle/libs.versions.toml.
 * When the project upgrades to Gradle 7.5+, this class can be replaced
 * by native version catalog references (libs.versions.xxx).
 *
 * @see <a href="../../../../../../gradle/libs.versions.toml">gradle/libs.versions.toml</a>
 */
class FtgoVersions {

    // --- Platform & Language ---
    static final String JAVA_VERSION = '17'

    // --- Spring Boot & Spring Framework ---
    static final String SPRING_BOOT = '3.2.5'
    static final String SPRING_DEPENDENCY_MANAGEMENT = '1.1.4'

    // --- Observability ---
    static final String MICROMETER = '1.12.5'
    static final String MICROMETER_TRACING = '1.2.5'

    // --- Data & Persistence ---
    static final String FLYWAY = '10.11.0'
    static final String HIBERNATE = '6.4.4.Final'
    static final String MYSQL_CONNECTOR = '8.3.0'

    // --- Serialization ---
    static final String JACKSON = '2.17.0'

    // --- Testing ---
    static final String JUNIT_JUPITER = '5.10.2'
    static final String REST_ASSURED = '5.4.0'
    static final String MOCKITO = '5.11.0'
    static final String ASSERTJ = '3.25.3'
    static final String TESTCONTAINERS = '1.19.7'

    // --- Utilities ---
    static final String COMMONS_LANG3 = '3.14.0'
    static final String LOMBOK = '1.18.32'
    static final String MAPSTRUCT = '1.5.5.Final'
    static final String SLF4J = '2.0.12'

    // --- Security ---
    static final String SPRING_SECURITY = '6.2.4'

    // --- Docker / Container ---
    static final String JIB = '3.4.1'

    // --- API Documentation ---
    static final String SPRINGDOC_OPENAPI = '2.4.0'

    // --- Group ---
    static final String FTGO_GROUP = 'net.chrisrichardson.ftgo'
}
