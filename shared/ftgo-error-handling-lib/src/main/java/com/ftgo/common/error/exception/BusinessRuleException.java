package com.ftgo.common.error.exception;

import com.ftgo.common.error.code.CommonErrorCode;
import com.ftgo.common.error.code.ErrorCode;

/**
 * Exception thrown when a business rule is violated.
 *
 * <p>Maps to HTTP 422 Unprocessable Entity.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * throw new BusinessRuleException(OrderErrorCode.ORDER_MINIMUM_NOT_MET,
 *     "Order total must be at least $10.00");
 * </pre>
 */
public class BusinessRuleException extends FtgoException {

    public BusinessRuleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessRuleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessRuleException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public BusinessRuleException(String message) {
        super(CommonErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
