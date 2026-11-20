package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.springframework.transaction.annotation.Transactional;
import net.chrisrichardson.ftgo.courierservice.web.CourierResponse;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CourierService {

  private CourierRepository courierRepository;

  @PersistenceContext
  private EntityManager entityManager;

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

  @Transactional(readOnly = true)
  public CourierResponse getCourier(long courierId) {
    Courier courier = findCourierById(courierId);
    return new CourierResponse(courier, entityManager.createQuery(
            "select new net.chrisrichardson.ftgo.courierservice.web.CourierResponse$ActionResponse(a.type, a.time, o.id) " +
            "from Courier c join c.plan.actions a left join a.order o where c.id = :courierId",
            CourierResponse.ActionResponse.class)
            .setParameter("courierId", courierId).getResultList());
  }

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90)
      throw new IllegalArgumentException("Latitude must be between -90 and 90");
    if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180)
      throw new IllegalArgumentException("Longitude must be between -180 and 180");
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }
}
