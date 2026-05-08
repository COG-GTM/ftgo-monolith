package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class CourierService {

  private static final Logger logger = LoggerFactory.getLogger(CourierService.class);

  private final CourierRepository courierRepository;
  private final CourierAssignmentStrategy courierAssignmentStrategy;

  public CourierService(CourierRepository courierRepository,
                        CourierAssignmentStrategy courierAssignmentStrategy) {
    this.courierRepository = courierRepository;
    this.courierAssignmentStrategy = courierAssignmentStrategy;
  }

  @Transactional
  public void updateAvailability(long courierId, boolean available) {
    if (available)
      noteAvailable(courierId);
    else
      noteUnavailable(courierId);
  }

  @Transactional
  public Courier createCourier(PersonName name, Address address) {
    Courier courier = new Courier(name, address);
    courierRepository.save(courier);
    return courier;
  }

  void noteAvailable(long courierId) {
    courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId))
            .noteAvailable();
  }

  void noteUnavailable(long courierId) {
    courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId))
            .noteUnavailable();
  }

  public Courier findCourierById(long courierId) {
    return courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
  }

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }

  @Transactional
  public CourierAssignment assignCourier(long orderId, Address restaurantAddress, LocalDateTime readyBy) {
    Double targetLat = restaurantAddress == null ? null : restaurantAddress.getLatitude();
    Double targetLon = restaurantAddress == null ? null : restaurantAddress.getLongitude();

    List<Courier> available = courierRepository.findAllByAvailable(true);
    Courier courier = courierAssignmentStrategy.assignCourier(available, targetLat, targetLon);

    courier.addAction(Action.makePickup(orderId));

    LocalDateTime estimatedDeliveryTime = estimateDeliveryTime(courier, restaurantAddress, readyBy);
    courier.addAction(Action.makeDropoff(orderId, estimatedDeliveryTime));

    logger.info("Order {} assigned to courier {} (active deliveries: {}, ETA: {})",
            orderId, courier.getId(), courier.getActiveDeliveryCount(), estimatedDeliveryTime);

    return new CourierAssignment(courier.getId(), estimatedDeliveryTime);
  }

  private LocalDateTime estimateDeliveryTime(Courier courier, Address restaurantAddress, LocalDateTime readyBy) {
    if (courier.hasLocation() && restaurantAddress != null && restaurantAddress.getLatitude() != null) {
      double pickupDistance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
              courier.getCurrentLatitude(), courier.getCurrentLongitude(),
              restaurantAddress.getLatitude(),
              restaurantAddress.getLongitude());

      long pickupMinutes = (long) DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(pickupDistance);
      LocalDateTime pickupArrival = LocalDateTime.now().plusMinutes(pickupMinutes);
      LocalDateTime effectiveReadyTime = pickupArrival.isAfter(readyBy) ? pickupArrival : readyBy;

      return effectiveReadyTime.plusMinutes(15);
    }
    return readyBy.plusMinutes(30);
  }

  public static class CourierAssignment {
    public final Long courierId;
    public final LocalDateTime estimatedDeliveryTime;

    public CourierAssignment(Long courierId, LocalDateTime estimatedDeliveryTime) {
      this.courierId = courierId;
      this.estimatedDeliveryTime = estimatedDeliveryTime;
    }
  }
}
