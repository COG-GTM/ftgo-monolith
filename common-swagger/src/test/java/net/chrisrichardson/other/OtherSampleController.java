package net.chrisrichardson.other;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only controller OUTSIDE {@code net.chrisrichardson.ftgo}. The FTGO OpenAPI group must
 * exclude it, mirroring the old springfox {@code basePackage} selector.
 */
@RestController
public class OtherSampleController {

    /** Endpoint path used by the tests to assert the operation is filtered out of the FTGO group. */
    public static final String PATH = "/other-sample";

    @GetMapping(PATH)
    public String sample() {
        return "other";
    }
}
