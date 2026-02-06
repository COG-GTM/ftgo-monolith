package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
@DynamicUpdate
public class Courier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private PersonName name;

  @Embedded
  private Address address;

  @Embedded
  private Plan plan = new Plan();

  private Boolean available;

  public Courier() {
  }

  public Courier(PersonName name, Address address) {
    this.name = name;
    this.address = address;
  }

  public void noteAvailable() {
    this.available = true;
  }

  public void addAction(Action action) {
    if (plan == null) {
      plan = new Plan();
    }
    plan.add(action);
  }

  public void cancelDeliveryByOrderId(Long orderId) {
    plan.removeDeliveryByOrderId(orderId);
  }

  public boolean isAvailable() {
    return available;
  }

  public Plan getPlan() {
    return plan;
  }

  public Long getId() {
    return id;
  }

  public PersonName getName() {
    return name;
  }

  public Address getAddress() {
    return address;
  }

  public void noteUnavailable() {
    this.available = false;
  }

  public List<Action> actionsForOrderId(Long orderId) {
    return plan.actionsForOrderId(orderId);
  }
}
