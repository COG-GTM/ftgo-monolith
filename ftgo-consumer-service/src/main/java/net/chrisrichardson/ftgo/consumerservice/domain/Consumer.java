package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "consumers")
@Access(AccessType.FIELD)
@DynamicUpdate
public class Consumer {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "hibernate_sequence")
  @TableGenerator(name = "hibernate_sequence", table = "hibernate_sequence", pkColumnName = "sequence_name", valueColumnName = "next_val", allocationSize = 1)
  private Long id;

  @Embedded
  private PersonName name;

  private Consumer() {
  }

  public Consumer(PersonName name) {
    this.name = name;
  }


  public void validateOrderByConsumer(Money orderTotal) {
  }

  public Long getId() {
    return id;
  }

  public PersonName getName() {
    return name;
  }
}
