package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CourierWorkloadResponse;
import net.chrisrichardson.ftgo.courierservice.api.CourierAction;
import net.chrisrichardson.ftgo.courierservice.api.CourierPlan;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.api.GetCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.security.FtgoUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.stream.Collectors;

@RestController
public class CourierController {

  private CourierService courierService;
  private CourierAccessPolicy courierAccessPolicy;

  public CourierController(CourierService courierService, CourierAccessPolicy courierAccessPolicy) {
    this.courierService = courierService;
    this.courierAccessPolicy = courierAccessPolicy;
  }

  @RequestMapping(path="/couriers", method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                                       @RequestBody CreateCourierRequest request) {
    courierAccessPolicy.checkOperator(caller);
    Courier courier = courierService.createCourier(request.getName(), request.getAddress());
    return ResponseEntity.ok(new CreateCourierResponse(courier.getId()));
  }

  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateCourierLocation(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                                      @PathVariable long courierId, @RequestBody CourierAvailability availability) {
    courierAccessPolicy.checkSelfOrOperator(caller, courierId);
    courierService.updateAvailability(courierId, availability.isAvailable());
    return ResponseEntity.ok().build();
  }

  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<GetCourierResponse> get(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                                @PathVariable long courierId) {
    courierAccessPolicy.checkSelfOrOperator(caller, courierId);
    Courier courier = courierService.findCourierById(courierId);
    CourierPlan plan = new CourierPlan(courier.getPlan().getActions().stream()
            .map(action -> new CourierAction(action.getType().name(), action.getOrderId(), action.getTime()))
            .collect(Collectors.toList()));
    GetCourierResponse response = new GetCourierResponse(courier.getId(), courier.getName(), courier.getAddress(),
            courier.isAvailable(), courier.getCurrentLatitude(), courier.getCurrentLongitude(),
            courier.getLastLocationUpdate(), plan);
    return ResponseEntity.ok(response);
  }

  @RequestMapping(path="/couriers/{courierId}/location", method= RequestMethod.POST)
  public ResponseEntity<String> updateLocation(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                               @PathVariable long courierId, @RequestBody CourierLocationUpdate locationUpdate) {
    courierAccessPolicy.checkSelfOrOperator(caller, courierId);
    courierService.updateLocation(courierId, locationUpdate.getLatitude(), locationUpdate.getLongitude());
    return ResponseEntity.ok().build();
  }

  @RequestMapping(path="/couriers/{courierId}/workload", method= RequestMethod.GET)
  public ResponseEntity<CourierWorkloadResponse> getWorkload(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                                             @PathVariable long courierId) {
    courierAccessPolicy.checkSelfOrOperator(caller, courierId);
    Courier courier = courierService.findCourierById(courierId);
    CourierWorkloadResponse response = new CourierWorkloadResponse(
            courier.getId(),
            courier.getActiveDeliveryCount(),
            courier.isAvailable(),
            courier.getCurrentLatitude(),
            courier.getCurrentLongitude(),
            courier.getLastLocationUpdate()
    );
    return ResponseEntity.ok(response);
  }

}
