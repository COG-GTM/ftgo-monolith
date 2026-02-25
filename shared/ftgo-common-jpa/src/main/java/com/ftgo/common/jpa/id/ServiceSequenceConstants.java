package com.ftgo.common.jpa.id;

/**
 * Constants for per-service ID sequence tables.
 *
 * <p>Each microservice uses its own sequence table instead of the
 * shared {@code hibernate_sequence} from the monolith. This eliminates
 * cross-service database dependencies and allows each service to
 * manage its own ID space independently.</p>
 *
 * <h3>Sequence Table Schema</h3>
 * <pre>{@code
 * CREATE TABLE {service}_id_sequence (
 *     next_val BIGINT NOT NULL
 * ) ENGINE = InnoDB;
 * }</pre>
 *
 * <h3>JPA Entity Configuration</h3>
 * <pre>{@code
 * @Entity
 * @Table(name = "orders")
 * public class Order {
 *     @Id
 *     @GeneratedValue(strategy = GenerationType.TABLE,
 *         generator = "order_id_gen")
 *     @TableGenerator(name = "order_id_gen",
 *         table = ServiceSequenceConstants.ORDER_SEQUENCE_TABLE,
 *         pkColumnName = "next_val",
 *         allocationSize = 50)
 *     private Long id;
 * }
 * }</pre>
 */
public final class ServiceSequenceConstants {

    /** Consumer Service ID sequence table name. */
    public static final String CONSUMER_SEQUENCE_TABLE = "consumer_id_sequence";

    /** Courier Service ID sequence table name. */
    public static final String COURIER_SEQUENCE_TABLE = "courier_id_sequence";

    /** Order Service ID sequence table name. */
    public static final String ORDER_SEQUENCE_TABLE = "order_id_sequence";

    /** Restaurant Service ID sequence table name. */
    public static final String RESTAURANT_SEQUENCE_TABLE = "restaurant_id_sequence";

    /**
     * Initial sequence value for migrated services.
     * Set high enough to avoid conflicts with existing monolith IDs.
     */
    public static final long INITIAL_SEQUENCE_VALUE = 1000L;

    /**
     * Default allocation size for TABLE generator.
     * Higher values reduce database round-trips for ID allocation.
     */
    public static final int DEFAULT_ALLOCATION_SIZE = 50;

    private ServiceSequenceConstants() {
        // Utility class - prevent instantiation
    }
}
