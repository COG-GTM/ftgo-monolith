package com.ftgo.common.error.exception;

import com.ftgo.common.error.code.CommonErrorCode;
import com.ftgo.common.error.code.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the FTGO exception hierarchy.
 */
class ExceptionHierarchyTest {

    @Test
    @DisplayName("ResourceNotFoundException carries correct error code and HTTP status")
    void resourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order", 42L);

        assertThat(ex.getErrorCode()).isEqualTo(CommonErrorCode.RESOURCE_NOT_FOUND);
        assertThat(ex.getHttpStatus()).isEqualTo(404);
        assertThat(ex.getMessage()).isEqualTo("Order with id '42' was not found");
        assertThat(ex.getResourceType()).isEqualTo("Order");
        assertThat(ex.getResourceId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("ResourceNotFoundException with custom error code")
    void resourceNotFoundWithCustomCode() {
        ResourceNotFoundException ex = new ResourceNotFoundException(
                OrderErrorCode.ORDER_NOT_FOUND, "Order 42 not found");

        assertThat(ex.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        assertThat(ex.getHttpStatus()).isEqualTo(404);
        assertThat(ex.getMessage()).isEqualTo("Order 42 not found");
    }

    @Test
    @DisplayName("BusinessRuleException carries correct error code and HTTP status")
    void businessRuleException() {
        BusinessRuleException ex = new BusinessRuleException(
                OrderErrorCode.ORDER_MINIMUM_NOT_MET,
                "Order total must be at least $10.00");

        assertThat(ex.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_MINIMUM_NOT_MET);
        assertThat(ex.getHttpStatus()).isEqualTo(422);
        assertThat(ex.getMessage()).isEqualTo("Order total must be at least $10.00");
    }

    @Test
    @DisplayName("BusinessRuleException with default error code")
    void businessRuleExceptionDefault() {
        BusinessRuleException ex = new BusinessRuleException("Some rule violated");

        assertThat(ex.getErrorCode()).isEqualTo(CommonErrorCode.BUSINESS_RULE_VIOLATION);
        assertThat(ex.getHttpStatus()).isEqualTo(422);
    }

    @Test
    @DisplayName("StateTransitionException carries current state")
    void stateTransitionException() {
        StateTransitionException ex = new StateTransitionException(TestState.DELIVERED);

        assertThat(ex.getErrorCode()).isEqualTo(CommonErrorCode.STATE_TRANSITION_ERROR);
        assertThat(ex.getHttpStatus()).isEqualTo(409);
        assertThat(ex.getCurrentState()).isEqualTo(TestState.DELIVERED);
        assertThat(ex.getMessage()).contains("DELIVERED");
    }

    @Test
    @DisplayName("ServiceCommunicationException includes service name")
    void serviceCommunicationException() {
        RuntimeException cause = new RuntimeException("Connection refused");
        ServiceCommunicationException ex = new ServiceCommunicationException("restaurant-service", cause);

        assertThat(ex.getErrorCode()).isEqualTo(CommonErrorCode.UPSTREAM_SERVICE_ERROR);
        assertThat(ex.getHttpStatus()).isEqualTo(502);
        assertThat(ex.getServiceName()).isEqualTo("restaurant-service");
        assertThat(ex.getMessage()).contains("restaurant-service");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("ServiceTimeoutException includes service name")
    void serviceTimeoutException() {
        ServiceTimeoutException ex = new ServiceTimeoutException("courier-service");

        assertThat(ex.getErrorCode()).isEqualTo(CommonErrorCode.SERVICE_TIMEOUT);
        assertThat(ex.getHttpStatus()).isEqualTo(504);
        assertThat(ex.getServiceName()).isEqualTo("courier-service");
        assertThat(ex.getMessage()).contains("courier-service");
    }

    @Test
    @DisplayName("All custom exceptions extend FtgoException")
    void allExtendFtgoException() {
        assertThat(new ResourceNotFoundException("X", 1L)).isInstanceOf(FtgoException.class);
        assertThat(new BusinessRuleException("test")).isInstanceOf(FtgoException.class);
        assertThat(new StateTransitionException(TestState.APPROVED)).isInstanceOf(FtgoException.class);
        assertThat(new ServiceCommunicationException("svc", "err")).isInstanceOf(FtgoException.class);
        assertThat(new ServiceTimeoutException("svc")).isInstanceOf(FtgoException.class);
    }

    @Test
    @DisplayName("All custom exceptions extend RuntimeException")
    void allExtendRuntimeException() {
        assertThat(new ResourceNotFoundException("X", 1L)).isInstanceOf(RuntimeException.class);
        assertThat(new BusinessRuleException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new StateTransitionException(TestState.APPROVED)).isInstanceOf(RuntimeException.class);
    }

    enum TestState {
        APPROVED, DELIVERED
    }
}
