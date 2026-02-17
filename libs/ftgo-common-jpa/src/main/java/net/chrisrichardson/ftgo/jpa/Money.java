package net.chrisrichardson.ftgo.jpa;

import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private BigDecimal amount;

    public Money() {
        this.amount = BigDecimal.ZERO;
    }

    public Money(BigDecimal amount) {
        this.amount = amount;
    }

    public Money(String amount) {
        this.amount = new BigDecimal(amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(new BigDecimal(quantity)));
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount != null ? amount.stripTrailingZeros().hashCode() : 0;
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
