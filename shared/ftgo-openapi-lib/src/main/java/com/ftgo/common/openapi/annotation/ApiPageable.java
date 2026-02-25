package com.ftgo.common.openapi.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that documents standard pagination query parameters
 * for FTGO API endpoints.
 *
 * <p>Apply this to controller methods that accept pagination parameters
 * to automatically document them in the OpenAPI spec:
 *
 * <pre>
 * {@literal @}GetMapping
 * {@literal @}ApiPageable
 * public ResponseEntity&lt;PagedResponse&lt;OrderDTO&gt;&gt; listOrders(
 *         {@literal @}RequestParam(defaultValue = "0") int page,
 *         {@literal @}RequestParam(defaultValue = "20") int size,
 *         {@literal @}RequestParam(defaultValue = "createdAt") String sort,
 *         {@literal @}RequestParam(defaultValue = "DESC") String direction) { ... }
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
        name = "page",
        description = "Zero-based page number",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")
)
@Parameter(
        name = "size",
        description = "Number of items per page",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100")
)
@Parameter(
        name = "sort",
        description = "Field name to sort by (e.g., createdAt, name, status)",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "string", defaultValue = "createdAt")
)
@Parameter(
        name = "direction",
        description = "Sort direction: ASC or DESC",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "string", defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
)
public @interface ApiPageable {
}
