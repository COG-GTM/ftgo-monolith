package net.chrisrichardson.ftgo.domain;


import net.chrisrichardson.ftgo.common.Address;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Embeddable
@Access(AccessType.FIELD)
public class DeliveryInformation {

  private LocalDateTime deliveryTime;

  // Address has its own column names when embedded directly (street1, latitude...); on the orders
  // table every Address column is prefixed with delivery_address_ (see the Flyway V1/V2 migrations).
  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name="street1", column=@Column(name="delivery_address_street1")),
          @AttributeOverride(name="street2", column=@Column(name="delivery_address_street2")),
          @AttributeOverride(name="city", column=@Column(name="delivery_address_city")),
          @AttributeOverride(name="state", column=@Column(name="delivery_address_state")),
          @AttributeOverride(name="zip", column=@Column(name="delivery_address_zip")),
          @AttributeOverride(name="latitude", column=@Column(name="delivery_address_latitude")),
          @AttributeOverride(name="longitude", column=@Column(name="delivery_address_longitude")),
  })
  private Address deliveryAddress;
}

