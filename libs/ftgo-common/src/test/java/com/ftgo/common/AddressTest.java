package com.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AddressTest {

    @Test
    void shouldCreateWithAllFields() {
        Address address = new Address("123 Main St", "Apt 4", "Springfield", "IL", "62704");
        assertEquals("123 Main St", address.getStreet1());
        assertEquals("Apt 4", address.getStreet2());
        assertEquals("Springfield", address.getCity());
        assertEquals("IL", address.getState());
        assertEquals("62704", address.getZip());
    }

    @Test
    void shouldCreateWithNoArgConstructor() {
        Address address = new Address();
        assertNotNull(address);
    }

    @Test
    void shouldAllowSettingStreet1() {
        Address address = new Address();
        address.setStreet1("456 Oak Ave");
        assertEquals("456 Oak Ave", address.getStreet1());
    }

    @Test
    void shouldAllowSettingStreet2() {
        Address address = new Address();
        address.setStreet2("Suite 100");
        assertEquals("Suite 100", address.getStreet2());
    }

    @Test
    void shouldAllowSettingCity() {
        Address address = new Address();
        address.setCity("Chicago");
        assertEquals("Chicago", address.getCity());
    }

    @Test
    void shouldAllowSettingState() {
        Address address = new Address();
        address.setState("IL");
        assertEquals("IL", address.getState());
    }

    @Test
    void shouldAllowSettingZip() {
        Address address = new Address();
        address.setZip("60601");
        assertEquals("60601", address.getZip());
    }
}
