package net.chrisrichardson.ftgo.common;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class Money {

    public static Money ZERO = new Money(0);

    private BigDecimal amount;

    private Money() {
    }

    public Money(BigDecimal amount) {
        this.amount = amount;
    }

    public Money(String s) {
        this.amount = new BigDecimal(s);
    }

    public Money(int i) {
        this.amount = new BigDecimal(i);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return "Money{amount=" + amount + "}";
    }

    public Money add(Money delta) {
        return new Money(amount.add(delta.amount));
    }

    public Money subtract(Money delta) {
        return new Money(amount.subtract(delta.amount));
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return amount.compareTo(other.amount) >= 0;
    }

    public String asString() {
        return amount.toPlainString();
    }

    public Money multiply(int x) {
        return new Money(amount.multiply(new BigDecimal(x)));
    }
}
