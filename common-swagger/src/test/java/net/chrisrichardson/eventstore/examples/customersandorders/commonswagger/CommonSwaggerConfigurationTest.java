package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

import net.chrisrichardson.ftgo.commonswagger.testapi.FtgoSampleController;
import net.chrisrichardson.other.OtherSampleController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test for the shared springdoc configuration: boots a minimal web context with
 * {@link CommonSwaggerConfiguration} plus two sample controllers and checks the endpoints that
 * the FTGO scripts and README rely on ({@code /swagger-ui.html}, {@code /v3/api-docs}).
 */
@SpringBootTest(classes = CommonSwaggerConfigurationTest.TestApp.class)
@AutoConfigureMockMvc
class CommonSwaggerConfigurationTest {

    /**
     * Minimal Boot application for the test: auto-configuration (MVC + springdoc), the shared
     * swagger configuration under test, and one controller inside/outside the FTGO package.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({CommonSwaggerConfiguration.class, FtgoSampleController.class, OtherSampleController.class})
    static class TestApp {
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerUiHtmlRedirectsToSpringdocSwaggerUi() throws Exception {
        // springdoc keeps the legacy springfox URL alive as a redirect to its own UI page.
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    void openApiJsonIsServedWithFtgoTitle() throws Exception {
        // The default (ungrouped) document carries the shared OpenAPI bean's metadata.
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("FTGO"))
                .andExpect(jsonPath("$.paths").value(hasKey(FtgoSampleController.PATH)));
    }

    @Test
    void ftgoGroupDocumentsOnlyControllersInFtgoPackage() throws Exception {
        // The 'ftgo' group replaces RequestHandlerSelectors.basePackage("net.chrisrichardson.ftgo").
        mockMvc.perform(get("/v3/api-docs/" + CommonSwaggerConfiguration.FTGO_GROUP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("FTGO"))
                .andExpect(jsonPath("$.paths").value(hasKey(FtgoSampleController.PATH)))
                .andExpect(jsonPath("$.paths").value(not(hasKey(OtherSampleController.PATH))));
    }

    @Test
    void asyncResponseEntityReturnTypeIsUnwrappedToPayloadSchema() throws Exception {
        // CompletableFuture<ResponseEntity<String>> must be documented as a plain string body,
        // which is what the former springfox genericModelSubstitutes/alternateTypeRules achieved.
        mockMvc.perform(get("/v3/api-docs/" + CommonSwaggerConfiguration.FTGO_GROUP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['" + FtgoSampleController.PATH
                        + "'].get.responses['200'].content['*/*'].schema.type").value("string"));
    }

    @Test
    void swaggerConfigListsFtgoGroup() throws Exception {
        // Swagger UI reads this config to populate its group drop-down.
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[?(@.name == '" + CommonSwaggerConfiguration.FTGO_GROUP + "')]").exists());
    }
}
