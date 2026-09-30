package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class CourierService {

  private CourierRepository courierRepository;
  private CourierAccessTokens accessTokens;

  public CourierService(CourierRepository courierRepository, CourierAccessTokens accessTokens) {
    this.courierRepository = courierRepository;
    this.accessTokens = accessTokens;
  }

  @Transactional
  public void updateAvailability(long courierId, boolean available) {
    if (available)
      noteAvailable(courierId);
    else
      noteUnavailable(courierId);
  }

  @Transactional
  public CourierRegistration createCourier(PersonName name, Address address) {
    String accessToken = accessTokens.generate();
    Courier courier = new Courier(name, address, CourierAccessTokens.hash(accessToken));
    courierRepository.save(courier);
    return new CourierRegistration(courier, accessToken);
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

  public Optional<Courier> findCourierByAccessToken(String accessToken) {
    return courierRepository.findByAccessTokenHash(CourierAccessTokens.hash(accessToken));
  }

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    findCourierById(courierId).updateLocation(latitude, longitude);
  }
}
