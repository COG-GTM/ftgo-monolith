package com.ftgo.domain;

import com.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineItemQuantityChangeTest {

    @Test
    void constructorAndGetters() {
        Money current = new Money(100);
        Money updated = new Money(120);
        Money delta = new Money(20);

        LineItemQuantityChange change = new LineItemQuantityChange(current, updated, delta);

        assertThat(change.getCurrentOrderTotal()).isEqualTo(current);
        assertThat(change.getNewOrderTotal()).isEqualTo(updated);
        assertThat(change.getDelta()).isEqualTo(delta);
    }
}
