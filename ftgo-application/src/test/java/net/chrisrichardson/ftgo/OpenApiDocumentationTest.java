package net.chrisrichardson.ftgo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FtgoApplicationMain.class)
@AutoConfigureMockMvc
@Import(OpenApiDocumentationTest.WrapperController.class)
public class OpenApiDocumentationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    public void unwrapsResponseAndAsyncContainers() throws Exception {
        JsonNode spec = mapper.readTree(mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        for (String path : new String[]{"entity", "future", "deferred"}) {
            assertEquals("#/components/schemas/Payload", spec.at("/paths/~1openapi-test~1" + path
                    + "/get/responses/200/content/*~1*/schema/$ref").asText());
        }
        for (String name : new String[]{"ResponseEntity", "CompletableFuture", "DeferredResult"}) {
            assertFalse(spec.path("components").path("schemas").has(name));
        }
    }

    @RestController
    static class WrapperController {
        @GetMapping("/openapi-test/entity")
        public ResponseEntity<Payload> entity() { return ResponseEntity.ok(new Payload()); }

        @GetMapping("/openapi-test/future")
        public CompletableFuture<ResponseEntity<Payload>> future() {
            return CompletableFuture.completedFuture(entity());
        }

        @GetMapping("/openapi-test/deferred")
        public DeferredResult<ResponseEntity<Payload>> deferred() {
            DeferredResult<ResponseEntity<Payload>> result = new DeferredResult<>();
            result.setResult(entity());
            return result;
        }
    }

    static class Payload {
        public String getValue() { return "value"; }
    }
}
