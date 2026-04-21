package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CourierWorkloadResponse;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.api.ScheduleDeliveryRequest;
import net.chrisrichardson.ftgo.courierservice.api.ScheduleDeliveryResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CourierController {

  private CourierService courierService;

  public CourierController(CourierService courierService) {
    this.courierService = courierService;
  }

  @RequestMapping(path="/couriers", method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(@RequestBody CreateCourierRequest request) {
    Courier courier = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(courier.getId()), HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.PUT)
  public ResponseEntity<String> updateCourierAvailability(@PathVariable long courierId, @RequestBody CourierAvailability availability) {
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<Courier> get(@PathVariable long courierId) {
    Courier courier = courierService.findCourierById(courierId);
    return new ResponseEntity<>(courier, HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/schedule-delivery", method= RequestMethod.POST)
  public ResponseEntity<ScheduleDeliveryResponse> scheduleDelivery(@RequestBody ScheduleDeliveryRequest request) {
    long courierId = courierService.assignDelivery(request.getOrderId(), request.getReadyBy());
    return new ResponseEntity<>(new ScheduleDeliveryResponse(courierId), HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/location", method= RequestMethod.POST)
  public ResponseEntity<String> updateLocation(@PathVariable long courierId, @RequestBody CourierLocationUpdate locationUpdate) {
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

}
