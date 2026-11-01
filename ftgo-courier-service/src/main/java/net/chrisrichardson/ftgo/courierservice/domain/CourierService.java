package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierActionDTO;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CourierService {

  private static final Logger logger = LoggerFactory.getLogger(CourierService.class);

  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;

  public CourierService(CourierRepository courierRepository, CourierAssignmentStrategy courierAssignmentStrategy) {
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
    courierRepository.findById(courierId).get().noteAvailable();
  }

  void noteUnavailable(long courierId) {
    courierRepository.findById(courierId).get().noteUnavailable();
  }

  public Courier findCourierById(long courierId) {
    return courierRepository.findById(courierId).get();
  }

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }

  @Transactional
  public CourierAssignmentResponse assignCourier(CourierAssignmentRequest request) {
    List<Courier> couriers = courierRepository.findAllAvailable();
    Courier courier = courierAssignmentStrategy.assignCourier(
            couriers, request.getRestaurantLatitude(), request.getRestaurantLongitude());

    courier.addAction(Action.makePickup(request.getOrderId()));

    LocalDateTime estimatedDeliveryTime = estimateDeliveryTime(
            courier, request.getReadyBy(),
            request.getRestaurantLatitude(), request.getRestaurantLongitude());
    courier.addAction(Action.makeDropoff(request.getOrderId(), estimatedDeliveryTime));

    logger.info("Order {} assigned to courier {} (active deliveries: {}, ETA: {})",
            request.getOrderId(), courier.getId(), courier.getActiveDeliveryCount(), estimatedDeliveryTime);

    List<CourierActionDTO> actionDTOs = courier.actionsForDelivery(request.getOrderId())
            .stream()
            .map(a -> new CourierActionDTO(a.getType().name(), a.getTime(), request.getOrderId()))
            .collect(Collectors.toList());

    return new CourierAssignmentResponse(courier.getId(), estimatedDeliveryTime, actionDTOs);
  }

  private LocalDateTime estimateDeliveryTime(Courier courier, LocalDateTime readyBy,
                                              Double restaurantLatitude, Double restaurantLongitude) {
    if (courier.hasLocation() && restaurantLatitude != null && restaurantLongitude != null) {
      double pickupDistance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
              courier.getCurrentLatitude(), courier.getCurrentLongitude(),
              restaurantLatitude, restaurantLongitude);

      long pickupMinutes = (long) DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(pickupDistance);
      LocalDateTime pickupArrival = LocalDateTime.now().plusMinutes(pickupMinutes);
      LocalDateTime effectiveReadyTime = readyBy == null ? pickupArrival
              : (pickupArrival.isAfter(readyBy) ? pickupArrival : readyBy);

      return effectiveReadyTime.plusMinutes(15);
    }

    if (readyBy == null) {
      return LocalDateTime.now().plusMinutes(30);
    }
    return readyBy.plusMinutes(30);
  }

  public List<CourierActionDTO> getActionsForOrder(long courierId, long orderId) {
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    return courier.actionsForDelivery(orderId)
            .stream()
            .map(a -> new CourierActionDTO(a.getType().name(), a.getTime(), orderId))
            .collect(Collectors.toList());
  }
}
