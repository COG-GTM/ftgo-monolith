package net.chrisrichardson.ftgo.courierservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.ErrorResponseExamples;
import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CourierWorkloadResponse;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Couriers")
public class CourierController {

  private CourierService courierService;

  public CourierController(CourierService courierService) {
    this.courierService = courierService;
  }

  @Operation(summary = "Register a courier",
          description = "Creates an unavailable courier. If the address has coordinates they become the courier's initial location.",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Courier name and home address",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateCourierRequest.class),
                          examples = @ExampleObject(name = "Jane Smith", value = CourierApiExamples.CREATE_COURIER_REQUEST))),
          responses = @ApiResponse(responseCode = "200", description = "Courier created",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateCourierResponse.class),
                          examples = @ExampleObject(name = "Created", value = CourierApiExamples.CREATE_COURIER_RESPONSE))))
  @RequestMapping(path="/couriers", method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(@RequestBody CreateCourierRequest request) {
    Courier courier = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(courier.getId()), HttpStatus.OK);
  }

  @Operation(summary = "Set courier availability",
          description = "Only available couriers are considered when orders are accepted.",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Whether the courier can take deliveries",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourierAvailability.class),
                          examples = {
                                  @ExampleObject(name = "Available", value = CourierApiExamples.AVAILABLE_REQUEST),
                                  @ExampleObject(name = "Unavailable", value = CourierApiExamples.UNAVAILABLE_REQUEST)
                          })),
          responses = {
                  @ApiResponse(responseCode = "200", description = "Availability updated (empty body)", content = @Content),
                  @ApiResponse(responseCode = "500", description = "Courier not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Unknown courier", value = ErrorResponseExamples.INTERNAL_SERVER_ERROR)))
          })
  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateCourierLocation(@Parameter(description = "Courier id", example = "1") @PathVariable long courierId, @RequestBody CourierAvailability availability) {
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Get a courier",
          description = "Returns the courier entity including its delivery plan.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Courier found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = Courier.class),
                                  examples = @ExampleObject(name = "Jane Smith", value = CourierApiExamples.GET_COURIER_RESPONSE))),
                  @ApiResponse(responseCode = "500", description = "Courier not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Unknown courier", value = ErrorResponseExamples.INTERNAL_SERVER_ERROR)))
          })
  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<Courier> get(@Parameter(description = "Courier id", example = "1") @PathVariable long courierId) {
    Courier courier = courierService.findCourierById(courierId);
    return new ResponseEntity<>(courier, HttpStatus.OK);
  }

  @Operation(summary = "Update courier location",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Courier's current coordinates",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourierLocationUpdate.class),
                          examples = @ExampleObject(name = "Oakland", value = CourierApiExamples.LOCATION_UPDATE_REQUEST))),
          responses = {
                  @ApiResponse(responseCode = "200", description = "Location updated (empty body)", content = @Content),
                  @ApiResponse(responseCode = "404", description = "Courier not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Unknown courier", value = CourierApiExamples.COURIER_NOT_FOUND)))
          })
  @RequestMapping(path="/couriers/{courierId}/location", method= RequestMethod.POST)
  public ResponseEntity<String> updateLocation(@Parameter(description = "Courier id", example = "1") @PathVariable long courierId, @RequestBody CourierLocationUpdate locationUpdate) {
    courierService.updateLocation(courierId, locationUpdate.getLatitude(), locationUpdate.getLongitude());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Get courier workload",
          description = "Summarizes the courier's active deliveries, availability and last known location.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Workload",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourierWorkloadResponse.class),
                                  examples = @ExampleObject(name = "One active delivery", value = CourierApiExamples.WORKLOAD_RESPONSE))),
                  @ApiResponse(responseCode = "500", description = "Courier not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Unknown courier", value = ErrorResponseExamples.INTERNAL_SERVER_ERROR)))
          })
  @RequestMapping(path="/couriers/{courierId}/workload", method= RequestMethod.GET)
  public ResponseEntity<CourierWorkloadResponse> getWorkload(@Parameter(description = "Courier id", example = "1") @PathVariable long courierId) {
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
