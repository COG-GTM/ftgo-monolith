package net.chrisrichardson.ftgo.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boots the domain module's JPA model on Hibernate 6 (Spring Boot 3.5, jakarta.persistence) against an
 * in-memory H2 database and checks that:
 * <ul>
 *   <li>every entity/embeddable mapping is accepted by Hibernate 6 (the context fails to start otherwise),</li>
 *   <li>the schema Hibernate derives from the annotations uses the same tables as the Flyway
 *       migrations in ftgo-flyway (in particular the legacy {@code hibernate_sequence} table for
 *       Consumer ids rather than Hibernate 6's default per-entity {@code consumers_seq}),</li>
 *   <li>the aggregates round-trip through the database, including element collections and the
 *       {@code @Version} optimistic-locking column on Order.</li>
 * </ul>
 * Hibernate creates the schema itself (ddl-auto=create-drop); Flyway is disabled because the real
 * migrations are MySQL-specific.
 */
@SpringBootTest(classes = DomainConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                // H2 in MySQL compatibility mode driven by Hibernate's MySQL dialect, so the DDL and id
                // generators behave as they will against the real database (no native sequences, so the
                // Consumer id generator must fall back to the hibernate_sequence table).
                "spring.datasource.url=jdbc:h2:mem:ftgo-domain;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
                // Hibernate would otherwise read H2's own version (2.x) through JDBC metadata and configure
                // the MySQL dialect for a pre-8.0 server; tell it to trust these declared values instead.
                "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
                "spring.jpa.properties.jakarta.persistence.database-product-name=MySQL",
                "spring.jpa.properties.jakarta.persistence.database-major-version=8",
                "spring.jpa.properties.jakarta.persistence.database-minor-version=0",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false"
        })
@Transactional
public class DomainEntityMappingTest {

  // Tables created by ftgo-flyway/src/main/resources/db/migration (V1 + V2); the Hibernate-derived
  // schema must not need anything else, otherwise the application breaks against the real MySQL schema.
  private static final Set<String> FLYWAY_TABLES = new TreeSet<>(Arrays.asList(
          "api_request_log", "consumers", "courier", "courier_actions", "hibernate_sequence",
          "order_line_items", "orders", "restaurant_menu_items", "restaurants"));

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private CourierRepository courierRepository;

  @Autowired
  private ConsumerRepository consumerRepository;

  @Test
  public void shouldGenerateSameTablesAsFlywayMigrations() {
    Set<String> hibernateTables = new TreeSet<>(jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class));
    assertEquals(FLYWAY_TABLES, hibernateTables);
  }

  @Test
  public void shouldMapColumnsToFlywaySchema() {
    // Columns that changed default naming/override behaviour between Hibernate 5 and 6, or that are
    // easy to get wrong with nested @Embedded/@AttributeOverride mappings.
    assertColumns("orders", "id", "version", "order_state", "consumer_id", "restaurant_id",
            "assigned_courier_id", "order_minimum", "payment_token", "delivery_time",
            "delivery_address_street1", "delivery_address_street2", "delivery_address_city",
            "delivery_address_state", "delivery_address_zip",
            "delivery_address_latitude", "delivery_address_longitude",
            "ready_by", "accept_time", "preparing_time", "ready_for_pickup_time", "picked_up_time", "delivered_time");
    assertColumns("order_line_items", "order_id", "menu_item_id", "name", "price", "quantity");
    assertColumns("restaurant_menu_items", "restaurant_id", "id", "name", "price");
    assertColumns("courier", "id", "available", "first_name", "last_name", "street1", "street2", "city",
            "state", "zip", "latitude", "longitude", "current_latitude", "current_longitude", "last_location_update");
    assertColumns("courier_actions", "courier_id", "order_id", "time", "type");
    assertColumns("consumers", "id", "first_name", "last_name");
  }

  @Test
  public void shouldPersistRestaurantWithMenuItems() {
    Restaurant restaurant = restaurantRepository.save(makeRestaurant());
    flushAndClear();

    Restaurant loaded = restaurantRepository.findById(restaurant.getId()).orElseThrow();
    assertEquals("Ajanta", loaded.getName());
    assertEquals("Oakland", loaded.getAddress().getCity());
    assertEquals(37.8044, loaded.getAddress().getLatitude());
    MenuItem menuItem = loaded.findMenuItem("1").orElseThrow();
    assertEquals(new MenuItem("1", "Chicken Vindaloo", new Money("12.34")), menuItem);
  }

  @Test
  public void shouldPersistOrderWithLineItemsAndVersion() {
    Restaurant restaurant = restaurantRepository.save(makeRestaurant());
    Order order = orderRepository.save(new Order(42L, restaurant,
            Collections.singletonList(new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2))));
    flushAndClear();

    Order loaded = orderRepository.findById(order.getId()).orElseThrow();
    assertEquals(OrderState.APPROVED, loaded.getOrderState());
    assertEquals(42L, loaded.getConsumerId());
    assertEquals(restaurant.getId(), loaded.getRestaurant().getId());
    assertEquals(new Money("24.68"), loaded.getOrderTotal());
    assertEquals(1, loaded.getLineItems().size());
    // @Version is initialised by Hibernate on insert and bumped on a state change.
    Long initialVersion = loaded.getVersion();
    assertNotNull(initialVersion);
    loaded.cancel();
    flushAndClear();
    assertEquals(initialVersion + 1, orderRepository.findById(order.getId()).orElseThrow().getVersion());
  }

  @Test
  public void shouldPersistCourierPlanAsElementCollection() {
    Restaurant restaurant = restaurantRepository.save(makeRestaurant());
    Order order = orderRepository.save(new Order(42L, restaurant, Collections.emptyList()));
    Courier courier = new Courier(new PersonName("Casey", "Courier"),
            new Address("2 Main St", null, "Oakland", "ca", "94612", 37.80, -122.27));
    courier.noteAvailable();
    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.of(2026, 1, 1, 12, 30)));
    courier = courierRepository.save(courier);
    order.schedule(courier);
    flushAndClear();

    Courier loaded = courierRepository.findById(courier.getId()).orElseThrow();
    assertTrue(loaded.isAvailable());
    assertEquals("Casey", loaded.getName().getFirstName());
    assertEquals(37.80, loaded.getCurrentLatitude());
    List<Action> actions = loaded.actionsForDelivery(order);
    assertEquals(2, actions.size());
    assertEquals(1, loaded.getActiveDeliveryCount());
    assertEquals(courier.getId(), orderRepository.findById(order.getId()).orElseThrow().getAssignedCourier().getId());
    // The JPQL queries in CourierRepository must still parse under Hibernate 6's HQL translator.
    assertEquals(1, courierRepository.findAllAvailable().size());
    assertEquals(1, courierRepository.findAllAvailableWithLocation().size());
  }

  @Test
  public void shouldAssignConsumerIdsFromHibernateSequenceTable() {
    Consumer consumer = consumerRepository.save(new Consumer(new PersonName("Chris", "Consumer")));
    flushAndClear();

    assertNotNull(consumer.getId());
    assertEquals("Chris", consumerRepository.findById(consumer.getId()).orElseThrow().getName().getFirstName());
    // Ids must come from the shared hibernate_sequence table provisioned by the Flyway V1 migration.
    assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hibernate_sequence", Integer.class));
  }

  private Restaurant makeRestaurant() {
    return new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "ca", "94612", 37.8044, -122.2712),
            new RestaurantMenu(Collections.singletonList(new MenuItem("1", "Chicken Vindaloo", new Money("12.34")))));
  }

  // Forces the pending inserts/updates to SQL and drops the first-level cache so reads hit the database.
  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private void assertColumns(String table, String... expected) {
    Set<String> actual = new TreeSet<>(jdbcTemplate.queryForList(
            "SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ?",
            String.class, table));
    assertEquals(new TreeSet<>(Arrays.asList(expected)), actual, "columns of " + table);
  }
}
