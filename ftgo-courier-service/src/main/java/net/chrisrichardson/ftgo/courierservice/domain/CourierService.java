package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.AvailableCourierDTO;
import net.chrisrichardson.ftgo.courierservice.api.CourierActionDTO;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CourierService {

  private CourierRepository courierRepository;

  public CourierService(CourierRepository courierRepository) {
    this.courierRepository = courierRepository;
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

  @Transactional(readOnly = true)
  public List<AvailableCourierDTO> findAllAvailable() {
    return courierRepository.findAllAvailable().stream()
        .map(courier -> new AvailableCourierDTO(
            courier.getId(),
            courier.getName(),
            courier.getAddress()))
        .collect(Collectors.toList());
  }

  @Transactional
  public void assignCourier(long courierId, Long orderId, LocalDateTime pickupTime, LocalDateTime dropoffTime) {
    Courier courier = courierRepository.findById(courierId)
        .orElseThrow(() -> new RuntimeException("Courier not found: " + courierId));
    courier.addAction(Action.makePickupForOrder(orderId, pickupTime));
    courier.addAction(Action.makeDropoffForOrder(orderId, dropoffTime));
  }

  @Transactional(readOnly = true)
  public List<CourierActionDTO> getActionsForOrder(long courierId, long orderId) {
    Courier courier = courierRepository.findById(courierId)
        .orElseThrow(() -> new RuntimeException("Courier not found: " + courierId));
    return courier.actionsForOrderId(orderId).stream()
        .map(action -> new CourierActionDTO(
            action.getType().name(),
            action.getOrderId(),
            action.getTime()))
        .collect(Collectors.toList());
  }

}
