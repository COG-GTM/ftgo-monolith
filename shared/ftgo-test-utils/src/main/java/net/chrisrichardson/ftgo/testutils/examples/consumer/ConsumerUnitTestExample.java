package net.chrisrichardson.ftgo.testutils.examples.consumer;

/**
 * Example unit tests for the Consumer bounded context.
 *
 * <p>Demonstrates testing Consumer domain logic with builders.
 *
 * <pre>{@code
 * // In ftgo-consumer-service/src/test/java/.../ConsumerDomainTest.java:
 *
 * @DisplayName("Consumer Domain Unit Tests")
 * class ConsumerDomainTest {
 *
 *     @Test
 *     @DisplayName("should create consumer with name")
 *     void shouldCreateConsumerWithName() {
 *         Consumer consumer = ConsumerBuilder.aConsumer()
 *             .withFirstName("Jane")
 *             .withLastName("Smith")
 *             .build();
 *
 *         assertThat(consumer.getName().getFirstName()).isEqualTo("Jane");
 *         assertThat(consumer.getName().getLastName()).isEqualTo("Smith");
 *     }
 *
 *     @Test
 *     @DisplayName("should validate order for consumer")
 *     void shouldValidateOrderForConsumer() {
 *         Consumer consumer = ConsumerBuilder.aConsumer().build();
 *         Money orderTotal = MoneyBuilder.dollars(50);
 *
 *         // Should not throw - validates order is acceptable
 *         consumer.validateOrderByConsumer(orderTotal);
 *     }
 *
 *     @Test
 *     @DisplayName("should create consumer with PersonName builder")
 *     void shouldCreateConsumerWithPersonNameBuilder() {
 *         PersonName name = PersonNameBuilder.aPersonName()
 *             .withFirstName("Alice")
 *             .withLastName("Johnson")
 *             .build();
 *
 *         Consumer consumer = ConsumerBuilder.aConsumer()
 *             .withName(name)
 *             .build();
 *
 *         assertThat(consumer.getName()).isEqualTo(name);
 *     }
 * }
 * }</pre>
 *
 * <pre>{@code
 * // In ftgo-consumer-service/src/test/java/.../ConsumerServiceTest.java:
 *
 * @ExtendWith(MockitoExtension.class)
 * @DisplayName("Consumer Service Unit Tests")
 * class ConsumerServiceTest {
 *
 *     @Mock
 *     private ConsumerRepository consumerRepository;
 *
 *     @InjectMocks
 *     private ConsumerService consumerService;
 *
 *     @Test
 *     @DisplayName("should create consumer and return with ID")
 *     void shouldCreateConsumer() {
 *         Consumer consumer = ConsumerBuilder.aConsumer().build();
 *         when(consumerRepository.save(any(Consumer.class))).thenReturn(consumer);
 *
 *         Consumer result = consumerService.create(
 *             new PersonName("John", "Doe")
 *         );
 *
 *         assertThat(result).isNotNull();
 *         verify(consumerRepository).save(any(Consumer.class));
 *     }
 *
 *     @Test
 *     @DisplayName("should validate order for existing consumer")
 *     void shouldValidateOrderForExistingConsumer() {
 *         Consumer consumer = ConsumerBuilder.aConsumer().build();
 *         when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));
 *
 *         consumerService.validateOrderForConsumer(1L, MoneyBuilder.dollars(50));
 *
 *         verify(consumerRepository).findById(1L);
 *     }
 * }
 * }</pre>
 */
public final class ConsumerUnitTestExample {
    private ConsumerUnitTestExample() {
        // Documentation-only class
    }
}
