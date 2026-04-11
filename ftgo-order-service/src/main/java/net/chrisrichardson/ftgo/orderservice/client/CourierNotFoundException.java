package net.chrisrichardson.ftgo.orderservice.client;

public class CourierNotFoundException extends RuntimeException {

    private final long courierId;

    public CourierNotFoundException(long courierId) {
        super("Courier not found: " + courierId);
        this.courierId = courierId;
    }

    public long getCourierId() {
        return courierId;
    }
}
