package com.ftgo.RESTAURANT_SERVICE_PACKAGE.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

/**
 * PROVIDER-SIDE CONTRACT TEST TEMPLATE (Pact)
 *
 * This template demonstrates provider-side contract verification using Pact.
 * The provider runs this test to verify it satisfies contracts defined by consumers.
 *
 * Conventions:
 *   - File location: src/test/java/<package>/contract/
 *   - Naming:        <Provider>ProviderContractTest.java
 *
 * Replace:
 *   - RESTAURANT_SERVICE_PACKAGE -> your service package
 *   - "RestaurantService"        -> provider service name
 *   - RestaurantService, Restaurant, etc. -> your domain classes
 *
 * Dependencies required in build.gradle:
 *   testImplementation 'au.com.dius.pact.provider:junit5:4.6.7'
 *
 * Workflow:
 *   1. Consumer generates Pact file (see ConsumerContractTestTemplate)
 *   2. Pact file is placed in src/test/resources/pacts/ (or fetched from broker)
 *   3. Provider runs this test to verify contract compliance
 *   4. @State methods set up the required provider state for each interaction
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Provider("RestaurantService")
@PactFolder("src/test/resources/pacts")
@DisplayName("Restaurant Service Provider Contract Verification")
class RestaurantServiceProviderContractTest {

    @LocalServerPort
    private int port;

    @MockBean
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    @DisplayName("verify Pact interactions")
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("restaurant with ID 1 exists")
    void restaurantExists() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Sample Restaurant");

        given(restaurantService.findById(1L)).willReturn(Optional.of(restaurant));
    }

    @State("restaurant with ID 999 does not exist")
    void restaurantDoesNotExist() {
        given(restaurantService.findById(999L)).willReturn(Optional.empty());
    }
}
