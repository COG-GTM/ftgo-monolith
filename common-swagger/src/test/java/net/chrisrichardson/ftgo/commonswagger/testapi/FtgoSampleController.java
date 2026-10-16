package net.chrisrichardson.ftgo.commonswagger.testapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Test-only controller living under {@code net.chrisrichardson.ftgo}, i.e. inside the package the
 * FTGO OpenAPI group scans. Its endpoint must appear in the generated spec.
 *
 * <p>Returns {@code CompletableFuture<ResponseEntity<String>>} so the test also proves that springdoc
 * unwraps the async/response wrappers that the old springfox Docket had explicit rules for.
 */
@RestController
public class FtgoSampleController {

    /** Endpoint path used by the tests to look the operation up in the OpenAPI document. */
    public static final String PATH = "/ftgo-sample";

    @GetMapping(PATH)
    public CompletableFuture<ResponseEntity<String>> sample() {
        return CompletableFuture.completedFuture(ResponseEntity.ok("ftgo"));
    }
}
