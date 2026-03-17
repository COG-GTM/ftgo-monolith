package net.chrisrichardson.ftgo.courierservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Couriers", description = "Courier management API - register, retrieve, and manage courier availability")
public class CourierController {

  private CourierService courierService;

  public CourierController(CourierService courierService) {
    this.courierService = courierService;
  }

  @Operation(summary = "Register a new courier", description = "Creates a new courier in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Courier created successfully",
          content = @Content(schema = @Schema(implementation = CreateCourierResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid courier data")
  })
  @RequestMapping(path="/couriers", method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(@RequestBody CreateCourierRequest request) {
    Courier courier = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(courier.getId()), HttpStatus.OK);
  }

  @Operation(summary = "Update courier availability", description = "Updates the availability status of a courier")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Availability updated successfully"),
      @ApiResponse(responseCode = "404", description = "Courier not found")
  })
  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateCourierLocation(
      @Parameter(description = "Unique courier identifier", required = true) @PathVariable long courierId,
      @RequestBody CourierAvailability availability) {
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Get courier by ID", description = "Retrieves a specific courier by their unique identifier")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Courier found"),
      @ApiResponse(responseCode = "404", description = "Courier not found")
  })
  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<Courier> get(
      @Parameter(description = "Unique courier identifier", required = true) @PathVariable long courierId) {
    Courier courier = courierService.findCourierById(courierId);
    return new ResponseEntity<>(courier, HttpStatus.OK);
  }

}
