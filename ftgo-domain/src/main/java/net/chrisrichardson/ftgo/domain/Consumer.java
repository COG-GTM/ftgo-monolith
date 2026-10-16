package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;

@Entity
@Table(name = "consumers")
@Access(AccessType.FIELD)
@DynamicUpdate
public class Consumer {

  // Hibernate 6 changed GenerationType.AUTO to allocate ids from a per-entity `consumers_seq`
  // sequence/table (increment 50). The Flyway schema only provisions the Hibernate 5 style shared
  // `hibernate_sequence` table, so bind the generator to it explicitly. On MySQL (no native sequences)
  // Hibernate's SequenceStyleGenerator falls back to that table's `next_val` column; allocationSize=1
  // keeps the increment identical to the legacy generator.
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @SequenceGenerator(name = "hibernate_sequence", sequenceName = "hibernate_sequence", allocationSize = 1)
  private Long id;

  @Embedded
  private PersonName name;

  private Consumer() {
  }

  public Consumer(PersonName name) {
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
