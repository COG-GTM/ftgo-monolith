package com.ftgo.testutil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomDataGenerator {

    private static final String[] FIRST_NAMES = {
            "Alice", "Bob", "Carol", "David", "Eve", "Frank", "Grace", "Hank"
    };
    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"
    };
    private static final String[] STREET_NAMES = {
            "Main St", "Oak Ave", "Elm Dr", "Cedar Ln", "Pine Rd", "Maple Ct"
    };
    private static final String[] CITIES = {
            "Springfield", "Riverside", "Fairview", "Madison", "Georgetown", "Salem"
    };
    private static final String[] CUISINES = {
            "Italian", "Chinese", "Mexican", "Japanese", "Indian", "Thai", "French", "American"
    };

    private RandomDataGenerator() {
    }

    public static Long id() {
        return ThreadLocalRandom.current().nextLong(1, 1_000_000);
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String email() {
        return name("user").toLowerCase().replace(" ", ".") + "@ftgo-test.com";
    }

    public static String name(String prefix) {
        return prefix + "-" + uuid().substring(0, 8);
    }

    public static String firstName() {
        return pick(FIRST_NAMES);
    }

    public static String lastName() {
        return pick(LAST_NAMES);
    }

    public static String fullName() {
        return firstName() + " " + lastName();
    }

    public static String phoneNumber() {
        return "+1-555-" + String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 9999));
    }

    public static String street() {
        int number = ThreadLocalRandom.current().nextInt(100, 9999);
        return number + " " + pick(STREET_NAMES);
    }

    public static String city() {
        return pick(CITIES);
    }

    public static String zipCode() {
        return String.format("%05d", ThreadLocalRandom.current().nextInt(10000, 99999));
    }

    public static String restaurantName() {
        return pick(CUISINES) + " Kitchen " + uuid().substring(0, 4);
    }

    public static BigDecimal price() {
        return price(1.00, 50.00);
    }

    public static BigDecimal price(double min, double max) {
        double value = ThreadLocalRandom.current().nextDouble(min, max);
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static int quantity() {
        return ThreadLocalRandom.current().nextInt(1, 10);
    }

    public static <T> T pick(T[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }
}
