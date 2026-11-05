package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommonSwaggerConfigurationTest {
    @Test
    public void describesFtgoApi() {
        assertEquals("FTGO API", new CommonSwaggerConfiguration().api().getInfo().getTitle());
    }
}
