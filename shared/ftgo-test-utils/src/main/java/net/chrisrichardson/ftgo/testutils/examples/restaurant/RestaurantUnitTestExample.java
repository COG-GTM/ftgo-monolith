package net.chrisrichardson.ftgo.testutils.examples.restaurant;

/**
 * Example unit tests for the Restaurant bounded context.
 *
 * <p>Demonstrates testing Restaurant domain logic with builders.
 *
 * <pre>{@code
 * // In ftgo-restaurant-service/src/test/java/.../RestaurantDomainTest.java:
 *
 * @DisplayName("Restaurant Domain Unit Tests")
 * class RestaurantDomainTest {
 *
 *     @Test
 *     @DisplayName("should create restaurant with name and address")
 *     void shouldCreateRestaurantWithNameAndAddress() {
 *         Address address = AddressBuilder.anAddress()
 *             .withStreet1("1 University Ave")
 *             .withCity("Berkeley")
 *             .build();
 *
 *         Restaurant restaurant = RestaurantBuilder.aRestaurant()
 *             .withName("Ajanta")
 *             .withAddress(address)
 *             .build();
 *
 *         assertThat(restaurant.getName()).isEqualTo("Ajanta");
 *         assertThat(restaurant.getAddress().getCity()).isEqualTo("Berkeley");
 *     }
 *
 *     @Test
 *     @DisplayName("should find menu item by ID")
 *     void shouldFindMenuItemById() {
 *         MenuItem vindaloo = MenuItemBuilder.aMenuItem()
 *             .withId("1").withName("Chicken Vindaloo").withPrice("12.34").build();
 *         MenuItem naan = MenuItemBuilder.aMenuItem()
 *             .withId("2").withName("Naan").withPrice("3.00").build();
 *
 *         Restaurant restaurant = RestaurantBuilder.aRestaurant()
 *             .withMenuItems(vindaloo, naan)
 *             .build();
 *
 *         Optional<MenuItem> found = restaurant.findMenuItem("1");
 *
 *         assertThat(found).isPresent();
 *         assertThat(found.get().getName()).isEqualTo("Chicken Vindaloo");
 *     }
 *
 *     @Test
 *     @DisplayName("should return empty for non-existent menu item")
 *     void shouldReturnEmptyForNonExistentMenuItem() {
 *         Restaurant restaurant = RestaurantBuilder.aRestaurant().build();
 *
 *         assertThat(restaurant.findMenuItem("999")).isEmpty();
 *     }
 *
 *     @Test
 *     @DisplayName("should create restaurant with ID for mock scenarios")
 *     void shouldCreateRestaurantWithId() {
 *         Restaurant restaurant = RestaurantBuilder.aRestaurant()
 *             .withId(42L)
 *             .withName("Test Restaurant")
 *             .buildWithId();
 *
 *         assertThat(restaurant.getId()).isEqualTo(42L);
 *         assertThat(restaurant.getName()).isEqualTo("Test Restaurant");
 *     }
 * }
 * }</pre>
 *
 * <pre>{@code
 * // In ftgo-restaurant-service/src/test/java/.../RestaurantServiceTest.java:
 *
 * @ExtendWith(MockitoExtension.class)
 * @DisplayName("Restaurant Service Unit Tests")
 * class RestaurantServiceTest {
 *
 *     @Mock
 *     private RestaurantRepository restaurantRepository;
 *
 *     @InjectMocks
 *     private RestaurantService restaurantService;
 *
 *     @Test
 *     @DisplayName("should create restaurant")
 *     void shouldCreateRestaurant() {
 *         Restaurant restaurant = RestaurantBuilder.aRestaurant()
 *             .withName("New Restaurant")
 *             .build();
 *         when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);
 *
 *         Restaurant result = restaurantService.create(
 *             "New Restaurant",
 *             AddressBuilder.anAddress().build(),
 *             new RestaurantMenu(Collections.singletonList(
 *                 MenuItemBuilder.aMenuItem().build()
 *             ))
 *         );
 *
 *         assertThat(result).isNotNull();
 *         verify(restaurantRepository).save(any(Restaurant.class));
 *     }
 * }
 * }</pre>
 */
public final class RestaurantUnitTestExample {
    private RestaurantUnitTestExample() {
        // Documentation-only class
    }
}
