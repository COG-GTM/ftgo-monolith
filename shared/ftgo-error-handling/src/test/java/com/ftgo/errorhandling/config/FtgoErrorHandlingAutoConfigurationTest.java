package com.ftgo.errorhandling.config;

import com.ftgo.errorhandling.handler.GlobalExceptionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Tests for {@link FtgoErrorHandlingAutoConfiguration}.
 * Verifies that the auto-configuration correctly registers the GlobalExceptionHandler bean.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = FtgoErrorHandlingAutoConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
public class FtgoErrorHandlingAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldRegisterGlobalExceptionHandlerBean() {
        assertTrue("GlobalExceptionHandler bean should be registered",
                applicationContext.containsBean("globalExceptionHandler"));

        GlobalExceptionHandler handler = applicationContext.getBean(GlobalExceptionHandler.class);
        assertNotNull("GlobalExceptionHandler should not be null", handler);
    }
}
