package net.chrisrichardson.ftgo.openapi.example;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * OpenAPI annotation examples for FTGO REST endpoints.
 *
 * <p>This interface demonstrates how to annotate REST controllers with
 * SpringDoc/OpenAPI 3 annotations. It serves as a reference for developers
 * migrating from Springfox Swagger 2.x to SpringDoc OpenAPI 3.</p>
 *
 * <p><strong>This is NOT a functional controller.</strong> It is an annotation
 * reference that shows the recommended patterns for documenting FTGO APIs.</p>
 *
 * <h3>Migration from Springfox to SpringDoc</h3>
 * <table>
 *     <tr><th>Springfox (Old)</th><th>SpringDoc (New)</th></tr>
 *     <tr><td>{@code @Api}</td><td>{@code @Tag}</td></tr>
 *     <tr><td>{@code @ApiOperation}</td><td>{@code @Operation}</td></tr>
 *     <tr><td>{@code @ApiParam}</td><td>{@code @Parameter}</td></tr>
 *     <tr><td>{@code @ApiResponse}</td><td>{@code @ApiResponse} (io.swagger.v3)</td></tr>
 *     <tr><td>{@code @ApiModel}</td><td>{@code @Schema}</td></tr>
 *     <tr><td>{@code @ApiModelProperty}</td><td>{@code @Schema}</td></tr>
 *     <tr><td>{@code @ApiIgnore}</td><td>{@code @Parameter(hidden = true)}</td></tr>
 *     <tr><td>{@code @EnableSwagger2}</td><td>Not needed (auto-configured)</td></tr>
 * </table>
 *
 * <h3>Example: Annotated Controller</h3>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/v1/orders")
 * @Tag(name = "Orders", description = "Order management operations")
 * public class OrderController {
 *
 *     @GetMapping("/{orderId}")
 *     @Operation(summary = "Get order by ID",
 *                description = "Retrieves a single order by its unique identifier")
 *     @ApiResponses({
 *         @ApiResponse(responseCode = "200", description = "Order found"),
 *         @ApiResponse(responseCode = "404", description = "Order not found")
 *     })
 *     public ResponseEntity<ApiResponse<OrderDTO>> getOrder(
 *             @Parameter(description = "Order ID", required = true, example = "12345")
 *             @PathVariable Long orderId) {
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * @see net.chrisrichardson.ftgo.openapi.config.FtgoOpenApiAutoConfiguration
 */
@Tag(name = "Orders", description = "Order management operations — Example annotations for FTGO API documentation")
public interface OrderApiExample {

    // -------------------------------------------------------------------------
    // GET /api/v1/orders/{orderId} — Retrieve a single order
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a single order by its unique identifier. "
                    + "Returns the full order details including line items and status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful order retrieval",
                                    value = "{\"status\": \"success\", \"data\": {\"orderId\": 12345, \"consumerId\": 1, \"restaurantId\": 10, \"state\": \"APPROVED\", \"orderTotal\": \"23.50\", \"createdAt\": \"2024-01-15T10:30:00Z\"}, \"timestamp\": \"2024-01-15T10:30:05Z\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Order not found error",
                                    value = "{\"status\": \"error\", \"code\": \"ORDER_NOT_FOUND\", \"message\": \"Order with id 12345 not found\", \"path\": \"/api/v1/orders/12345\", \"timestamp\": \"2024-01-15T10:30:05Z\"}"
                            )
                    )
            )
    })
    void getOrderById(
            @Parameter(description = "Unique order identifier", required = true, example = "12345")
            Long orderId
    );

    // -------------------------------------------------------------------------
    // GET /api/v1/orders — List orders with pagination
    // -------------------------------------------------------------------------

    @Operation(
            summary = "List orders with pagination",
            description = "Retrieves a paginated list of orders. Supports sorting by any order field."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Paginated order list",
                                    value = "{\"status\": \"success\", \"data\": [{\"orderId\": 12345, \"consumerId\": 1, \"state\": \"APPROVED\", \"orderTotal\": \"23.50\"}], \"page\": 0, \"size\": 20, \"totalElements\": 150, \"totalPages\": 8, \"sort\": \"createdAt,desc\", \"timestamp\": \"2024-01-15T10:30:05Z\"}"
                            )
                    )
            )
    })
    void listOrders(
            @Parameter(description = "Page number (0-based)", example = "0")
            int page,
            @Parameter(description = "Page size", example = "20")
            int size,
            @Parameter(description = "Sort criteria (field,direction)", example = "createdAt,desc")
            String sort
    );

    // -------------------------------------------------------------------------
    // POST /api/v1/orders — Create a new order
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Create a new order",
            description = "Creates a new order for a consumer from a specific restaurant."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Order creation response",
                                    value = "{\"status\": \"success\", \"data\": {\"orderId\": 12346, \"state\": \"APPROVAL_PENDING\", \"createdAt\": \"2024-01-15T10:31:00Z\"}, \"message\": \"Order created successfully\", \"timestamp\": \"2024-01-15T10:31:00Z\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    value = "{\"status\": \"error\", \"code\": \"VALIDATION_ERROR\", \"message\": \"Request validation failed\", \"details\": [{\"field\": \"consumerId\", \"rejectedValue\": null, \"message\": \"must not be null\"}], \"path\": \"/api/v1/orders\", \"timestamp\": \"2024-01-15T10:31:00Z\"}"
                            )
                    )
            )
    })
    void createOrder(
            @RequestBody(
                    description = "Order creation request",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create order request",
                                    value = "{\"consumerId\": 1, \"restaurantId\": 10, \"deliveryAddress\": {\"street1\": \"123 Main St\", \"city\": \"Oakland\", \"state\": \"CA\", \"zip\": \"94611\"}, \"lineItems\": [{\"menuItemId\": \"MI-001\", \"quantity\": 2}]}"
                            )
                    )
            )
            Object createOrderRequest
    );

    // -------------------------------------------------------------------------
    // PUT /api/v1/orders/{orderId}/cancel — Cancel an order
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Cancel an order",
            description = "Cancels an existing order. Only orders in APPROVED state can be cancelled."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order cannot be cancelled in its current state")
    })
    void cancelOrder(
            @Parameter(description = "Unique order identifier", required = true, example = "12345")
            Long orderId
    );

    // -------------------------------------------------------------------------
    // DELETE — Not used for orders (use cancel instead)
    // -------------------------------------------------------------------------
    // Note: Following FTGO REST API standards, orders are never deleted.
    // Use the cancel endpoint (PUT /api/v1/orders/{orderId}/cancel) instead.
}
