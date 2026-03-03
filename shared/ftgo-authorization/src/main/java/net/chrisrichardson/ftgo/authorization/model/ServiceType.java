package net.chrisrichardson.ftgo.authorization.model;

/**
 * Enumerates the bounded context services in the FTGO system.
 *
 * <p>Used by the permission evaluator to determine which service's
 * ownership rules apply when validating resource access.</p>
 */
public enum ServiceType {

    CONSUMER_SERVICE("consumer-service"),
    ORDER_SERVICE("order-service"),
    RESTAURANT_SERVICE("restaurant-service"),
    COURIER_SERVICE("courier-service");

    private final String value;

    ServiceType(String value) {
        this.value = value;
    }

    /**
     * Returns the service type identifier string.
     *
     * @return the service type value
     */
    public String getValue() {
        return value;
    }
}
