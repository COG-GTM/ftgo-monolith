package net.chrisrichardson.ftgo.courierservice.domain;

import java.util.List;

public interface CourierAssignmentStrategy {

  Courier assignCourier(List<Courier> availableCouriers, Double restaurantLatitude, Double restaurantLongitude);

}
