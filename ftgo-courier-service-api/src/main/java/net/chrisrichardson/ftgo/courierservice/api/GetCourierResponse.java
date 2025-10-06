package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.PersonName;

public class GetCourierResponse {
    private Long id;
    private PersonName name;
    private Boolean available;
    
    public GetCourierResponse() {}
    
    public GetCourierResponse(Long id, PersonName name, Boolean available) {
        this.id = id;
        this.name = name;
        this.available = available;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public PersonName getName() { return name; }
    public void setName(PersonName name) { this.name = name; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
