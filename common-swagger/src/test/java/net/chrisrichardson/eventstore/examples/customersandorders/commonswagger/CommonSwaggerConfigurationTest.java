package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CommonSwaggerConfigurationTest {
    private final BeanPostProcessor processor = CommonSwaggerConfiguration.antPathHandlerMappings();

    @Test
    public void convertsParsedMappingsToAntMatching() {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        mapping.setPatternParser(new PathPatternParser());
        assertSame(mapping, processor.postProcessBeforeInitialization(mapping, "mapping"));
        assertNull(mapping.getPatternParser());
    }

    @Test
    public void leavesAntMappingsUnchanged() {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        assertSame(mapping, processor.postProcessBeforeInitialization(mapping, "mapping"));
        assertNull(mapping.getPatternParser());
    }

    @Test
    public void leavesOtherBeansUnchanged() {
        Object bean = new Object();
        assertSame(bean, processor.postProcessBeforeInitialization(bean, "other"));
    }
}
