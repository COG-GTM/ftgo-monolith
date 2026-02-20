package com.ftgo.testutil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public final class TestDataFactory {

    private static final AtomicLong ID_SEQUENCE = new AtomicLong(1000);

    private TestDataFactory() {
    }

    public static Long nextId() {
        return ID_SEQUENCE.incrementAndGet();
    }

    public static BigDecimal money() {
        return new BigDecimal("9.99");
    }

    public static BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }

    public static BigDecimal money(double amount) {
        return BigDecimal.valueOf(amount);
    }

    public static String address() {
        return "123 Main St, Springfield, IL 62704";
    }

    public static String street() {
        return "123 Main St";
    }

    public static String city() {
        return "Springfield";
    }

    public static String state() {
        return "IL";
    }

    public static String zip() {
        return "62704";
    }

    public static String email() {
        return "test-" + nextId() + "@ftgo-test.com";
    }

    public static String phoneNumber() {
        return "+1-555-" + String.format("%04d", nextId() % 10000);
    }

    public static String consumerName() {
        return "Test Consumer " + nextId();
    }

    public static String restaurantName() {
        return "Test Restaurant " + nextId();
    }

    public static String menuItemId() {
        return "menu-item-" + nextId();
    }

    public static String menuItemName() {
        return "Test Menu Item " + nextId();
    }

    public static int quantity() {
        return 1;
    }

    public static int quantity(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return value;
    }

    public static LocalDateTime timestamp() {
        return LocalDateTime.of(2025, 1, 15, 12, 0, 0);
    }

    public static LocalDateTime timestamp(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 12, 0, 0);
    }

    public static void resetIdSequence() {
        ID_SEQUENCE.set(1000);
    }
}
