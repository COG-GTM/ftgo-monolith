package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonNameTest {

  @Test
  public void shouldCreatePersonName() {
    PersonName name = new PersonName("John", "Doe");
    assertThat(name.getFirstName()).isEqualTo("John");
    assertThat(name.getLastName()).isEqualTo("Doe");
  }

  @Test
  public void shouldSetFirstName() {
    PersonName name = new PersonName("John", "Doe");
    name.setFirstName("Jane");
    assertThat(name.getFirstName()).isEqualTo("Jane");
  }

  @Test
  public void shouldSetLastName() {
    PersonName name = new PersonName("John", "Doe");
    name.setLastName("Smith");
    assertThat(name.getLastName()).isEqualTo("Smith");
  }
}
