package com.ftgo.common.error.exception;

import com.ftgo.common.error.code.CommonErrorCode;
import com.ftgo.common.error.code.ErrorCode;

/**
 * Exception thrown when an invalid state transition is attempted.
 *
 * <p>Maps to HTTP 409 Conflict. This is the typed replacement for
 * the legacy {@code UnsupportedStateTransitionException}.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * throw new StateTransitionException(OrderState.APPROVED);
 * throw new StateTransitionException(OrderErrorCode.ORDER_STATE_INVALID,
 *     "Cannot cancel an order that is already delivered");
 * </pre>
 */
public class StateTransitionException extends FtgoException {

    private final Enum<?> currentState;

    public StateTransitionException(Enum<?> currentState) {
        super(CommonErrorCode.STATE_TRANSITION_ERROR,
                String.format("Invalid state transition from current state: %s", currentState));
        this.currentState = currentState;
    }

    public StateTransitionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.currentState = null;
    }

    public StateTransitionException(String message) {
        super(CommonErrorCode.STATE_TRANSITION_ERROR, message);
        this.currentState = null;
    }

    public Enum<?> getCurrentState() {
        return currentState;
    }
}
