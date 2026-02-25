package com.ftgo.security.authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that documents the required role(s) for a method.
 * <p>
 * This is a <strong>documentation-only</strong> annotation to complement
 * the standard {@code @PreAuthorize} annotations. It provides a readable
 * way to express which FTGO roles are required for each service method.
 * </p>
 *
 * <h3>Usage</h3>
 * <pre>
 * &#64;RequireRole(FtgoRole.CUSTOMER)
 * &#64;PreAuthorize("hasRole('CUSTOMER')")
 * public Order createOrder(Long consumerId, ...) { }
 *
 * &#64;RequireRole({FtgoRole.COURIER, FtgoRole.ADMIN})
 * &#64;PreAuthorize("hasAnyRole('COURIER', 'ADMIN')")
 * public void updateDeliveryStatus(...) { }
 * </pre>
 *
 * @see FtgoRole
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * The role(s) required to access the annotated method or class.
     */
    FtgoRole[] value();
}
