package net.chrisrichardson.ftgo.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.PackageMatcher;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.ActionType;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DeliveryInformation;
import net.chrisrichardson.ftgo.domain.DistanceOptimizedCourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.LineItemQuantityChange;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.OrderLineItems;
import net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderRevision;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.PaymentInformation;
import net.chrisrichardson.ftgo.domain.Plan;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * The bounded-context map, as code.
 *
 * <p>Each context owns its service module packages plus the entities and repositories it owns
 * inside the shared {@code ftgo-domain} module. Its published language is the matching
 * {@code *-service-api} module; everything else is internal to the context.
 */
enum BoundedContext {

  CONSUMER("net.chrisrichardson.ftgo.consumerservice..",
          "net.chrisrichardson.ftgo.consumerservice.api..",
          Consumer.class, ConsumerRepository.class),

  RESTAURANT("net.chrisrichardson.ftgo.restaurantservice..",
          "net.chrisrichardson.ftgo.restaurantservice.events..",
          Restaurant.class, RestaurantRepository.class, RestaurantMenu.class, MenuItem.class),

  COURIER("net.chrisrichardson.ftgo.courierservice..",
          "net.chrisrichardson.ftgo.courierservice.api..",
          Courier.class, CourierRepository.class, Plan.class, Action.class, ActionType.class,
          CourierAssignmentStrategy.class, DistanceOptimizedCourierAssignmentStrategy.class,
          NoCourierAvailableException.class),

  ORDER("net.chrisrichardson.ftgo.orderservice..",
          "net.chrisrichardson.ftgo.orderservice.api..",
          Order.class, OrderLineItem.class, OrderLineItems.class, OrderRevision.class, OrderState.class,
          OrderRepository.class, DeliveryInformation.class, PaymentInformation.class,
          LineItemQuantityChange.class, OrderMinimumNotMetException.class);

  private final PackageMatcher packages;
  private final PackageMatcher publishedApi;
  private final Set<String> ownedSharedDomainClasses = new HashSet<>();

  BoundedContext(String packages, String publishedApi, Class<?>... ownedSharedDomainClasses) {
    this.packages = PackageMatcher.of(packages);
    this.publishedApi = PackageMatcher.of(publishedApi);
    Arrays.stream(ownedSharedDomainClasses).map(Class::getName).forEach(this.ownedSharedDomainClasses::add);
  }

  boolean contains(JavaClass javaClass) {
    JavaClass c = ContextMap.normalize(javaClass);
    return packages.matches(c.getPackageName()) || ownedSharedDomainClasses.contains(c.getName());
  }

  boolean publishes(JavaClass javaClass) {
    return publishedApi.matches(ContextMap.normalize(javaClass).getPackageName());
  }

  static Optional<BoundedContext> of(JavaClass javaClass) {
    return Arrays.stream(values()).filter(context -> context.contains(javaClass)).findFirst();
  }
}
