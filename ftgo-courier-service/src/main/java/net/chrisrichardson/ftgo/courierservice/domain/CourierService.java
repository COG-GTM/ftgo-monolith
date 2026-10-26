package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    validateCoordinate("latitude", latitude, 90.0);
    validateCoordinate("longitude", longitude, 180.0);
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }

  private static void validateCoordinate(String name, double value, double limit) {
    if (!Double.isFinite(value) || Math.abs(value) > limit) {
      throw new IllegalArgumentException(name + " must be a finite value between -" + limit + " and " + limit);
    }
  }
}
