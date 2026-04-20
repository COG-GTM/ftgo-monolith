package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courier")
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean available;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "last_name"))
    })
    private PersonName name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street1", column = @Column(name = "street1")),
            @AttributeOverride(name = "street2", column = @Column(name = "street2")),
            @AttributeOverride(name = "city", column = @Column(name = "city")),
            @AttributeOverride(name = "state", column = @Column(name = "state")),
            @AttributeOverride(name = "zip", column = @Column(name = "zip"))
    })
    private Address currentLocation;

    protected Courier() {
    }

    public Courier(PersonName name, Boolean available) {
        this.name = name;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public PersonName getName() {
        return name;
    }

    public Address getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Address currentLocation) {
        this.currentLocation = currentLocation;
    }
}
