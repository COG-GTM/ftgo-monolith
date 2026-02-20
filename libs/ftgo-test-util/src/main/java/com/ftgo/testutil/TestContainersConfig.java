package com.ftgo.testutil;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class TestContainersConfig {

    public static final String MYSQL_IMAGE = "mysql:8.0";
    public static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.5.0";

    public static final String DEFAULT_DB_NAME = "ftgo_test";
    public static final String DEFAULT_USERNAME = "ftgo";
    public static final String DEFAULT_PASSWORD = "ftgo";

    private TestContainersConfig() {
    }

    public static MySQLContainer<?> mysql() {
        return mysql(DEFAULT_DB_NAME);
    }

    public static MySQLContainer<?> mysql(String databaseName) {
        return new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
                .withDatabaseName(databaseName)
                .withUsername(DEFAULT_USERNAME)
                .withPassword(DEFAULT_PASSWORD)
                .withReuse(true);
    }

    public static KafkaContainer kafka() {
        return new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
                .withReuse(true);
    }

    public static void configureMySQLProperties(
            MySQLContainer<?> container,
            java.util.function.BiConsumer<String, java.util.function.Supplier<Object>> registry) {
        registry.accept("spring.datasource.url", container::getJdbcUrl);
        registry.accept("spring.datasource.username", container::getUsername);
        registry.accept("spring.datasource.password", container::getPassword);
        registry.accept("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.accept("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    public static void configureKafkaProperties(
            KafkaContainer container,
            java.util.function.BiConsumer<String, java.util.function.Supplier<Object>> registry) {
        registry.accept("spring.kafka.bootstrap-servers", container::getBootstrapServers);
    }
}
