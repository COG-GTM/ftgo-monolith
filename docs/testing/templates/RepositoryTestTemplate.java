package com.ftgo.ORDER_SERVICE_PACKAGE.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REPOSITORY LAYER UNIT TEST TEMPLATE
 *
 * Copy this template when creating a new repository-layer unit test.
 * Uses @DataJpaTest which configures an in-memory database, scans for
 * JPA entities, and configures Spring Data repositories.
 *
 * Conventions:
 *   - File location: src/test/java/<package>/repository/
 *   - Naming:        <Repository>Test.java
 *   - No @Tag needed (unit tests are the default suite)
 *
 * Replace:
 *   - ORDER_SERVICE_PACKAGE -> your service package (e.g., order)
 *   - OrderRepository       -> your repository interface
 *   - Order                 -> your JPA entity
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist order and generate ID")
        void shouldPersistAndGenerateId() {
            // Given
            Order order = new Order();
            order.setConsumerId(1L);
            order.setRestaurantId(2L);

            // When
            Order saved = orderRepository.save(order);
            entityManager.flush();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getConsumerId()).isEqualTo(1L);
            assertThat(saved.getRestaurantId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should find order by ID")
        void shouldFindById() {
            // Given
            Order order = new Order();
            order.setConsumerId(1L);
            order.setRestaurantId(2L);
            Order persisted = entityManager.persistAndFlush(order);

            // When
            Optional<Order> found = orderRepository.findById(persisted.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getConsumerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistent() {
            // When
            Optional<Order> found = orderRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("custom queries")
    class CustomQueries {

        @BeforeEach
        void setUp() {
            Order order1 = new Order();
            order1.setConsumerId(1L);
            order1.setRestaurantId(10L);
            entityManager.persist(order1);

            Order order2 = new Order();
            order2.setConsumerId(1L);
            order2.setRestaurantId(20L);
            entityManager.persist(order2);

            Order order3 = new Order();
            order3.setConsumerId(2L);
            order3.setRestaurantId(10L);
            entityManager.persist(order3);

            entityManager.flush();
        }

        @Test
        @DisplayName("should find all orders by consumer ID")
        void shouldFindByConsumerId() {
            // When
            List<Order> orders = orderRepository.findByConsumerId(1L);

            // Then
            assertThat(orders).hasSize(2);
            assertThat(orders).allMatch(o -> o.getConsumerId().equals(1L));
        }

        @Test
        @DisplayName("should find all orders by restaurant ID")
        void shouldFindByRestaurantId() {
            // When
            List<Order> orders = orderRepository.findByRestaurantId(10L);

            // Then
            assertThat(orders).hasSize(2);
            assertThat(orders).allMatch(o -> o.getRestaurantId().equals(10L));
        }
    }
}
