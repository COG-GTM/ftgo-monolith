package com.ftgo.common.jpa.id;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServiceSequenceConstantsTest {

    @Test
    public void shouldDefineConsumerSequenceTable() {
        assertEquals("consumer_id_sequence", ServiceSequenceConstants.CONSUMER_SEQUENCE_TABLE);
    }

    @Test
    public void shouldDefineCourierSequenceTable() {
        assertEquals("courier_id_sequence", ServiceSequenceConstants.COURIER_SEQUENCE_TABLE);
    }

    @Test
    public void shouldDefineOrderSequenceTable() {
        assertEquals("order_id_sequence", ServiceSequenceConstants.ORDER_SEQUENCE_TABLE);
    }

    @Test
    public void shouldDefineRestaurantSequenceTable() {
        assertEquals("restaurant_id_sequence", ServiceSequenceConstants.RESTAURANT_SEQUENCE_TABLE);
    }

    @Test
    public void shouldDefineInitialSequenceValue() {
        assertEquals(1000L, ServiceSequenceConstants.INITIAL_SEQUENCE_VALUE);
    }

    @Test
    public void shouldDefineDefaultAllocationSize() {
        assertEquals(50, ServiceSequenceConstants.DEFAULT_ALLOCATION_SIZE);
    }
}
