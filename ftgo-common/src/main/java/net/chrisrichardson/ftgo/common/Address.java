package net.chrisrichardson.ftgo.common;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;

@Embeddable
@Access(AccessType.FIELD)
public class Address {

  @Size(max = 255)
  private String street1;
  @Size(max = 255)
  private String street2;
  @Size(max = 255)
  private String city;
  @Size(max = 255)
  private String state;
  @Size(max = 255)
  private String zip;
  @DecimalMin("-90")
  @DecimalMax("90")
  private Double latitude;
  @DecimalMin("-180")
  @DecimalMax("180")
  private Double longitude;

  public Address() {
  }

  public Address(String street1, String street2, String city, String state, String zip) {
    this.street1 = street1;
    this.street2 = street2;
    this.city = city;
    this.state = state;
    this.zip = zip;
  }

  public Address(String street1, String street2, String city, String state, String zip, Double latitude, Double longitude) {
    this(street1, street2, city, state, zip);
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public String getStreet1() {
    return street1;
  }

  public void setStreet1(String street1) {
    this.street1 = street1;
  }

  public String getStreet2() {
    return street2;
  }

  public void setStreet2(String street2) {
    this.street2 = street2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }
}
