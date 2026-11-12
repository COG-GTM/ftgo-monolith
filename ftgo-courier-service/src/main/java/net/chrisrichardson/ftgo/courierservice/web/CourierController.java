package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierCredentialsRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierDetailsResponse;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CourierWorkloadResponse;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
  public ResponseEntity<CreateCourierResponse> create(@RequestBody CreateCourierRequest request) {
    Courier courier = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(courier.getId()), HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateCourierLocation(@PathVariable long courierId, @RequestBody CourierAvailability availability) {
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<CourierDetailsResponse> get(@PathVariable long courierId, Authentication authentication) {
    courierAccessPolicy.checkCanRead(authentication, courierId);
    Courier courier = courierService.findCourierById(courierId);
    List<CourierDetailsResponse.CourierAction> actions = courier.getPlan().getActions().stream()
            .map(action -> new CourierDetailsResponse.CourierAction(action.getType().name(), action.getTime()))
            .collect(Collectors.toList());
    CourierDetailsResponse response = new CourierDetailsResponse(courier.getId(), courier.getName(),
            courier.isAvailable(), new CourierDetailsResponse.CourierPlan(actions));
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/location", method= RequestMethod.POST)
  public ResponseEntity<String> updateLocation(@PathVariable long courierId, @RequestBody CourierLocationUpdate locationUpdate) {
    courierService.updateLocation(courierId, locationUpdate.getLatitude(), locationUpdate.getLongitude());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/workload", method= RequestMethod.GET)
  public ResponseEntity<CourierWorkloadResponse> getWorkload(@PathVariable long courierId, Authentication authentication) {
    courierAccessPolicy.checkCanRead(authentication, courierId);
    Courier courier = courierService.findCourierById(courierId);
    CourierWorkloadResponse response = new CourierWorkloadResponse(
            courier.getId(),
            courier.getActiveDeliveryCount(),
            courier.isAvailable(),
            courier.getCurrentLatitude(),
            courier.getCurrentLongitude(),
            courier.getLastLocationUpdate()
    );
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/credentials", method = RequestMethod.PUT)
  public ResponseEntity<Void> setCredentials(@PathVariable long courierId,
                                             @RequestBody CourierCredentialsRequest request,
                                             Authentication authentication) {
    courierAccessPolicy.checkIsOperator(authentication);
    courierService.setCredentials(courierId, request.getUsername(), request.getPassword());
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
