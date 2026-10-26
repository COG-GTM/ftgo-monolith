package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CourierServiceTest {

  private static final long UNKNOWN_COURIER_ID = 999L;
  private static final long COURIER_ID = 1L;

  private final Map<Long, Courier> couriers = new HashMap<>();
  private CourierService courierService;

  @Before
  public void setUp() {
    CourierRepository courierRepository = (CourierRepository) Proxy.newProxyInstance(
            CourierRepository.class.getClassLoader(),
            new Class<?>[]{CourierRepository.class},
            (proxy, method, args) -> {
              if (method.getName().equals("findById")) {
                return Optional.ofNullable(couriers.get(args[0]));
              }
              throw new UnsupportedOperationException(method.getName());
            });
    courierService = new CourierService(courierRepository);
  }

  @Test(expected = CourierNotFoundException.class)
  public void findCourierByIdThrowsCourierNotFoundForUnknownId() {
    courierService.findCourierById(UNKNOWN_COURIER_ID);
  }

  @Test(expected = CourierNotFoundException.class)
  public void markAvailableThrowsCourierNotFoundForUnknownId() {
    courierService.updateAvailability(UNKNOWN_COURIER_ID, true);
  }

  @Test(expected = CourierNotFoundException.class)
  public void markUnavailableThrowsCourierNotFoundForUnknownId() {
    courierService.updateAvailability(UNKNOWN_COURIER_ID, false);
  }

  @Test
  public void updateAvailabilityUpdatesExistingCourier() {
    Courier courier = new Courier();
    couriers.put(COURIER_ID, courier);

    courierService.updateAvailability(COURIER_ID, true);
    assertTrue(courier.isAvailable());

    courierService.updateAvailability(COURIER_ID, false);
    assertFalse(courier.isAvailable());

    assertSame(courier, courierService.findCourierById(COURIER_ID));
  }
}
