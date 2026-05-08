package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;

import java.util.List;

public interface CourierAssignmentStrategy {

  Courier assignCourier(List<Courier> availableCouriers, Address restaurantAddress);

}
