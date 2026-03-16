package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Thrown when an entity state transition is not allowed.
 *
 * <p>This is the centralized equivalent of
 * {@code net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException}.
 * Maps to HTTP 409 Conflict.
 */
public class UnsupportedStateTransitionException extends BaseException {

    public UnsupportedStateTransitionException(Enum<?> currentState) {
        super(ErrorCode.UNSUPPORTED_STATE_TRANSITION,
                "Unsupported state transition from current state: " + currentState);
    }

    public UnsupportedStateTransitionException(Enum<?> currentState, String targetAction) {
        super(ErrorCode.UNSUPPORTED_STATE_TRANSITION,
                "Cannot " + targetAction + " from state: " + currentState);
    }

    public UnsupportedStateTransitionException(String message) {
        super(ErrorCode.UNSUPPORTED_STATE_TRANSITION, message);
    }
}
