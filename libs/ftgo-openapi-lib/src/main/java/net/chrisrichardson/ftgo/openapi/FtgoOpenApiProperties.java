package net.chrisrichardson.ftgo.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.openapi")
public class FtgoOpenApiProperties {

    private String title = "FTGO Microservice API";
    private String description = "FTGO Microservice REST API";
    private String version = "v1";
    private String serverUrl = "http://localhost:8080";
    private String contactName = "FTGO Engineering";
    private String contactEmail = "engineering@ftgo.com";
    private String licenseName = "Apache 2.0";
    private String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";
    private String basePackage = "net.chrisrichardson.ftgo";
    private boolean securityEnabled = false;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getLicenseName() { return licenseName; }
    public void setLicenseName(String licenseName) { this.licenseName = licenseName; }
    public String getLicenseUrl() { return licenseUrl; }
    public void setLicenseUrl(String licenseUrl) { this.licenseUrl = licenseUrl; }
    public String getBasePackage() { return basePackage; }
    public void setBasePackage(String basePackage) { this.basePackage = basePackage; }
    public boolean isSecurityEnabled() { return securityEnabled; }
    public void setSecurityEnabled(boolean securityEnabled) { this.securityEnabled = securityEnabled; }
}
