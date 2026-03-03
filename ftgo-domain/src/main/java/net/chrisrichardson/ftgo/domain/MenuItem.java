package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import java.util.Objects;

import javax.persistence.*;

@Embeddable
@Access(AccessType.FIELD)
public class MenuItem {

  private String id;
  private String name;


  @Embedded
  @AttributeOverride(name="amount", column = @Column(name="price"))
  private Money price;

  private MenuItem() {
  }

  public MenuItem(String id, String name, Money price) {
    this.id = id;
    this.name = name;
    this.price = price;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MenuItem menuItem = (MenuItem) o;
    return Objects.equals(id, menuItem.id) &&
            Objects.equals(name, menuItem.name) &&
            Objects.equals(price, menuItem.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, price);
  }

  @Override
  public String toString() {
    return "MenuItem{id='" + id + "', name='" + name + "', price=" + price + "}";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Money getPrice() {
    return price;
  }

  public void setPrice(Money price) {
    this.price = price;
  }
}
