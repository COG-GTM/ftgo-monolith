package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class CourierAssignmentService {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final CourierRepository courierRepository;
  private final CourierAssignmentStrategy courierAssignmentStrategy;

  public CourierAssignmentService(CourierRepository courierRepository,
                                  CourierAssignmentStrategy courierAssignmentStrategy) {
    this.courierRepository = courierRepository;
    this.courierAssignmentStrategy = courierAssignmentStrategy;
  }

  @Transactional
  public CourierAssignmentResponse assignCourier(CourierAssignmentRequest request) {
    List<Courier> couriers = courierRepository.findAllAvailable();
    Courier courier = courierAssignmentStrategy.assignCourier(couriers, request.getRestaurantAddress());

    courier.addAction(Action.makePickup(request.getOrderId()));

    LocalDateTime estimatedDeliveryTime = estimateDeliveryTime(courier, request.getRestaurantAddress(), request.getReadyBy());
    courier.addAction(Action.makeDropoff(request.getOrderId(), estimatedDeliveryTime));

    logger.info("Order {} assigned to courier {} (active deliveries: {}, ETA: {})",
            request.getOrderId(), courier.getId(), courier.getActiveDeliveryCount(), estimatedDeliveryTime);

    return new CourierAssignmentResponse(courier.getId(), estimatedDeliveryTime);
  }

  private LocalDateTime estimateDeliveryTime(Courier courier, Address restaurantAddress, LocalDateTime readyBy) {
    if (courier.hasLocation()
            && restaurantAddress != null
            && restaurantAddress.getLatitude() != null) {

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
}
