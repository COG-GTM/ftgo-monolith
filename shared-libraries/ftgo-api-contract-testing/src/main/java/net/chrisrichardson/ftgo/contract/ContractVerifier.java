package net.chrisrichardson.ftgo.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Utility for verifying API contracts between services.
 *
 * <p>Provides methods to load contract definitions from resources and verify
 * that API responses conform to the expected contract structure. This enables
 * consumer-driven contract testing between FTGO microservices.
 *
 * <p><b>Contract File Format:</b></p>
 * Contract files are JSON files stored in {@code src/test/resources/contracts/}:
 * <pre>
 * {
 *   "consumer": "order-service",
 *   "provider": "restaurant-service",
 *   "interactions": [
 *     {
 *       "description": "get restaurant by id",
 *       "request": {
 *         "method": "GET",
 *         "path": "/restaurants/1"
 *       },
 *       "response": {
 *         "status": 200,
 *         "body": {
 *           "id": 1,
 *           "name": "Test Restaurant"
 *         }
 *       }
 *     }
 *   ]
 * }
 * </pre>
 */
public class ContractVerifier {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ContractVerifier() {
    }

    /**
     * Loads a contract definition from the classpath.
     *
     * @param contractPath path to the contract JSON file on the classpath
     * @return the parsed contract as a JsonNode
     * @throws IOException if the contract file cannot be read or parsed
     */
    public static JsonNode loadContract(String contractPath) throws IOException {
        try (InputStream is = ContractVerifier.class.getClassLoader().getResourceAsStream(contractPath)) {
            Objects.requireNonNull(is, "Contract file not found: " + contractPath);
            return objectMapper.readTree(is);
        }
    }

    /**
     * Verifies that a JSON response body contains all fields specified in the contract.
     *
     * @param actual   the actual JSON response body
     * @param expected the expected contract response body
     * @return true if all expected fields are present in the actual response
     */
    public static boolean verifyResponseStructure(JsonNode actual, JsonNode expected) {
        if (expected.isObject()) {
            var fields = expected.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                if (!actual.has(field)) {
                    return false;
                }
                if (expected.get(field).isObject() || expected.get(field).isArray()) {
                    if (!verifyResponseStructure(actual.get(field), expected.get(field))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
