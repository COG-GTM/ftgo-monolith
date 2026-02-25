package com.ftgo.common.jpa.id;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IdGenerationStrategyTest {

    @Test
    public void shouldDefineThreeStrategies() {
        assertEquals(3, IdGenerationStrategy.values().length);
    }

    @Test
    public void shouldHaveIdentityStrategy() {
        assertNotNull(IdGenerationStrategy.valueOf("IDENTITY"));
    }

    @Test
    public void shouldHaveTableSequenceStrategy() {
        assertNotNull(IdGenerationStrategy.valueOf("TABLE_SEQUENCE"));
    }

    @Test
    public void shouldHaveUuidStrategy() {
        assertNotNull(IdGenerationStrategy.valueOf("UUID"));
    }
}
