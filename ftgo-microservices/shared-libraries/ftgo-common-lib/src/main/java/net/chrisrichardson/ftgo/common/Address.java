package net.chrisrichardson.ftgo.common;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class Address {

    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;

    private Address() {
    }

    public Address(String street1, String street2, String city, String state, String zip) {
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    public String getStreet1() {
        return street1;
    }

    public String getStreet2() {
        return street2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street1, address.street1)
                && Objects.equals(street2, address.street2)
                && Objects.equals(city, address.city)
                && Objects.equals(state, address.state)
                && Objects.equals(zip, address.zip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street1, street2, city, state, zip);
    }

    @Override
    public String toString() {
        return "Address{street1='" + street1 + "', street2='" + street2
                + "', city='" + city + "', state='" + state + "', zip='" + zip + "'}";
    }
}
