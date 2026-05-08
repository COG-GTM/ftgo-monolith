package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourierAssignmentController {

  private final CourierAssignmentService courierAssignmentService;

  public CourierAssignmentController(CourierAssignmentService courierAssignmentService) {
    this.courierAssignmentService = courierAssignmentService;
  }

  @PostMapping("/couriers/assign")
  public ResponseEntity<CourierAssignmentResponse> assignCourier(@RequestBody CourierAssignmentRequest request) {
    CourierAssignmentResponse response = courierAssignmentService.assignCourier(request);
    return ResponseEntity.ok(response);
  }
}
