package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street1", column = @Column(name = "street1")),
            @AttributeOverride(name = "street2", column = @Column(name = "street2")),
            @AttributeOverride(name = "city", column = @Column(name = "city")),
            @AttributeOverride(name = "state", column = @Column(name = "state")),
            @AttributeOverride(name = "zip", column = @Column(name = "zip"))
    })
    private Address address;

    protected Restaurant() {
    }

    public Restaurant(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
