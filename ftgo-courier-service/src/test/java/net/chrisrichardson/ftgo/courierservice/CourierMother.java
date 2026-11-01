package net.chrisrichardson.ftgo.courierservice;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;

public class CourierMother {

  public static final String FIRST_NAME = "Jane";
  public static final String LAST_NAME = "Smith";
  public static final PersonName COURIER_NAME = new PersonName(FIRST_NAME, LAST_NAME);
  public static final Address COURIER_ADDRESS = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);

  public static Courier makeCourier() {
    return new Courier(COURIER_NAME, COURIER_ADDRESS);
  }

  public static Courier makeAvailableCourier() {
    Courier courier = makeCourier();
    courier.noteAvailable();
    return courier;
  }
}
