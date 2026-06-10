package net.chrisrichardson.ftgo.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRevisionTest {

  @Test
  void shouldCreateOrderRevision() {
    Map<String, Integer> quantities = new HashMap<>();
    quantities.put("item1", 3);
    OrderRevision revision = new OrderRevision(Optional.empty(), quantities);

    assertThat(revision.getDeliveryInformation()).isEmpty();
    assertThat(revision.getRevisedLineItemQuantities()).containsEntry("item1", 3);
  }

  @Test
  void shouldCreateWithDeliveryInformation() {
    DeliveryInformation di = new DeliveryInformation();
    Map<String, Integer> quantities = new HashMap<>();
    OrderRevision revision = new OrderRevision(Optional.of(di), quantities);

    assertThat(revision.getDeliveryInformation()).isPresent();
  }

  @Test
  void shouldSupportSetters() {
    Map<String, Integer> quantities = new HashMap<>();
    OrderRevision revision = new OrderRevision(Optional.empty(), quantities);

    Map<String, Integer> newQuantities = new HashMap<>();
    newQuantities.put("item2", 5);
    revision.setRevisedLineItemQuantities(newQuantities);
    revision.setDeliveryInformation(Optional.of(new DeliveryInformation()));

    assertThat(revision.getRevisedLineItemQuantities()).containsEntry("item2", 5);
    assertThat(revision.getDeliveryInformation()).isPresent();
  }
}
