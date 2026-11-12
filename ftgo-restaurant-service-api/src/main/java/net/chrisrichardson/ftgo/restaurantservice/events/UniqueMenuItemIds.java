package net.chrisrichardson.ftgo.restaurantservice.events;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueMenuItemIdsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueMenuItemIds {
  String message() default "menu item ids must be unique";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
