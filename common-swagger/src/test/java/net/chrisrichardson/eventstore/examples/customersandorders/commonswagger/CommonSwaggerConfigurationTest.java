package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonSwaggerConfigurationTest {
    @Test
    public void describesFtgoApi() {
        assertEquals("FTGO API", new CommonSwaggerConfiguration().api().getInfo().getTitle());
    }
}
