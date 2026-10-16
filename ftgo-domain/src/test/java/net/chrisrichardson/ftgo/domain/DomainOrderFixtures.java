package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class DomainOrderFixtures {

  static final long CONSUMER_ID = 1511300065921L;
  static final String CHICKEN_VINDALOO_ID = "1";
  static final String SAMOSA_ID = "2";

  private DomainOrderFixtures() {
  }

  static Restaurant restaurant() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    return new Restaurant("Ajanta", address, new RestaurantMenu(Collections.emptyList()));
  }

  static OrderLineItem chickenVindaloo(int quantity) {
    return new OrderLineItem(CHICKEN_VINDALOO_ID, "Chicken Vindaloo", new Money("12.34"), quantity);
  }

  static OrderLineItem samosa(int quantity) {
    return new OrderLineItem(SAMOSA_ID, "Samosa", new Money("3.50"), quantity);
  }

  static List<OrderLineItem> lineItems(OrderLineItem... items) {
    return new ArrayList<>(Arrays.asList(items));
  }

  static Order order(OrderLineItem... items) {
    return new Order(CONSUMER_ID, restaurant(), lineItems(items));
  }

  static Order standardOrder() {
    return order(chickenVindaloo(5), samosa(2));
  }

  static Map<String, Integer> quantities(Object... idsAndQuantities) {
    Map<String, Integer> result = new HashMap<>();
    for (int i = 0; i < idsAndQuantities.length; i += 2) {
      result.put((String) idsAndQuantities[i], (Integer) idsAndQuantities[i + 1]);
    }
    return result;
  }

  static OrderRevision revision(Map<String, Integer> quantities) {
    return new OrderRevision(Optional.empty(), quantities);
  }

  static Money money(String amount) {
    return new Money(amount);
  }
}
