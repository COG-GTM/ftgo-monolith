package net.chrisrichardson.ftgo.openapi.config;

/**
 * Constants and utilities for API versioning.
 *
 * <p>FTGO uses URL path versioning with the format {@code /api/v{major}/...}.
 *
 * <p><b>Versioning Strategy:</b></p>
 * <ul>
 *   <li><b>Current version:</b> v1 ({@code /api/v1/...})</li>
 *   <li><b>Format:</b> URL path prefix ({@code /api/v{N}/resource})</li>
 *   <li><b>Deprecation policy:</b> Previous versions supported for at least 6 months after
 *       a new version is released. Deprecated versions return a {@code Sunset} header.</li>
 * </ul>
 *
 * <p><b>When to increment the version:</b></p>
 * <ul>
 *   <li>Removing an endpoint or field (breaking change)</li>
 *   <li>Changing a field type (breaking change)</li>
 *   <li>Changing required/optional status of a field (breaking change)</li>
 * </ul>
 *
 * <p><b>Non-breaking changes (no version bump):</b></p>
 * <ul>
 *   <li>Adding new optional fields to responses</li>
 *   <li>Adding new endpoints</li>
 *   <li>Adding new optional query parameters</li>
 * </ul>
 */
public final class ApiVersioning {

    private ApiVersioning() {
    }

    /** Current API version number. */
    public static final int CURRENT_VERSION = 1;

    /** URL prefix for the current API version. */
    public static final String API_V1 = "/api/v1";

    /** Base path pattern for versioned API endpoints. */
    public static final String BASE_PATH = "/api/v{version}";
}
