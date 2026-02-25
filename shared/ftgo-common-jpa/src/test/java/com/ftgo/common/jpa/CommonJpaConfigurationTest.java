package com.ftgo.common.jpa;

import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CommonJpaConfigurationTest {

    @Test
    public void shouldBeAnnotatedWithConfiguration() {
        assertTrue("CommonJpaConfiguration should be annotated with @Configuration",
                CommonJpaConfiguration.class.isAnnotationPresent(Configuration.class));
    }

    @Test
    public void shouldBeAnnotatedWithImport() {
        assertTrue("CommonJpaConfiguration should be annotated with @Import",
                CommonJpaConfiguration.class.isAnnotationPresent(Import.class));
    }

    @Test
    public void shouldBeInstantiable() {
        CommonJpaConfiguration config = new CommonJpaConfiguration();
        assertNotNull("CommonJpaConfiguration should be instantiable", config);
    }
}
