package com.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonNameTest {

    @Test
    void shouldCreateWithFirstAndLastName() {
        PersonName name = new PersonName("John", "Doe");
        assertEquals("John", name.getFirstName());
        assertEquals("Doe", name.getLastName());
    }

    @Test
    void shouldAllowSettingFirstName() {
        PersonName name = new PersonName("John", "Doe");
        name.setFirstName("Jane");
        assertEquals("Jane", name.getFirstName());
    }

    @Test
    void shouldAllowSettingLastName() {
        PersonName name = new PersonName("John", "Doe");
        name.setLastName("Smith");
        assertEquals("Smith", name.getLastName());
    }
}
