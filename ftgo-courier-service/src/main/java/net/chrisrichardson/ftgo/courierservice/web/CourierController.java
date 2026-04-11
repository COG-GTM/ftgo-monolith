package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.AssignCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.AssignCourierResponse;
import net.chrisrichardson.ftgo.courierservice.api.AvailableCourierDTO;
import net.chrisrichardson.ftgo.courierservice.api.CourierActionDTO;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateCourierLocation(@PathVariable long courierId, @RequestBody CourierAvailability availability) {
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<Courier> get(@PathVariable long courierId) {
    Courier courier = courierService.findCourierById(courierId);
    return new ResponseEntity<>(courier, HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/available", method= RequestMethod.GET)
  public ResponseEntity<List<AvailableCourierDTO>> getAvailableCouriers() {
    List<AvailableCourierDTO> couriers = courierService.findAllAvailable();
    return new ResponseEntity<>(couriers, HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/assign", method= RequestMethod.POST)
  public ResponseEntity<AssignCourierResponse> assignCourier(@PathVariable long courierId, @RequestBody AssignCourierRequest request) {
    courierService.assignCourier(courierId, request.getOrderId(), request.getPickupTime(), request.getDropoffTime());
    return new ResponseEntity<>(new AssignCourierResponse(courierId), HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/actions/{orderId}", method= RequestMethod.GET)
  public ResponseEntity<List<CourierActionDTO>> getActionsForOrder(@PathVariable long courierId, @PathVariable long orderId) {
    List<CourierActionDTO> actions = courierService.getActionsForOrder(courierId, orderId);
    return new ResponseEntity<>(actions, HttpStatus.OK);
  }

}
