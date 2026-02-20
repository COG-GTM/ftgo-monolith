package com.ftgo.testutil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TestDataFactory")
class TestDataFactoryTest {

    @BeforeEach
    void setUp() {
        TestDataFactory.resetIdSequence();
    }

    @Test
    @DisplayName("nextId should return sequential IDs")
    void nextIdShouldReturnSequentialIds() {
        Long first = TestDataFactory.nextId();
        Long second = TestDataFactory.nextId();

        assertThat(first).isEqualTo(1001L);
        assertThat(second).isEqualTo(1002L);
    }

    @Test
    @DisplayName("money should return default value")
    void moneyShouldReturnDefault() {
        assertThat(TestDataFactory.money()).isEqualByComparingTo(new BigDecimal("9.99"));
    }

    @Test
    @DisplayName("money(String) should parse amount")
    void moneyShouldParseStringAmount() {
        assertThat(TestDataFactory.money("25.50")).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    @Test
    @DisplayName("money(double) should convert amount")
    void moneyShouldConvertDoubleAmount() {
        assertThat(TestDataFactory.money(12.99)).isEqualByComparingTo(BigDecimal.valueOf(12.99));
    }

    @Test
    @DisplayName("email should return unique values")
    void emailShouldReturnUniqueValues() {
        String first = TestDataFactory.email();
        String second = TestDataFactory.email();

        assertThat(first).isNotEqualTo(second);
        assertThat(first).endsWith("@ftgo-test.com");
    }

    @Test
    @DisplayName("phoneNumber should return valid format")
    void phoneNumberShouldReturnValidFormat() {
        String phone = TestDataFactory.phoneNumber();

        assertThat(phone).startsWith("+1-555-");
    }

    @Test
    @DisplayName("consumerName should return unique values")
    void consumerNameShouldReturnUniqueValues() {
        String first = TestDataFactory.consumerName();
        String second = TestDataFactory.consumerName();

        assertThat(first).startsWith("Test Consumer ");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("restaurantName should return unique values")
    void restaurantNameShouldReturnUniqueValues() {
        String first = TestDataFactory.restaurantName();
        String second = TestDataFactory.restaurantName();

        assertThat(first).startsWith("Test Restaurant ");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("menuItemId should return unique values")
    void menuItemIdShouldReturnUniqueValues() {
        String first = TestDataFactory.menuItemId();
        String second = TestDataFactory.menuItemId();

        assertThat(first).startsWith("menu-item-");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("quantity should return 1 by default")
    void quantityShouldReturnOneByDefault() {
        assertThat(TestDataFactory.quantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("quantity(int) should return specified value")
    void quantityShouldReturnSpecifiedValue() {
        assertThat(TestDataFactory.quantity(5)).isEqualTo(5);
    }

    @Test
    @DisplayName("quantity should reject non-positive values")
    void quantityShouldRejectNonPositive() {
        assertThatThrownBy(() -> TestDataFactory.quantity(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("timestamp should return fixed default")
    void timestampShouldReturnFixedDefault() {
        LocalDateTime ts = TestDataFactory.timestamp();

        assertThat(ts).isEqualTo(LocalDateTime.of(2025, 1, 15, 12, 0, 0));
    }

    @Test
    @DisplayName("timestamp(y,m,d) should use specified date")
    void timestampShouldUseSpecifiedDate() {
        LocalDateTime ts = TestDataFactory.timestamp(2024, 6, 15);

        assertThat(ts.getYear()).isEqualTo(2024);
        assertThat(ts.getMonthValue()).isEqualTo(6);
        assertThat(ts.getDayOfMonth()).isEqualTo(15);
    }

    @Test
    @DisplayName("address methods should return non-empty values")
    void addressMethodsShouldReturnValues() {
        assertThat(TestDataFactory.address()).isNotBlank();
        assertThat(TestDataFactory.street()).isNotBlank();
        assertThat(TestDataFactory.city()).isNotBlank();
        assertThat(TestDataFactory.state()).isNotBlank();
        assertThat(TestDataFactory.zip()).isNotBlank();
    }
}
