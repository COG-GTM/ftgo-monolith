package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Entity
@Table(name = "restaurants")
@Access(AccessType.FIELD)
@DynamicUpdate
public class Restaurant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Embedded
  private Address address;

  @Embedded
  @ElementCollection
  @CollectionTable(name = "restaurant_menu_items")
  private List<MenuItem> menuItems;

  public Restaurant() {
  }

  public Restaurant(String name, Address address, RestaurantMenu menu) {
    this.name = name;
    this.address = address;
    this.menuItems = menu.getMenuItems();
  }

  public Restaurant(Long id, String name, RestaurantMenu menu) {
    this.id = id;
    this.name = name;
    this.menuItems = menu.getMenuItems();
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public Address getAddress() {
    return address;
  }

  public Stream<MenuItem> streamMenuItems() {
    return menuItems.stream();
  }

  public Optional<MenuItem> findMenuItem(String menuItemId) {
    return streamMenuItems()
            .filter(Objects::nonNull)
            .filter(item -> item.getId() != null && item.getId().equals(menuItemId))
            .findFirst();
  }

  public void reviseMenu(RestaurantMenu revisedMenu) {
    throw new UnsupportedOperationException();
  }
}
