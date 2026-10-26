package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryInformationTest {

  @Test
  public void shouldCreateDefaultDeliveryInformation() {
    DeliveryInformation di = new DeliveryInformation();
    assertThat(di).isNotNull();
  }
}
