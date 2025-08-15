package net.chrisrichardson.ftgo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FtgoApplicationMain.class)
@AutoConfigureWebMvc
public class InternationalizationIntegrationTest {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testMessageSourceConfiguration() {
        String englishMessage = messageSource.getMessage("app.title", null, Locale.ENGLISH);
        assertEquals("FTGO Food Delivery", englishMessage);

        String arabicMessage = messageSource.getMessage("app.title", null, new Locale("ar"));
        assertNotNull(arabicMessage);
        assertTrue(arabicMessage.contains("FTGO"));
    }

    @Test
    public void testLocaleResolverConfiguration() {
        assertNotNull(localeResolver);
        assertEquals(Locale.ENGLISH, localeResolver.resolveLocale(null));
    }

    @Test
    public void testOrderMessages() {
        String englishOrderCreated = messageSource.getMessage("order.created", null, Locale.ENGLISH);
        assertEquals("Order created successfully", englishOrderCreated);

        String arabicOrderCreated = messageSource.getMessage("order.created", null, new Locale("ar"));
        assertNotNull(arabicOrderCreated);
    }

    @Test
    public void testConsumerMessages() {
        String englishConsumerCreated = messageSource.getMessage("consumer.created", null, Locale.ENGLISH);
        assertEquals("Customer created successfully", englishConsumerCreated);

        String arabicConsumerCreated = messageSource.getMessage("consumer.created", null, new Locale("ar"));
        assertNotNull(arabicConsumerCreated);
    }

    @Test
    public void testRestaurantMessages() {
        String englishRestaurantCreated = messageSource.getMessage("restaurant.created", null, Locale.ENGLISH);
        assertEquals("Restaurant created successfully", englishRestaurantCreated);

        String arabicRestaurantCreated = messageSource.getMessage("restaurant.created", null, new Locale("ar"));
        assertNotNull(arabicRestaurantCreated);
    }

    @Test
    public void testRtlDirectionIndicator() {
        String rtlDirection = messageSource.getMessage("text.direction", null, new Locale("ar"));
        assertEquals("rtl", rtlDirection);
    }
}
