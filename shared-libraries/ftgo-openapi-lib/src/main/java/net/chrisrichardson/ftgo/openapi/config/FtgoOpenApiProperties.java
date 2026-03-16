package net.chrisrichardson.ftgo.openapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO OpenAPI documentation.
 *
 * <p>All properties are prefixed with {@code ftgo.openapi} and can be
 * overridden in each service's {@code application.properties} or
 * {@code application.yml}.
 *
 * <p>Example configuration:
 * <pre>
 * ftgo.openapi.title=Order Service API
 * ftgo.openapi.version=v1
 * ftgo.openapi.description=Manages order lifecycle
 * ftgo.openapi.base-package=net.chrisrichardson.ftgo.orderservice
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.openapi")
public class FtgoOpenApiProperties {

    /** API title displayed in the OpenAPI specification. */
    private String title = "FTGO API";

    /** API version string (e.g., "v1"). */
    private String version = "v1";

    /** Human-readable description of the API. */
    private String description = "FTGO Microservices REST API";

    /** Contact name for the API. */
    private String contactName = "FTGO Team";

    /** Contact email for the API. */
    private String contactEmail = "";

    /** Base package to scan for REST controllers. */
    private String basePackage = "net.chrisrichardson.ftgo";

    /** Terms of service URL. */
    private String termsOfServiceUrl = "";

    /** License name for the API. */
    private String licenseName = "";

    /** License URL for the API. */
    private String licenseUrl = "";

    /** Whether to enable JWT Bearer authentication in the spec. */
    private boolean securityEnabled = true;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }
}
