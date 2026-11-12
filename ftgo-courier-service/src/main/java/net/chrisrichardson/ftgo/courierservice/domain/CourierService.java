package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class CourierService {

  private CourierRepository courierRepository;
  private CourierCredentialsRepository courierCredentialsRepository;
  private PasswordEncoder passwordEncoder;
  private String operatorUsername;
  private EntityManager entityManager;

  public CourierService(CourierRepository courierRepository,
                        CourierCredentialsRepository courierCredentialsRepository,
                        PasswordEncoder passwordEncoder,
                        String operatorUsername,
                        EntityManager entityManager) {
    this.courierRepository = courierRepository;
    this.courierCredentialsRepository = courierCredentialsRepository;
    this.passwordEncoder = passwordEncoder;
    this.operatorUsername = operatorUsername;
    this.entityManager = entityManager;
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
    courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId)).noteAvailable();
  }

  void noteUnavailable(long courierId) {
    courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId)).noteUnavailable();
  }

  public Courier findCourierById(long courierId) {
    return courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
  }

  @Transactional
  public void setCredentials(long courierId, String username, String rawPassword) {
    if (!courierRepository.findById(courierId).isPresent()) {
      throw new CourierNotFoundException(courierId);
    }
    if (!StringUtils.hasText(username) || username.length() > 100) {
      throw new IllegalArgumentException("Username must be non-blank and at most 100 characters");
    }
    if (username.equalsIgnoreCase(operatorUsername)) {
      throw new IllegalArgumentException("Username is reserved");
    }
    if (rawPassword == null || rawPassword.length() < 12) {
      throw new IllegalArgumentException("Password must be at least 12 characters");
    }

    CourierCredentials existingUsername = courierCredentialsRepository.findByUsername(username).orElse(null);
    if (existingUsername != null && existingUsername.getCourierId() != courierId) {
      throw new CourierUsernameTakenException(username);
    }

    CourierCredentials credentials = courierCredentialsRepository.findById(courierId)
            .orElse(new CourierCredentials(courierId));
    credentials.update(username, passwordEncoder.encode(rawPassword));
    try {
      courierCredentialsRepository.save(credentials);
      entityManager.flush();
    } catch (DataIntegrityViolationException | PersistenceException e) {
      throw new CourierUsernameTakenException(username);
    }
  }

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }
}
