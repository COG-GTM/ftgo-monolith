package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CourierWorkloadResponse;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierRegistration;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CourierController {

  private CourierService courierService;
  private CourierAuthorizer courierAuthorizer;

  public CourierController(CourierService courierService, CourierAuthorizer courierAuthorizer) {
    this.courierService = courierService;
    this.courierAuthorizer = courierAuthorizer;
  }

  @RequestMapping(path="/couriers", method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(@RequestBody CreateCourierRequest request) {
    CourierRegistration registration = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(registration.getCourier().getId(), registration.getAccessToken()), HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateAvailability(@PathVariable long courierId, @RequestBody CourierAvailability availability, HttpServletRequest request) {
    courierAuthorizer.requireAccessTo(request, courierId);
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<Courier> get(@PathVariable long courierId, HttpServletRequest request) {
    courierAuthorizer.requireAccessTo(request, courierId);
    Courier courier = courierService.findCourierById(courierId);
    return new ResponseEntity<>(courier, HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/location", method= RequestMethod.POST)
  public ResponseEntity<String> updateLocation(@PathVariable long courierId, @RequestBody CourierLocationUpdate locationUpdate, HttpServletRequest request) {
    courierAuthorizer.requireAccessTo(request, courierId);
    courierService.updateLocation(courierId, locationUpdate.getLatitude(), locationUpdate.getLongitude());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/workload", method= RequestMethod.GET)
  public ResponseEntity<CourierWorkloadResponse> getWorkload(@PathVariable long courierId, HttpServletRequest request) {
    courierAuthorizer.requireAccessTo(request, courierId);
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

}
