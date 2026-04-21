package net.chrisrichardson.ftgo.domain;

import java.util.List;

public interface CourierAssignmentStrategy {

  Courier assignCourier(List<Courier> availableCouriers, Order order);

}
