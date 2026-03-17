package net.chrisrichardson.ftgo.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import net.chrisrichardson.ftgo.openapi.config.FtgoOpenApiConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FtgoOpenApiConfiguration.class)
@TestPropertySource(properties = {
        "ftgo.openapi.title=Test API",
        "ftgo.openapi.description=Test Description",
        "ftgo.openapi.version=2.0.0"
})
class FtgoOpenApiConfigurationTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void shouldCreateOpenAPIBeanWithCustomProperties() {
        assertNotNull(openAPI);
        assertEquals("Test API", openAPI.getInfo().getTitle());
        assertEquals("Test Description", openAPI.getInfo().getDescription());
        assertEquals("2.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void shouldIncludeContactInfo() {
        assertNotNull(openAPI.getInfo().getContact());
        assertEquals("FTGO Team", openAPI.getInfo().getContact().getName());
    }

    @Test
    void shouldIncludeLicenseInfo() {
        assertNotNull(openAPI.getInfo().getLicense());
        assertEquals("Apache 2.0", openAPI.getInfo().getLicense().getName());
    }

    @Test
    void shouldIncludeServerInfo() {
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
        assertEquals("/", openAPI.getServers().get(0).getUrl());
    }
}
