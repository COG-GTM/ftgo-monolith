package com.ftgo.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStateTest {

    @Test
    void allStatesExist() {
        assertThat(OrderState.values()).containsExactly(
                OrderState.APPROVED,
                OrderState.ACCEPTED,
                OrderState.PREPARING,
                OrderState.READY_FOR_PICKUP,
                OrderState.PICKED_UP,
                OrderState.DELIVERED,
                OrderState.CANCELLED
        );
    }

    @Test
    void valueOfApproved() {
        assertThat(OrderState.valueOf("APPROVED")).isEqualTo(OrderState.APPROVED);
    }

    @Test
    void valueOfCancelled() {
        assertThat(OrderState.valueOf("CANCELLED")).isEqualTo(OrderState.CANCELLED);
    }
}
