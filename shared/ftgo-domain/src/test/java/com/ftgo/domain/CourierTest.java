package com.ftgo.domain;

import com.ftgo.common.Address;
import com.ftgo.common.PersonName;
import org.junit.Test;

import static org.junit.Assert.*;

public class CourierTest {

    @Test
    public void shouldCreateCourier() {
        PersonName name = new PersonName("John", "Doe");
        Address address = new Address("123 Main St", "", "Springfield", "IL", "62701");

        Courier courier = new Courier(name, address);
        assertNotNull(courier);
    }

    @Test
    public void shouldToggleAvailability() {
        Courier courier = new Courier(new PersonName("John", "Doe"),
                new Address("123 Main St", "", "Springfield", "IL", "62701"));

        courier.noteAvailable();
        assertTrue(courier.isAvailable());

        courier.noteUnavailable();
        assertFalse(courier.isAvailable());
    }
}
