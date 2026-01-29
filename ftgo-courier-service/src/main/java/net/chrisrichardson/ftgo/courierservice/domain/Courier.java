package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
@DynamicUpdate
@Table(name = "courier")
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PersonName name;

    @Embedded
    private Address address;

    @ElementCollection
    @CollectionTable(name = "courier_actions", joinColumns = @JoinColumn(name = "courier_id"))
    private List<CourierAction> actions = new LinkedList<>();

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

    public void noteUnavailable() {
        this.available = false;
    }

    public void addAction(CourierAction action) {
        actions.add(action);
    }

    public boolean isAvailable() {
        return available != null && available;
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

    public List<CourierAction> getActions() {
        return actions;
    }

    public Boolean getAvailable() {
        return available;
    }
}
