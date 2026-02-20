package com.ftgo.domain;

import com.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemTest {

    @Test
    void constructorSetsFields() {
        MenuItem item = new MenuItem("item-1", "Burger", new Money(10));

        assertThat(item.getId()).isEqualTo("item-1");
        assertThat(item.getName()).isEqualTo("Burger");
        assertThat(item.getPrice()).isEqualTo(new Money(10));
    }

    @Test
    void equalsAndHashCode() {
        MenuItem item1 = new MenuItem("item-1", "Burger", new Money(10));
        MenuItem item2 = new MenuItem("item-1", "Burger", new Money(10));

        assertThat(item1).isEqualTo(item2);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    void notEqualWhenDifferentId() {
        MenuItem item1 = new MenuItem("item-1", "Burger", new Money(10));
        MenuItem item2 = new MenuItem("item-2", "Burger", new Money(10));

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void toStringContainsFields() {
        MenuItem item = new MenuItem("item-1", "Burger", new Money(10));
        String str = item.toString();

        assertThat(str).contains("item-1");
        assertThat(str).contains("Burger");
    }

    @Test
    void settersWork() {
        MenuItem item = new MenuItem("item-1", "Burger", new Money(10));
        item.setId("item-2");
        item.setName("Pizza");
        item.setPrice(new Money(15));

        assertThat(item.getId()).isEqualTo("item-2");
        assertThat(item.getName()).isEqualTo("Pizza");
        assertThat(item.getPrice()).isEqualTo(new Money(15));
    }
}
