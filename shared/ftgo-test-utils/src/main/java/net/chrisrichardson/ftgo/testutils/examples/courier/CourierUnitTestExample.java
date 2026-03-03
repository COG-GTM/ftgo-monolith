package net.chrisrichardson.ftgo.testutils.examples.courier;

/**
 * Example unit tests for the Courier bounded context.
 *
 * <p>Demonstrates testing Courier domain logic with builders.
 *
 * <pre>{@code
 * // In ftgo-courier-service/src/test/java/.../CourierDomainTest.java:
 *
 * @DisplayName("Courier Domain Unit Tests")
 * class CourierDomainTest {
 *
 *     @Test
 *     @DisplayName("should create courier with name and address")
 *     void shouldCreateCourierWithNameAndAddress() {
 *         Address address = AddressBuilder.anAddress()
 *             .withCity("San Francisco")
 *             .build();
 *
 *         Courier courier = CourierBuilder.aCourier()
 *             .withFirstName("Mike")
 *             .withLastName("Driver")
 *             .withAddress(address)
 *             .build();
 *
 *         assertThat(courier).isNotNull();
 *     }
 *
 *     @Test
 *     @DisplayName("should mark courier as available")
 *     void shouldMarkCourierAsAvailable() {
 *         Courier courier = CourierBuilder.aCourier().build();
 *
 *         courier.noteAvailable();
 *
 *         assertThat(courier.isAvailable()).isTrue();
 *     }
 *
 *     @Test
 *     @DisplayName("should mark courier as unavailable")
 *     void shouldMarkCourierAsUnavailable() {
 *         Courier courier = CourierBuilder.aCourier().build();
 *         courier.noteAvailable();
 *
 *         courier.noteUnavailable();
 *
 *         assertThat(courier.isAvailable()).isFalse();
 *     }
 * }
 * }</pre>
 *
 * <pre>{@code
 * // In ftgo-courier-service/src/test/java/.../CourierServiceTest.java:
 *
 * @ExtendWith(MockitoExtension.class)
 * @DisplayName("Courier Service Unit Tests")
 * class CourierServiceTest {
 *
 *     @Mock
 *     private CourierRepository courierRepository;
 *
 *     @InjectMocks
 *     private CourierService courierService;
 *
 *     @Test
 *     @DisplayName("should update courier availability")
 *     void shouldUpdateCourierAvailability() {
 *         Courier courier = CourierBuilder.aCourier().build();
 *         when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
 *
 *         courierService.updateAvailability(1L, true);
 *
 *         assertThat(courier.isAvailable()).isTrue();
 *         verify(courierRepository).findById(1L);
 *     }
 *
 *     @Test
 *     @DisplayName("should create courier")
 *     void shouldCreateCourier() {
 *         Courier courier = CourierBuilder.aCourier().build();
 *         when(courierRepository.save(any(Courier.class))).thenReturn(courier);
 *
 *         Courier result = courierService.create(
 *             PersonNameBuilder.aPersonName().build(),
 *             AddressBuilder.anAddress().build()
 *         );
 *
 *         assertThat(result).isNotNull();
 *         verify(courierRepository).save(any(Courier.class));
 *     }
 * }
 * }</pre>
 */
public final class CourierUnitTestExample {
    private CourierUnitTestExample() {
        // Documentation-only class
    }
}
