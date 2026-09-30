package net.chrisrichardson.ftgo.courierservice.api;

public class CreateCourierResponse {
  private long id;
  private String accessToken;

  public CreateCourierResponse() {
  }

  public CreateCourierResponse(long id, String accessToken) {
    this.id = id;
    this.accessToken = accessToken;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
