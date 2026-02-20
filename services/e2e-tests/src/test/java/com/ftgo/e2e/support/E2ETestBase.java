package com.ftgo.e2e.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Tag("e2e")
@Testcontainers
public abstract class E2ETestBase {

    protected static final Network NETWORK = Network.newNetwork();

    @Container
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withNetwork(NETWORK)
            .withNetworkAliases("mysql")
            .withDatabaseName("ftgo")
            .withUsername("ftgo")
            .withPassword("ftgo")
            .withInitScript("init-databases.sql");

    @Container
    protected static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withNetwork(NETWORK)
            .withNetworkAliases("kafka");

    protected static RequestSpecification baseSpec;

    @BeforeAll
    static void setupRestAssured() {
        baseSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    protected static String getMysqlJdbcUrl() {
        return MYSQL.getJdbcUrl();
    }

    protected static String getKafkaBootstrapServers() {
        return KAFKA.getBootstrapServers();
    }
}
