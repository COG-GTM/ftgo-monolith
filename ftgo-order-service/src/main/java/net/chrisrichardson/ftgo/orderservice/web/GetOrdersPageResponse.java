package net.chrisrichardson.ftgo.orderservice.web;

import java.util.List;

public class GetOrdersPageResponse {

  private List<GetOrderResponse> content;
  private int page;
  private int size;
  private long totalElements;

  private GetOrdersPageResponse() {
  }

  public GetOrdersPageResponse(List<GetOrderResponse> content, int page, int size, long totalElements) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
  }

  public List<GetOrderResponse> getContent() {
    return content;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public long getTotalElements() {
    return totalElements;
  }
}
