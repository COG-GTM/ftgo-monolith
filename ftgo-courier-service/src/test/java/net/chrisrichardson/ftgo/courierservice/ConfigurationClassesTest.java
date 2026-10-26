package net.chrisrichardson.ftgo.courierservice;

import net.chrisrichardson.ftgo.courierservice.domain.CourierServiceConfiguration;
import net.chrisrichardson.ftgo.courierservice.web.CourierWebConfiguration;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ConfigurationClassesTest {

  @Test
  public void shouldCreateCourierServiceBean() {
    CourierServiceConfiguration config = new CourierServiceConfiguration();
    CourierRepository repo = mock(CourierRepository.class);
    assertThat(config.courierService(repo)).isNotNull();
  }

  @Test
  public void shouldInstantiateCourierWebConfiguration() {
    CourierWebConfiguration config = new CourierWebConfiguration();
    assertThat(config).isNotNull();
  }
}
