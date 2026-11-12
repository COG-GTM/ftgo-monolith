package net.chrisrichardson.ftgo.restaurantservice.events;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class UniqueMenuItemIdsValidator implements ConstraintValidator<UniqueMenuItemIds, RestaurantMenuDTO> {

  @Override
  public boolean isValid(RestaurantMenuDTO menu, ConstraintValidatorContext context) {
    if (menu == null || menu.getMenuItemDTOs() == null) {
      return true;
    }

    Set<String> ids = new HashSet<>();
    for (MenuItemDTO item : menu.getMenuItemDTOs()) {
      if (item != null && !ids.add(item.getId())) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("menuItemDTOs")
                .addConstraintViolation();
        return false;
      }
    }
    return true;
  }
}
