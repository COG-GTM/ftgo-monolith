package net.chrisrichardson.ftgo.courierservice;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DomainConfiguration.class)
@Transactional
public class CourierServiceIntegrationTest {

  @Autowired
  private CourierRepository courierRepository;

  @Test
  public void shouldSaveAndRetrieveCourier() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"));
    courierRepository.save(courier);

    assertNotNull(courier.getId());

    Optional<Courier> found = courierRepository.findById(courier.getId());
    assertTrue(found.isPresent());
    assertEquals("John", found.get().getName().getFirstName());
    assertEquals("Doe", found.get().getName().getLastName());
  }

  @Test
  public void shouldUpdateCourierAvailability() {
    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("2 Main St", null, "San Francisco", "CA", "94101"));
    courierRepository.save(courier);

    courier.noteAvailable();
    courierRepository.save(courier);

    Optional<Courier> found = courierRepository.findById(courier.getId());
    assertTrue(found.isPresent());
    assertTrue(found.get().isAvailable());
  }

  @Test
  public void shouldFindAllAvailableCouriers() {
    Courier courier1 = new Courier(new PersonName("John", "Doe"),
            new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"));
    courier1.noteAvailable();
    courierRepository.save(courier1);

    Courier courier2 = new Courier(new PersonName("Jane", "Smith"),
            new Address("2 Main St", null, "San Francisco", "CA", "94101"));
    courierRepository.save(courier2);

    List<Courier> available = courierRepository.findAllAvailable();
    assertEquals(1, available.size());
    assertEquals("John", available.get(0).getName().getFirstName());
  }

  @Test
  public void shouldMarkCourierUnavailable() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"));
    courier.noteAvailable();
    courierRepository.save(courier);

    courier.noteUnavailable();
    courierRepository.save(courier);

    Optional<Courier> found = courierRepository.findById(courier.getId());
    assertTrue(found.isPresent());
    assertFalse(found.get().isAvailable());
  }
}
