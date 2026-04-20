package net.chrisrichardson.ftgo.orderservice.domain;

public enum OrderState {
    APPROVAL_PENDING,
    APPROVED,
    REJECTED,
    CANCEL_PENDING,
    CANCELLED,
    REVISION_PENDING
}
