package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class CourierService {

  private CourierRepository courierRepository;

  public CourierService(CourierRepository courierRepository) {
    this.courierRepository = courierRepository;
  }

  @Transactional
  public void updateAvailability(long courierId, boolean available) {
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    if (available) {
      courier.noteAvailable();
    } else {
      courier.noteUnavailable();
    }
  }

  @Transactional
  public CreatedCourier createCourier(PersonName name, Address address) {
    String accessToken = CourierAccessTokens.generate();
    Courier courier = new Courier(name, address);
    courier.assignAccessTokenHash(CourierAccessTokens.hash(accessToken));
    courierRepository.save(courier);
    return new CreatedCourier(courier, accessToken);
  }

  public Courier findCourierById(long courierId) {
    return courierRepository.findById(courierId).get();
  }

  public Optional<Courier> findCourierByAccessToken(String token) {
    return courierRepository.findByAccessTokenHash(CourierAccessTokens.hash(token));
  }

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }
}
