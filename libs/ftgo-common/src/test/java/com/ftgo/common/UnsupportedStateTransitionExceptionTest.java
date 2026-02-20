package com.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class UnsupportedStateTransitionExceptionTest {

    private enum TestState { ACTIVE, INACTIVE }

    @Test
    void shouldContainStateInMessage() {
        UnsupportedStateTransitionException exception =
                new UnsupportedStateTransitionException(TestState.ACTIVE);
        assertEquals("current state: ACTIVE", exception.getMessage());
    }

    @Test
    void shouldBeRuntimeException() {
        UnsupportedStateTransitionException exception =
                new UnsupportedStateTransitionException(TestState.INACTIVE);
        assertInstanceOf(RuntimeException.class, exception);
    }
}
