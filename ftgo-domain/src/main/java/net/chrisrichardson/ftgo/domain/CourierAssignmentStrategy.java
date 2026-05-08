package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;

import java.util.List;

public interface CourierAssignmentStrategy {

  Courier assignCourier(List<Courier> availableCouriers, Order order, Address restaurantAddress);

}
