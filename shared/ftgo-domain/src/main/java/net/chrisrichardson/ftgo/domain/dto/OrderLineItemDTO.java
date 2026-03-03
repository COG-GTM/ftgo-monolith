package net.chrisrichardson.ftgo.domain.dto;

/**
 * Data Transfer Object for OrderLineItem used in cross-service communication.
 * Contains only serializable primitive/String fields for transport.
 */
public class OrderLineItemDTO {

    private String menuItemId;
    private String name;
    private String price;
    private int quantity;

    public OrderLineItemDTO() {
    }

    public OrderLineItemDTO(String menuItemId, String name, String price, int quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
