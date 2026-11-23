package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;
import java.util.List;

public interface CourierAvailabilityApi {

  /**
   * Assigns an available courier to deliver an order picked up at the given location.
   *
   * @throws NoCourierAvailableException if no courier is available
   */
  CourierAssignment assignCourierForDelivery(long orderId, Double pickupLatitude, Double pickupLongitude, LocalDateTime readyBy);

  List<DeliveryAction> findDeliveryActions(long courierId, long orderId);
}
