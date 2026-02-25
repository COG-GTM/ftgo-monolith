package com.ftgo.domain;

import com.ftgo.common.Money;
import com.ftgo.common.PersonName;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConsumerTest {

    @Test
    public void shouldCreateConsumer() {
        PersonName name = new PersonName("John", "Doe");
        Consumer consumer = new Consumer(name);

        assertEquals("John", consumer.getName().getFirstName());
        assertEquals("Doe", consumer.getName().getLastName());
    }

    @Test
    public void shouldValidateOrderByConsumer() {
        Consumer consumer = new Consumer(new PersonName("Jane", "Doe"));
        // Should not throw - business logic is not yet implemented
        consumer.validateOrderByConsumer(new Money(100));
    }
}
