package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonNameTest {

  @Test
  void shouldCreatePersonName() {
    PersonName name = new PersonName("John", "Doe");

    assertThat(name.getFirstName()).isEqualTo("John");
    assertThat(name.getLastName()).isEqualTo("Doe");
  }

  @Test
  void shouldSupportSetters() {
    PersonName name = new PersonName("John", "Doe");
    name.setFirstName("Jane");
    name.setLastName("Smith");

    assertThat(name.getFirstName()).isEqualTo("Jane");
    assertThat(name.getLastName()).isEqualTo("Smith");
  }
}
