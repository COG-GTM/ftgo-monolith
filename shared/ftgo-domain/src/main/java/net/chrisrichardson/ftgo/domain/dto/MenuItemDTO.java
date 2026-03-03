package net.chrisrichardson.ftgo.domain.dto;

/**
 * Data Transfer Object for MenuItem used in cross-service communication.
 * Contains only serializable primitive/String fields for transport.
 */
public class MenuItemDTO {

    private String id;
    private String name;
    private String price;

    public MenuItemDTO() {
    }

    public MenuItemDTO(String id, String name, String price) {
        this.id = id;
        this.name = name;
        this.price = price;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
