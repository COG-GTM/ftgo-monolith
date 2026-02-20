package com.ftgo.e2e;

import com.ftgo.e2e.support.E2ETestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@DisplayName("Order Flow E2E Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderFlowE2ETest extends E2ETestBase {

    @Test
    @Order(1)
    @DisplayName("MySQL container is running and accessible")
    void mysqlContainerIsRunning() {
        assertThat(MYSQL.isRunning()).isTrue();
        assertThat(getMysqlJdbcUrl()).contains("ftgo");
    }

    @Test
    @Order(2)
    @DisplayName("Kafka container is running and accessible")
    void kafkaContainerIsRunning() {
        assertThat(KAFKA.isRunning()).isTrue();
        assertThat(getKafkaBootstrapServers()).isNotBlank();
    }

    @Test
    @Order(3)
    @DisplayName("Shared network is available for inter-service communication")
    void sharedNetworkIsAvailable() {
        assertThat(NETWORK).isNotNull();
        assertThat(MYSQL.getNetworkAliases()).contains("mysql");
        assertThat(KAFKA.getNetworkAliases()).contains("kafka");
    }
}
