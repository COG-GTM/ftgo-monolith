package net.chrisrichardson.ftgo.courierservice.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CourierServiceTest {

  private CourierRepository courierRepository;
  private CourierService courierService;

  @Before
  public void setUp() {
    courierRepository = mock(CourierRepository.class);
    courierService = new CourierService(courierRepository);
  }

  @Test
  public void createCourierStoresOnlyTheHashOfItsAccessToken() throws Exception {
    CreatedCourier created = courierService.createCourier(
            new PersonName("Jane", "Doe"),
            new Address("1 Main Street", null, "Oakland", "CA", "94619"));

    ArgumentCaptor<Courier> courierCaptor = ArgumentCaptor.forClass(Courier.class);
    verify(courierRepository).save(courierCaptor.capture());
    String storedHash = CourierAccessTokens.hash(created.getAccessToken());
    assertEquals(storedHash, getAccessTokenHash(courierCaptor.getValue()));
    assertNotEquals(created.getAccessToken(), storedHash);
  }

  @Test
  public void findsCourierByHashedAccessToken() {
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(42L);
    String token = "token-to-look-up";
    String hash = CourierAccessTokens.hash(token);
    when(courierRepository.findByAccessTokenHash(hash)).thenReturn(Optional.of(courier));

    assertEquals(Optional.of(42L), courierService.findCourierIdByAccessToken(token));
    verify(courierRepository).findByAccessTokenHash(hash);
  }

  @Test
  public void courierAccessTokenHashIsNotSerialized() throws Exception {
    Courier courier = new Courier(new PersonName("Jane", "Doe"), null);
    String hash = CourierAccessTokens.hash("private-token");
    courier.assignAccessTokenHash(hash);

    String json = new ObjectMapper().writeValueAsString(courier);

    assertFalse(json.contains("accessTokenHash"));
    assertFalse(json.contains(hash));
  }

  private String getAccessTokenHash(Courier courier) throws Exception {
    Field field = Courier.class.getDeclaredField("accessTokenHash");
    field.setAccessible(true);
    return (String) field.get(courier);
  }
}
