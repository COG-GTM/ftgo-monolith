package net.chrisrichardson.ftgo.orderservice.integration;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_MENU_ITEMS;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_NAME;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = FtgoMySqlContainer.Initializer.class)
public abstract class AbstractOrderFlowIntegrationTest {

  // Child tables first so foreign keys are never violated
  private static final String[] TABLES = {
          "courier_actions", "order_line_items", "orders", "courier",
          "restaurant_menu_items", "restaurants", "consumers", "api_request_log"
  };

  @Autowired
  protected JdbcTemplate jdbcTemplate;

  @Autowired
  protected ConsumerService consumerService;

  @Autowired
  protected RestaurantRepository restaurantRepository;

  @Autowired
  protected CourierRepository courierRepository;

  @Autowired
  private PlatformTransactionManager transactionManager;

  protected TransactionTemplate transactionTemplate;

  @Before
  public void cleanDatabase() {
    transactionTemplate = new TransactionTemplate(transactionManager);
    for (String table : TABLES) {
      jdbcTemplate.update("delete from " + table);
    }
  }

  protected long createConsumer() {
    Consumer consumer = consumerService.create(new PersonName("John", "Doe"));
    return consumer.getId();
  }

  protected long createRestaurant() {
    Address address = new Address("1 Main Street", null, "Oakland", "CA", "94611", 37.8044, -122.2712);
    Restaurant restaurant = new Restaurant(AJANTA_RESTAURANT_NAME, address, new RestaurantMenu(AJANTA_RESTAURANT_MENU_ITEMS));
    return restaurantRepository.save(restaurant).getId();
  }

  protected long createAvailableCourier() {
    Address address = new Address("2 Broadway", null, "Oakland", "CA", "94612", 37.8080, -122.2700);
    Courier courier = new Courier(new PersonName("Casey", "Courier"), address);
    courier.noteAvailable();
    return courierRepository.save(courier).getId();
  }

  protected String orderStateInDb(long orderId) {
    return jdbcTemplate.queryForObject("select order_state from orders where id = ?", String.class, orderId);
  }

  protected List<String> courierActionTypesInDb(long orderId) {
    return jdbcTemplate.queryForList(
            "select type from courier_actions where order_id = ? order by type desc", String.class, orderId);
  }

  protected int countRows(String table) {
    return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
  }
}
