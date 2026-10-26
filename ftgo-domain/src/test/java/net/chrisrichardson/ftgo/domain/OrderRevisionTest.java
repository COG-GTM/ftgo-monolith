package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRevisionTest {

  @Test
  public void shouldCreateOrderRevision() {
    Map<String, Integer> quantities = new HashMap<>();
    quantities.put("m1", 3);
    OrderRevision revision = new OrderRevision(Optional.empty(), quantities);

    assertThat(revision.getDeliveryInformation()).isEmpty();
    assertThat(revision.getRevisedLineItemQuantities()).containsEntry("m1", 3);
  }

  @Test
  public void shouldCreateWithDeliveryInformation() {
    DeliveryInformation di = new DeliveryInformation();
    Map<String, Integer> quantities = new HashMap<>();
    OrderRevision revision = new OrderRevision(Optional.of(di), quantities);

    assertThat(revision.getDeliveryInformation()).isPresent();
  }

  @Test
  public void shouldSetDeliveryInformation() {
    Map<String, Integer> quantities = new HashMap<>();
    OrderRevision revision = new OrderRevision(Optional.empty(), quantities);

    DeliveryInformation di = new DeliveryInformation();
    revision.setDeliveryInformation(Optional.of(di));
    assertThat(revision.getDeliveryInformation()).isPresent();
  }

  @Test
  public void shouldSetRevisedLineItemQuantities() {
    Map<String, Integer> quantities = new HashMap<>();
    OrderRevision revision = new OrderRevision(Optional.empty(), quantities);

    Map<String, Integer> newQuantities = new HashMap<>();
    newQuantities.put("m2", 5);
    revision.setRevisedLineItemQuantities(newQuantities);
    assertThat(revision.getRevisedLineItemQuantities()).containsEntry("m2", 5);
  }
}
