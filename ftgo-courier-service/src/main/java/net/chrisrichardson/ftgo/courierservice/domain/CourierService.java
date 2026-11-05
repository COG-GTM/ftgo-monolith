package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;

import java.util.Optional;

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
  public CreatedCourier createCourier(PersonName name, Address address) {
    String accessToken = CourierAccessTokens.generate();
    Courier courier = new Courier(name, address);
    courier.assignAccessTokenHash(CourierAccessTokens.hash(accessToken));
    courierRepository.save(courier);
    return new CreatedCourier(courier, accessToken);
  }

  public Optional<Long> findCourierIdByAccessToken(String accessToken) {
    return courierRepository.findByAccessTokenHash(CourierAccessTokens.hash(accessToken))
            .map(Courier::getId);
  }

  void noteAvailable(long courierId) {
    courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId))
            .noteAvailable();
  }

  void noteUnavailable(long courierId) {
    courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId))
            .noteUnavailable();
  }

  public Courier findCourierById(long courierId) {
    return courierRepository.findById(courierId).get();
  }

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }
}
