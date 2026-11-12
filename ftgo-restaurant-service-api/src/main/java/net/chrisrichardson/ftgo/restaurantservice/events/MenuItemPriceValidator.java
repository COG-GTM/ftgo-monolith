package net.chrisrichardson.ftgo.restaurantservice.events;

import net.chrisrichardson.ftgo.common.Money;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class MenuItemPriceValidator implements ConstraintValidator<ValidMenuItemPrice, Money> {

  @Override
  public boolean isValid(Money money, ConstraintValidatorContext context) {
    if (money == null) {
      return true;
    }

    BigDecimal amount = new BigDecimal(money.asString()).stripTrailingZeros();
    int integerDigits = Math.max(0, amount.precision() - amount.scale());
    return amount.signum() >= 0 && amount.scale() <= 2 && integerDigits <= 17;
  }
}
