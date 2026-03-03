package com.ftgo.openapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO OpenAPI documentation.
 *
 * <p>These properties can be set in application.yml or application.properties:
 * <pre>
 * ftgo:
 *   openapi:
 *     title: FTGO Order Service API
 *     description: REST API for managing orders in the FTGO platform
 *     version: v1
 *     contact-name: FTGO Engineering
 *     contact-email: engineering@ftgo.com
 *     contact-url: https://github.com/COG-GTM/ftgo-monolith
 *     base-package: net.chrisrichardson.ftgo
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.openapi")
public class FtgoOpenApiProperties {

    /** API title displayed in Swagger UI header. */
    private String title = "FTGO API";

    /** API description displayed in Swagger UI. */
    private String description = "FTGO Microservices REST API";

    /** API version (e.g., v1, v2). */
    private String version = "v1";

    /** Contact name for the API. */
    private String contactName = "FTGO Engineering";

    /** Contact email for the API. */
    private String contactEmail = "";

    /** Contact URL for the API. */
    private String contactUrl = "https://github.com/COG-GTM/ftgo-monolith";

    /** License name for the API. */
    private String licenseName = "Apache 2.0";

    /** License URL for the API. */
    private String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";

    /** Base package to scan for API controllers. */
    private String basePackage = "net.chrisrichardson.ftgo";

    /** External documentation URL. */
    private String externalDocsUrl = "";

    /** External documentation description. */
    private String externalDocsDescription = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getContactUrl() {
        return contactUrl;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
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

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getExternalDocsUrl() {
        return externalDocsUrl;
    }

    public void setExternalDocsUrl(String externalDocsUrl) {
        this.externalDocsUrl = externalDocsUrl;
    }

    public String getExternalDocsDescription() {
        return externalDocsDescription;
    }

    public void setExternalDocsDescription(String externalDocsDescription) {
        this.externalDocsDescription = externalDocsDescription;
    }
}
