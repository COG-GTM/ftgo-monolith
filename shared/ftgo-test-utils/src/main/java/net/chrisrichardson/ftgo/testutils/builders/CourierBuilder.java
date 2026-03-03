package net.chrisrichardson.ftgo.testutils.builders;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;

/**
 * Builder for creating {@link Courier} instances in tests.
 *
 * <p>Usage:
 * <pre>{@code
 * Courier courier = CourierBuilder.aCourier()
 *     .withFirstName("Mike")
 *     .withLastName("Driver")
 *     .withAddress(AddressBuilder.anAddress().withCity("San Francisco").build())
 *     .build();
 * }</pre>
 */
public class CourierBuilder {

    private String firstName = "Mike";
    private String lastName = "Driver";
    private PersonName name;
    private Address address = AddressBuilder.anAddress().build();

    private CourierBuilder() {
    }

    public static CourierBuilder aCourier() {
        return new CourierBuilder();
    }

    public CourierBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CourierBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public CourierBuilder withName(PersonName name) {
        this.name = name;
        return this;
    }

    public CourierBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }

    public Courier build() {
        PersonName personName = this.name != null ? this.name : new PersonName(firstName, lastName);
        return new Courier(personName, address);
    }
}
