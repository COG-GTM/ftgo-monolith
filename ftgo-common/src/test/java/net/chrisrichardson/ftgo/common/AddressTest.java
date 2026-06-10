package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

  @Test
  void shouldCreateAddressWithBasicFields() {
    Address address = new Address("123 Main St", "Apt 4", "Oakland", "CA", "94612");

    assertThat(address.getStreet1()).isEqualTo("123 Main St");
    assertThat(address.getStreet2()).isEqualTo("Apt 4");
    assertThat(address.getCity()).isEqualTo("Oakland");
    assertThat(address.getState()).isEqualTo("CA");
    assertThat(address.getZip()).isEqualTo("94612");
    assertThat(address.getLatitude()).isNull();
    assertThat(address.getLongitude()).isNull();
  }

  @Test
  void shouldCreateAddressWithCoordinates() {
    Address address = new Address("123 Main St", "Apt 4", "Oakland", "CA", "94612", 37.8044, -122.2712);

    assertThat(address.getLatitude()).isEqualTo(37.8044);
    assertThat(address.getLongitude()).isEqualTo(-122.2712);
  }

  @Test
  void shouldCreateDefaultAddress() {
    Address address = new Address();

    assertThat(address.getStreet1()).isNull();
    assertThat(address.getCity()).isNull();
    assertThat(address.getLatitude()).isNull();
  }

  @Test
  void shouldSupportSetters() {
    Address address = new Address();
    address.setStreet1("456 Oak Ave");
    address.setStreet2("Suite 100");
    address.setCity("San Francisco");
    address.setState("CA");
    address.setZip("94102");
    address.setLatitude(37.7749);
    address.setLongitude(-122.4194);

    assertThat(address.getStreet1()).isEqualTo("456 Oak Ave");
    assertThat(address.getStreet2()).isEqualTo("Suite 100");
    assertThat(address.getCity()).isEqualTo("San Francisco");
    assertThat(address.getState()).isEqualTo("CA");
    assertThat(address.getZip()).isEqualTo("94102");
    assertThat(address.getLatitude()).isEqualTo(37.7749);
    assertThat(address.getLongitude()).isEqualTo(-122.4194);
  }
}
