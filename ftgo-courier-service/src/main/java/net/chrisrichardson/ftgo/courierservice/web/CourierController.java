package net.chrisrichardson.ftgo.courierservice.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.api.GetCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/couriers")
@Api(tags = "Couriers", description = "Courier management operations")
public class CourierController {

  private CourierService courierService;

  public CourierController(CourierService courierService) {
    this.courierService = courierService;
  }

  @ApiOperation(value = "Create a new courier", response = CreateCourierResponse.class)
  @ApiResponses({
      @ApiResponse(code = 200, message = "Courier created successfully"),
      @ApiResponse(code = 400, message = "Invalid request")
  })
  @RequestMapping(method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(@RequestBody CreateCourierRequest request) {
    Courier courier = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(courier.getId()), HttpStatus.OK);
  }

  @ApiOperation(value = "Update courier availability")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Availability updated"),
      @ApiResponse(code = 404, message = "Courier not found")
  })
  @RequestMapping(path="/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateCourierLocation(@ApiParam(value = "Courier ID", required = true) @PathVariable long courierId, @RequestBody CourierAvailability availability) {
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @ApiOperation(value = "Get courier by ID", response = GetCourierResponse.class)
  @ApiResponses({
      @ApiResponse(code = 200, message = "Courier found"),
      @ApiResponse(code = 404, message = "Courier not found")
  })
  @RequestMapping(path="/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<GetCourierResponse> get(@ApiParam(value = "Courier ID", required = true) @PathVariable long courierId) {
    Courier courier = courierService.findCourierById(courierId);
    GetCourierResponse response = new GetCourierResponse(
        courier.getId(),
        courier.getName(),
        courier.getAddress(),
        Boolean.TRUE.equals(courier.getAvailable())
    );
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

}
