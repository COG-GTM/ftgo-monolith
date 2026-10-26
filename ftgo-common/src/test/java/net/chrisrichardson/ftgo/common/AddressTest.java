package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressTest {

  @Test
  public void shouldCreateAddressWithFiveArgs() {
    Address address = new Address("123 Main", "Apt 4", "Springfield", "IL", "62701");
    assertThat(address.getStreet1()).isEqualTo("123 Main");
    assertThat(address.getStreet2()).isEqualTo("Apt 4");
    assertThat(address.getCity()).isEqualTo("Springfield");
    assertThat(address.getState()).isEqualTo("IL");
    assertThat(address.getZip()).isEqualTo("62701");
    assertThat(address.getLatitude()).isNull();
    assertThat(address.getLongitude()).isNull();
  }

  @Test
  public void shouldCreateAddressWithCoordinates() {
    Address address = new Address("123 Main", "Apt 4", "Springfield", "IL", "62701", 39.7817, -89.6501);
    assertThat(address.getLatitude()).isEqualTo(39.7817);
    assertThat(address.getLongitude()).isEqualTo(-89.6501);
  }

  @Test
  public void shouldCreateDefaultAddress() {
    Address address = new Address();
    assertThat(address.getStreet1()).isNull();
    assertThat(address.getCity()).isNull();
  }

  @Test
  public void shouldSetAndGetAllFields() {
    Address address = new Address();
    address.setStreet1("456 Oak");
    address.setStreet2("Suite 100");
    address.setCity("Chicago");
    address.setState("IL");
    address.setZip("60601");
    address.setLatitude(41.8781);
    address.setLongitude(-87.6298);

    assertThat(address.getStreet1()).isEqualTo("456 Oak");
    assertThat(address.getStreet2()).isEqualTo("Suite 100");
    assertThat(address.getCity()).isEqualTo("Chicago");
    assertThat(address.getState()).isEqualTo("IL");
    assertThat(address.getZip()).isEqualTo("60601");
    assertThat(address.getLatitude()).isEqualTo(41.8781);
    assertThat(address.getLongitude()).isEqualTo(-87.6298);
  }
}
