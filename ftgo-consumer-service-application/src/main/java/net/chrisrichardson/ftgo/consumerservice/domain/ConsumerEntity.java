package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "consumers")
@Access(AccessType.FIELD)
@DynamicUpdate
public class ConsumerEntity {

  @Id
  @GeneratedValue
  private Long id;

  @Embedded
  private PersonName name;

  private ConsumerEntity() {
  }

  public ConsumerEntity(PersonName name) {
    this.name = name;
  }

  public void validateOrderByConsumer(Money orderTotal) {
    // implement some business logic
  }

  public Long getId() {
    return id;
  }

  public PersonName getName() {
    return name;
  }
}
