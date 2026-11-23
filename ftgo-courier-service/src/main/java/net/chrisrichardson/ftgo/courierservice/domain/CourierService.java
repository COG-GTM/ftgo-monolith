package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignment;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailabilityApi;
import net.chrisrichardson.ftgo.courierservice.api.DeliveryAction;
import net.chrisrichardson.ftgo.courierservice.api.DeliveryActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CourierService implements CourierAvailabilityApi {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;

  public CourierService(CourierRepository courierRepository, CourierAssignmentStrategy courierAssignmentStrategy) {
    this.courierRepository = courierRepository;
    this.courierAssignmentStrategy = courierAssignmentStrategy;
  }

  @Transactional(CourierPersistenceConfiguration.TRANSACTION_MANAGER)
  public void updateAvailability(long courierId, boolean available) {
    if (available)
      noteAvailable(courierId);
    else
      noteUnavailable(courierId);
  }

  @Transactional(CourierPersistenceConfiguration.TRANSACTION_MANAGER)
  public Courier createCourier(PersonName name, Address address) {
    Courier courier = new Courier(name, address);
    courierRepository.save(courier);
    return courier;
  }

  void noteAvailable(long courierId) {
    courierRepository.findById(courierId).get().noteAvailable();
  }

  void noteUnavailable(long courierId) {
    courierRepository.findById(courierId).get().noteUnavailable();
  }

  public Courier findCourierById(long courierId) {
    return courierRepository.findById(courierId).get();
  }

  @Transactional(CourierPersistenceConfiguration.TRANSACTION_MANAGER)
  public void updateLocation(long courierId, double latitude, double longitude) {
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }

  @Override
  @Transactional(CourierPersistenceConfiguration.TRANSACTION_MANAGER)
  public CourierAssignment assignCourierForDelivery(long orderId, Double pickupLatitude, Double pickupLongitude, LocalDateTime readyBy) {
    List<Courier> couriers = courierRepository.findAllAvailable();
    Courier courier = courierAssignmentStrategy.assignCourier(couriers, pickupLatitude, pickupLongitude);

    courier.addAction(Action.makePickup(orderId));
    LocalDateTime estimatedDeliveryTime = estimateDeliveryTime(courier, pickupLatitude, pickupLongitude, readyBy);
    courier.addAction(Action.makeDropoff(orderId, estimatedDeliveryTime));

    logger.info("Order {} assigned to courier {} (active deliveries: {}, ETA: {})",
            orderId, courier.getId(), courier.getActiveDeliveryCount(), estimatedDeliveryTime);
    return new CourierAssignment(courier.getId(), estimatedDeliveryTime);
  }

  @Override
  @Transactional(transactionManager = CourierPersistenceConfiguration.TRANSACTION_MANAGER, readOnly = true)
  public List<DeliveryAction> findDeliveryActions(long courierId, long orderId) {
    return courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId))
            .actionsForDelivery(orderId)
            .stream()
            .map(a -> new DeliveryAction(DeliveryActionType.valueOf(a.getType().name()), a.getTime()))
            .collect(toList());
  }

  private LocalDateTime estimateDeliveryTime(Courier courier, Double pickupLatitude, Double pickupLongitude, LocalDateTime readyBy) {
    if (courier.hasLocation() && pickupLatitude != null && pickupLongitude != null) {
      double pickupDistance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
              courier.getCurrentLatitude(), courier.getCurrentLongitude(),
              pickupLatitude, pickupLongitude);
      long pickupMinutes = (long) DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(pickupDistance);
      LocalDateTime pickupArrival = LocalDateTime.now().plusMinutes(pickupMinutes);
      LocalDateTime effectiveReadyTime = pickupArrival.isAfter(readyBy) ? pickupArrival : readyBy;
      return effectiveReadyTime.plusMinutes(15);
    }
    return readyBy.plusMinutes(30);
  }
}
