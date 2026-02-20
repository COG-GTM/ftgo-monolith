package com.ftgo.testutil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RandomDataGenerator")
class RandomDataGeneratorTest {

    @Test
    @DisplayName("id should return positive value")
    void idShouldReturnPositive() {
        Long id = RandomDataGenerator.id();

        assertThat(id).isPositive();
    }

    @Test
    @DisplayName("uuid should return valid UUID format")
    void uuidShouldReturnValidFormat() {
        String uuid = RandomDataGenerator.uuid();

        assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("email should contain @ and domain")
    void emailShouldBeValid() {
        String email = RandomDataGenerator.email();

        assertThat(email).contains("@ftgo-test.com");
    }

    @Test
    @DisplayName("name should include prefix")
    void nameShouldIncludePrefix() {
        String name = RandomDataGenerator.name("order");

        assertThat(name).startsWith("order-");
    }

    @Test
    @DisplayName("firstName should return non-empty value")
    void firstNameShouldReturnValue() {
        assertThat(RandomDataGenerator.firstName()).isNotBlank();
    }

    @Test
    @DisplayName("lastName should return non-empty value")
    void lastNameShouldReturnValue() {
        assertThat(RandomDataGenerator.lastName()).isNotBlank();
    }

    @Test
    @DisplayName("fullName should contain space separator")
    void fullNameShouldContainSpace() {
        String name = RandomDataGenerator.fullName();

        assertThat(name).contains(" ");
    }

    @Test
    @DisplayName("phoneNumber should start with +1-555-")
    void phoneNumberShouldHavePrefix() {
        String phone = RandomDataGenerator.phoneNumber();

        assertThat(phone).startsWith("+1-555-");
    }

    @Test
    @DisplayName("street should contain number and name")
    void streetShouldBeFormatted() {
        String street = RandomDataGenerator.street();

        assertThat(street).matches("\\d+ .+");
    }

    @Test
    @DisplayName("city should return non-empty value")
    void cityShouldReturnValue() {
        assertThat(RandomDataGenerator.city()).isNotBlank();
    }

    @Test
    @DisplayName("zipCode should be 5 digits")
    void zipCodeShouldBe5Digits() {
        String zip = RandomDataGenerator.zipCode();

        assertThat(zip).matches("\\d{5}");
    }

    @Test
    @DisplayName("restaurantName should return non-empty value")
    void restaurantNameShouldReturnValue() {
        assertThat(RandomDataGenerator.restaurantName()).isNotBlank();
    }

    @Test
    @DisplayName("price should return value in default range")
    void priceShouldBeInDefaultRange() {
        BigDecimal price = RandomDataGenerator.price();

        assertThat(price).isBetween(new BigDecimal("1.00"), new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("price should return value in custom range")
    void priceShouldBeInCustomRange() {
        BigDecimal price = RandomDataGenerator.price(10.00, 20.00);

        assertThat(price).isBetween(new BigDecimal("10.00"), new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("price should have 2 decimal places")
    void priceShouldHaveTwoDecimals() {
        BigDecimal price = RandomDataGenerator.price();

        assertThat(price.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("quantity should return value between 1 and 9")
    void quantityShouldBeInRange() {
        int qty = RandomDataGenerator.quantity();

        assertThat(qty).isBetween(1, 9);
    }

    @RepeatedTest(5)
    @DisplayName("pick should return element from array")
    void pickShouldReturnFromArray() {
        String[] options = {"A", "B", "C"};

        String picked = RandomDataGenerator.pick(options);

        assertThat(picked).isIn("A", "B", "C");
    }
}
