package net.chrisrichardson.ftgo.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class for API contract tests.
 *
 * <p>Provides a standardized setup for REST-Assured MockMvc-based contract tests.
 * Service-specific contract test classes should extend this class and override
 * {@link #setup()} to configure any required mocks or test data.
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * @SpringBootTest
 * class OrderServiceContractTest extends ContractTestBase {
 *
 *     @MockBean
 *     private OrderService orderService;
 *
 *     @Override
 *     protected void setup() {
 *         super.setup();
 *         // Configure mocks
 *         when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
 *     }
 * }
 * }</pre>
 */
public abstract class ContractTestBase {

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    protected void setup() {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
