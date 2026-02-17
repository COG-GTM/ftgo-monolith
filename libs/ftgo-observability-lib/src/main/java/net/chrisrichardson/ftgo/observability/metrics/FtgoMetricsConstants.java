package net.chrisrichardson.ftgo.observability.metrics;

public final class FtgoMetricsConstants {

    private FtgoMetricsConstants() {
    }

    public static final String TAG_SERVICE = "service";
    public static final String TAG_OPERATION = "operation";
    public static final String TAG_STATUS = "status";
    public static final String TAG_ORDER_STATE = "state";
    public static final String TAG_RESTAURANT_ID = "restaurant_id";
    public static final String TAG_COURIER_ID = "courier_id";
    public static final String TAG_CONSUMER_ID = "consumer_id";
    public static final String TAG_PAYMENT_METHOD = "payment_method";

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILURE = "failure";

    public static final String PREFIX_ORDER = "ftgo.order";
    public static final String PREFIX_CONSUMER = "ftgo.consumer";
    public static final String PREFIX_RESTAURANT = "ftgo.restaurant";
    public static final String PREFIX_COURIER = "ftgo.courier";
}
