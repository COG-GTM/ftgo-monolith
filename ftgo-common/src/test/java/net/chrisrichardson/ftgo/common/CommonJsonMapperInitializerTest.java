package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CommonJsonMapperInitializerTest {

  @Test
  public void initializeRegistersMoneyAndIsoDateSerialization() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CommonJsonMapperInitializer initializer = new CommonJsonMapperInitializer();
    ReflectionTestUtils.setField(initializer, "objectMapper", mapper);

    initializer.initialize();

    assertEquals("\"12.34\"", mapper.writeValueAsString(new Money("12.34")));
    LocalDateTime dateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
    assertEquals("\"2020-01-02T03:04:05\"", mapper.writeValueAsString(dateTime));
    assertFalse(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    assertEquals(dateTime, mapper.readValue("\"2020-01-02T03:04:05\"", LocalDateTime.class));
  }

  @Test(expected = NullPointerException.class)
  public void initializeRequiresAnInjectedMapper() {
    new CommonJsonMapperInitializer().initialize();
  }
}
