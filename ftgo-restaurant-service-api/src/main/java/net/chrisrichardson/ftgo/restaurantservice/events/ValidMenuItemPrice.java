package net.chrisrichardson.ftgo.restaurantservice.events;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MenuItemPriceValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMenuItemPrice {
  String message() default "must be non-negative and fit decimal(19,2)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
