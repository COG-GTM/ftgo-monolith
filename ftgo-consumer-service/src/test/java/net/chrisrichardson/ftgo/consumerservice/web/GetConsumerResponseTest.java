package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GetConsumerResponseTest {

  @Test
  public void shouldCreateWithName() {
    PersonName name = new PersonName("Alice", "Jones");
    GetConsumerResponse response = new GetConsumerResponse(name);
    assertThat(response.getName().getFirstName()).isEqualTo("Alice");
    assertThat(response.getName().getLastName()).isEqualTo("Jones");
  }
}
