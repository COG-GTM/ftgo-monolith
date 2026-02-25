package com.ftgo.common.openapi.annotation;

import com.ftgo.common.openapi.model.ApiErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that declares the standard set of error responses
 * for FTGO API endpoints.
 *
 * <p>Apply this to controller methods to automatically document the
 * standard error responses (400, 401, 403, 500) in the OpenAPI spec:
 *
 * <pre>
 * {@literal @}GetMapping("/{id}")
 * {@literal @}ApiStandardResponses
 * public ResponseEntity&lt;ApiResponse&lt;OrderDTO&gt;&gt; getOrder(@PathVariable Long id) { ... }
 * </pre>
 *
 * <p>Individual methods can add additional responses (e.g., 404, 409)
 * using the standard {@code @ApiResponse} annotation alongside this one.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid input or validation failure",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Authentication required",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Insufficient permissions",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        )
})
public @interface ApiStandardResponses {
}
