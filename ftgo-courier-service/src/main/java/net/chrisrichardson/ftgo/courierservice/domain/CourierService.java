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
    findCourierById(courierId).noteAvailable();
  }

  void noteUnavailable(long courierId) {
    findCourierById(courierId).noteUnavailable();
  }

  public Courier findCourierById(long courierId) {
    return courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
  }

  @Transactional
  public void updateLocation(long courierId, Double latitude, Double longitude) {
    if (latitude == null || longitude == null || !Double.isFinite(latitude) || !Double.isFinite(longitude)
            || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90 and longitude between -180 and 180");
    }
    Courier courier = findCourierById(courierId);
    courier.updateLocation(latitude, longitude);
  }
}
