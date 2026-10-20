package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class OrderRevisionTest {

  @Test
  public void noArgConstructorUsedByFrameworksDefaultsToNoDeliveryChangeAndNoQuantities() throws Exception {
    Constructor<OrderRevision> constructor = OrderRevision.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    OrderRevision revision = constructor.newInstance();

    assertEquals(Optional.empty(), revision.getDeliveryInformation());
    assertNull(revision.getRevisedLineItemQuantities());
  }

  @Test
  public void constructorKeepsDeliveryInformationAndQuantities() {
    DeliveryInformation delivery = deliveryInformation();
    Map<String, Integer> quantities = Collections.singletonMap("1", 3);

    OrderRevision revision = new OrderRevision(Optional.of(delivery), quantities);

    assertSame(delivery, revision.getDeliveryInformation().get());
    assertEquals(Integer.valueOf(3), revision.getRevisedLineItemQuantities().get("1"));
  }

  @Test
  public void settersReplaceDeliveryInformationAndQuantities() {
    OrderRevision revision = new OrderRevision(Optional.empty(), Collections.singletonMap("1", 1));
    DeliveryInformation delivery = deliveryInformation();
    Map<String, Integer> newQuantities = Collections.singletonMap("2", 7);

    revision.setDeliveryInformation(Optional.of(delivery));
    revision.setRevisedLineItemQuantities(newQuantities);

    assertSame(delivery, revision.getDeliveryInformation().get());
    assertSame(newQuantities, revision.getRevisedLineItemQuantities());
    assertNull(revision.getRevisedLineItemQuantities().get("1"));
    assertEquals(Integer.valueOf(7), revision.getRevisedLineItemQuantities().get("2"));
  }

  private static DeliveryInformation deliveryInformation() {
    return new DeliveryInformation();
  }
}
