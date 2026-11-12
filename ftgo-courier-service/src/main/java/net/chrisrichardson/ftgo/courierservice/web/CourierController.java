package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CourierWorkloadResponse;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CreatedCourier;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CourierController {

  private CourierService courierService;
  private final CourierRequestAuthenticator courierRequestAuthenticator;

  public CourierController(CourierService courierService, CourierRequestAuthenticator courierRequestAuthenticator) {
    this.courierService = courierService;
    this.courierRequestAuthenticator = courierRequestAuthenticator;
  }

  @RequestMapping(path="/couriers", method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(
          @RequestHeader(value = "Authorization", required = false) String authorization,
          @RequestBody CreateCourierRequest request) {
    CourierPrincipal principal = authenticate(authorization);
    courierRequestAuthenticator.requireOperator(principal);
    CreatedCourier createdCourier = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(
            createdCourier.getCourier().getId(), createdCourier.getAccessToken()), HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateAvailability(
          @RequestHeader(value = "Authorization", required = false) String authorization,
          @PathVariable long courierId, @RequestBody CourierAvailability availability) {
    CourierPrincipal principal = authenticate(authorization);
    courierRequestAuthenticator.requireCourierOrOperator(principal, courierId);
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<Courier> get(@PathVariable long courierId) {
    Courier courier = courierService.findCourierById(courierId);
    return new ResponseEntity<>(courier, HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/location", method= RequestMethod.POST)
  public ResponseEntity<String> updateLocation(
          @RequestHeader(value = "Authorization", required = false) String authorization,
          @PathVariable long courierId, @RequestBody CourierLocationUpdate locationUpdate) {
    CourierPrincipal principal = authenticate(authorization);
    courierRequestAuthenticator.requireCourierOrOperator(principal, courierId);
    courierService.updateLocation(courierId, locationUpdate.getLatitude(), locationUpdate.getLongitude());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/workload", method= RequestMethod.GET)
  public ResponseEntity<CourierWorkloadResponse> getWorkload(@PathVariable long courierId) {
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

  private CourierPrincipal authenticate(String authorization) {
    return courierRequestAuthenticator.authenticate(authorization);
  }

}
