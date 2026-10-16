package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.ActionType;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DistanceOptimizedCourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderRevision;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;
  private SimpleMeterRegistry meterRegistry;
  private OrderService orderService;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    courierAssignmentStrategy = mock(CourierAssignmentStrategy.class);
    meterRegistry = new SimpleMeterRegistry();
    orderService = new OrderService(orderRepository, restaurantRepository, Optional.of(meterRegistry),
            consumerService, courierRepository, courierAssignmentStrategy);
  }

  @Test
  public void createOrderBuildsAndSavesApprovedOrderAndIncrementsCounters() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(RestaurantMother.AJANTA_RESTAURANT));

    Order order = orderService.createOrder(CONSUMER_ID, AJANTA_ID,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    assertThat(order.getConsumerId()).isEqualTo(CONSUMER_ID);
    assertThat(order.getRestaurant()).isSameAs(RestaurantMother.AJANTA_RESTAURANT);
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
    assertThat(order.getLineItems()).hasSize(1);
    OrderLineItem lineItem = order.getLineItems().get(0);
    assertThat(lineItem.getMenuItemId()).isEqualTo("1");
    assertThat(lineItem.getName()).isEqualTo("Chicken Vindaloo");
    assertThat(lineItem.getPrice()).isEqualTo(new Money("12.34"));
    assertThat(lineItem.getQuantity()).isEqualTo(5);
    assertThat(order.getOrderTotal()).isEqualTo(new Money("61.70"));
    verify(consumerService).validateOrderForConsumer(CONSUMER_ID, new Money("61.70"));
    verify(orderRepository).save(same(order));
    assertThat(meterRegistry.counter("approved_orders").count()).isEqualTo(1.0);
    assertThat(meterRegistry.counter("placed_orders").count()).isEqualTo(1.0);
  }

  @Test
  public void createOrderWorksWithoutMeterRegistry() {
    OrderService serviceWithoutMetrics = new OrderService(orderRepository, restaurantRepository, Optional.empty(),
            consumerService, courierRepository, courierAssignmentStrategy);
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(RestaurantMother.AJANTA_RESTAURANT));

    Order order = serviceWithoutMetrics.createOrder(CONSUMER_ID, AJANTA_ID,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    verify(orderRepository).save(same(order));
    verify(consumerService).validateOrderForConsumer(CONSUMER_ID, new Money("61.70"));
  }

  @Test
  public void createOrderWithUnknownRestaurantThrowsWithoutValidatingOrSaving() {
    when(restaurantRepository.findById(42L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.createOrder(CONSUMER_ID, 42L,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES))
            .isInstanceOf(RestaurantNotFoundException.class)
            .hasMessage("Restaurant not found with id 42");

    verifyZeroInteractions(consumerService, orderRepository);
  }

  @Test
  public void createOrderWithUnknownMenuItemThrowsWithoutValidatingOrSaving() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(RestaurantMother.AJANTA_RESTAURANT));

    assertThatThrownBy(() -> orderService.createOrder(CONSUMER_ID, AJANTA_ID,
            Collections.singletonList(new MenuItemIdAndQuantity("999", 1))))
            .isInstanceOf(InvalidMenuItemIdException.class)
            .hasMessage("Invalid menu item id 999");

    verifyZeroInteractions(consumerService, orderRepository);
  }

  @Test
  public void createOrderPropagatesConsumerValidationFailureWithoutSaving() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(RestaurantMother.AJANTA_RESTAURANT));
    doThrow(new ConsumerNotFoundException()).when(consumerService)
            .validateOrderForConsumer(CONSUMER_ID, new Money("61.70"));

    assertThatThrownBy(() -> orderService.createOrder(CONSUMER_ID, AJANTA_ID,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES))
            .isInstanceOf(ConsumerNotFoundException.class);

    verify(orderRepository, never()).save(any(Order.class));
    assertThat(meterRegistry.counter("approved_orders").count()).isEqualTo(0.0);
    assertThat(meterRegistry.counter("placed_orders").count()).isEqualTo(0.0);
  }

  @Test
  public void cancelChangesApprovedOrderAndReturnsSameInstance() {
    Order order = order();
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

    Order cancelled = orderService.cancel(99L);

    assertThat(cancelled).isSameAs(order);
    assertThat(cancelled.getOrderState()).isEqualTo(OrderState.CANCELLED);
  }

  @Test
  public void cancelThrowsWhenOrderDoesNotExist() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.cancel(99L)).isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  public void cancelRejectsAnAlreadyAcceptedOrder() {
    Order order = order();
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancel(99L)).isInstanceOf(UnsupportedStateTransitionException.class);
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  public void reviseOrderUpdatesQuantityAndTotal() {
    Order order = order();
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
    Map<String, Integer> quantities = new HashMap<>();
    quantities.put("1", 3);

    Order revised = orderService.reviseOrder(99L, new OrderRevision(Optional.empty(), quantities));

    assertThat(revised).isSameAs(order);
    assertThat(revised.getLineItems().get(0).getQuantity()).isEqualTo(3);
    assertThat(revised.getOrderTotal()).isEqualTo(new Money("37.02"));
  }

  @Test
  public void reviseOrderThrowsWhenOrderDoesNotExist() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.reviseOrder(99L,
            new OrderRevision(Optional.empty(), Collections.singletonMap("1", 3))))
            .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  public void reviseOrderRejectsAnOrderThatIsNotApproved() {
    Order order = order();
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.reviseOrder(99L,
            new OrderRevision(Optional.empty(), Collections.singletonMap("1", 3))))
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  // Suspected bug: updating only one line item currently passes null for every unrevised line.
  @Test
  public void reviseOrderOfOnlyOneOfTwoLineItemsCurrentlyThrowsNullPointerException() {
    Order order = new Order(CONSUMER_ID, RestaurantMother.AJANTA_RESTAURANT, Arrays.asList(
            chickenVindalooLineItem(), new OrderLineItem("2", "Rice", new Money("2.00"), 1)));
    order.setId(99L);
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.reviseOrder(99L,
            new OrderRevision(Optional.empty(), Collections.singletonMap("1", 3))))
            .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void acceptAssignsCourierAndSchedulesPickupAndDropoffAtReadyByWhenCourierIsNearby() {
    Restaurant restaurant = restaurantAt(37.8044, -122.2712);
    Order order = order(restaurant);
    Courier courier = courierAt(37.8044, -122.2712);
    List<Courier> couriers = Collections.singletonList(courier);
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
    when(courierRepository.findAllAvailable()).thenReturn(couriers);
    when(courierAssignmentStrategy.assignCourier(couriers, order)).thenReturn(courier);

    orderService.accept(99L, readyBy);

    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
    assertThat(order.getAssignedCourier()).isSameAs(courier);
    assertThat(courier.getPlan().getActions()).extracting(Action::getType)
            .containsExactly(ActionType.PICKUP, ActionType.DROPOFF);
    assertThat(courier.getPlan().getActions().get(0).getTime()).isNull();
    assertThat(courier.getPlan().getActions().get(1).getTime()).isEqualTo(readyBy.plusMinutes(15));
    verify(courierAssignmentStrategy).assignCourier(couriers, order);
    assertThat(meterRegistry.counter("courier_assignments").count()).isEqualTo(1.0);
  }

  @Test
  public void acceptEstimatesDropoffAfterCourierTravelWhenPickupArrivesAfterReadyBy() {
    Restaurant restaurant = restaurantAt(37.8044, -122.2712);
    Order order = order(restaurant);
    Courier courier = courierAt(37.8044, -122.16);
    List<Courier> couriers = Collections.singletonList(courier);
    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(1);
    double distance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
            courier.getCurrentLatitude(), courier.getCurrentLongitude(), 37.8044, -122.2712);
    long pickupMinutes = (long) DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(distance);
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
    when(courierRepository.findAllAvailable()).thenReturn(couriers);
    when(courierAssignmentStrategy.assignCourier(couriers, order)).thenReturn(courier);
    LocalDateTime before = LocalDateTime.now();

    orderService.accept(99L, readyBy);

    LocalDateTime after = LocalDateTime.now();
    LocalDateTime eta = courier.getPlan().getActions().get(1).getTime();
    assertThat(eta).isBetween(before.plusMinutes(pickupMinutes + 15), after.plusMinutes(pickupMinutes + 15));
  }

  @Test
  public void acceptUsesThirtyMinuteEtaWhenCourierHasNoLocation() {
    Order order = order(restaurantAt(37.8044, -122.2712));
    Courier courier = new Courier();
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    assign(courier, order);

    orderService.accept(99L, readyBy);

    assertThat(courier.getPlan().getActions().get(1).getTime()).isEqualTo(readyBy.plusMinutes(30));
  }

  @Test
  public void acceptUsesThirtyMinuteEtaWhenRestaurantHasNullAddress() {
    Order order = order(RestaurantMother.AJANTA_RESTAURANT);
    Courier courier = courierAt(37.8044, -122.2712);
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    assign(courier, order);

    orderService.accept(99L, readyBy);

    assertThat(courier.getPlan().getActions().get(1).getTime()).isEqualTo(readyBy.plusMinutes(30));
  }

  @Test
  public void acceptUsesThirtyMinuteEtaWhenRestaurantHasNoCoordinates() {
    Order order = order(restaurantAt(null, null));
    Courier courier = courierAt(37.8044, -122.2712);
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    assign(courier, order);

    orderService.accept(99L, readyBy);

    assertThat(courier.getPlan().getActions().get(1).getTime()).isEqualTo(readyBy.plusMinutes(30));
  }

  // Suspected bug: ETA calculation checks latitude but unboxes a null restaurant longitude.
  @Test
  public void acceptCurrentlyThrowsNullPointerExceptionWhenRestaurantHasLatitudeButNoLongitude() {
    Order order = order(restaurantAt(37.8, null));
    Courier courier = courierAt(37.8, -122.2);
    assign(courier, order);

    assertThatThrownBy(() -> orderService.accept(99L, LocalDateTime.now().plusHours(1)))
            .isInstanceOf(NullPointerException.class);

    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
    assertThat(courier.getPlan().getActions()).extracting(Action::getType).containsExactly(ActionType.PICKUP);
    assertThat(order.getAssignedCourier()).isNull();
  }

  @Test
  public void acceptDoesNotLookForCouriersWhenOrderIsNotFound() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.accept(99L, LocalDateTime.now().plusHours(1)))
            .isInstanceOf(OrderNotFoundException.class);

    verifyZeroInteractions(courierRepository, courierAssignmentStrategy);
  }

  @Test
  public void acceptRejectsReadyByInThePastBeforeLookingForCouriers() {
    Order order = order();
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.accept(99L, LocalDateTime.now().minusMinutes(1)))
            .isInstanceOf(IllegalArgumentException.class);

    verifyZeroInteractions(courierRepository, courierAssignmentStrategy);
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
  }

  @Test
  public void acceptPropagatesNoCourierAvailableAndDoesNotAssignCourier() {
    Order order = order();
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
    when(courierRepository.findAllAvailable()).thenReturn(Collections.emptyList());
    when(courierAssignmentStrategy.assignCourier(Collections.emptyList(), order))
            .thenThrow(new NoCourierAvailableException());

    assertThatThrownBy(() -> orderService.accept(99L, LocalDateTime.now().plusHours(1)))
            .isInstanceOf(NoCourierAvailableException.class);

    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
    assertThat(order.getAssignedCourier()).isNull();
  }

  @Test
  public void orderCanMoveThroughAllPreparationAndDeliveryStates() {
    Order order = order();
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    when(courierRepository.findAllAvailable()).thenReturn(Collections.emptyList());
    Courier courier = new Courier();
    when(courierAssignmentStrategy.assignCourier(Collections.emptyList(), order)).thenReturn(courier);

    orderService.accept(99L, readyBy);
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
    orderService.notePreparing(99L);
    assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);
    orderService.noteReadyForPickup(99L);
    assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);
    orderService.notePickedUp(99L);
    assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);
    orderService.noteDelivered(99L);
    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }

  @Test
  public void eachPreparationAndDeliveryOperationThrowsWhenOrderIsNotFound() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.notePreparing(99L)).isInstanceOf(OrderNotFoundException.class);
    assertThatThrownBy(() -> orderService.noteReadyForPickup(99L)).isInstanceOf(OrderNotFoundException.class);
    assertThatThrownBy(() -> orderService.notePickedUp(99L)).isInstanceOf(OrderNotFoundException.class);
    assertThatThrownBy(() -> orderService.noteDelivered(99L)).isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  public void preparationAndDeliveryOperationsRejectWrongStates() {
    Order order = order();
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.notePreparing(99L)).isInstanceOf(UnsupportedStateTransitionException.class);
    assertThatThrownBy(() -> orderService.noteDelivered(99L)).isInstanceOf(UnsupportedStateTransitionException.class);
  }

  // Suspected bug: OrderNotFoundException currently concatenates the ID without a separator.
  @Test
  public void orderNotFoundExceptionMessageCurrentlyHasNoSeparatorBeforeId() {
    assertThat(new OrderNotFoundException(99L).getMessage()).isEqualTo("Order not found99");
  }

  private void assign(Courier courier, Order order) {
    List<Courier> couriers = Collections.singletonList(courier);
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
    when(courierRepository.findAllAvailable()).thenReturn(couriers);
    when(courierAssignmentStrategy.assignCourier(couriers, order)).thenReturn(courier);
  }

  private Order order() {
    return order(RestaurantMother.AJANTA_RESTAURANT);
  }

  private Order order(Restaurant restaurant) {
    Order order = new Order(CONSUMER_ID, restaurant, OrderDetailsMother.chickenVindalooLineItems());
    order.setId(99L);
    return order;
  }

  private OrderLineItem chickenVindalooLineItem() {
    return new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 5);
  }

  private Restaurant restaurantAt(Double latitude, Double longitude) {
    return new Restaurant("R", new Address("1 Main St", null, "Oakland", "CA", "94612", latitude, longitude),
            new RestaurantMenu(Collections.emptyList()));
  }

  private Courier courierAt(double latitude, double longitude) {
    return new Courier(new PersonName("A", "B"),
            new Address("2 Main St", null, "Oakland", "CA", "94612", latitude, longitude));
  }
}
