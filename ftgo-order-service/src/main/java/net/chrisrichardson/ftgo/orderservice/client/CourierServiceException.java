package net.chrisrichardson.ftgo.orderservice.client;

public class CourierServiceException extends RuntimeException {

    public CourierServiceException(String message) {
        super(message);
    }

    public CourierServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
