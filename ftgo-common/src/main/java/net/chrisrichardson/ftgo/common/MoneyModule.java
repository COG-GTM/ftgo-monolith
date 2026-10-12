package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import java.io.IOException;
import java.math.BigDecimal;

public class MoneyModule extends SimpleModule {

  // Matches the decimal(19, 2) money columns.
  static final int MAX_INTEGER_DIGITS = 17;
  static final int MAX_SCALE = 2;
  static final int MAX_LENGTH = 32;

  class MoneyDeserializer extends StdScalarDeserializer<Money> {

    protected MoneyDeserializer() {
      super(Money.class);
    }

    @Override
    public Money deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken token = jp.getCurrentToken();
      if (token == JsonToken.VALUE_STRING) {
        String str = jp.getText().trim();
        if (str.isEmpty())
          return null;
        else
          return new Money(parseBoundedAmount(str, ctxt));
      } else
        throw ctxt.mappingException(getValueClass());
    }
  }

  static BigDecimal parseBoundedAmount(String str, DeserializationContext ctxt) throws IOException {
    if (str.length() > MAX_LENGTH)
      throw ctxt.weirdStringException(str, Money.class, "amount is too long");
    BigDecimal amount;
    try {
      amount = new BigDecimal(str);
    } catch (NumberFormatException e) {
      throw ctxt.weirdStringException(str, Money.class, "not a valid decimal amount");
    }
    if (amount.scale() > MAX_SCALE || amount.precision() - amount.scale() > MAX_INTEGER_DIGITS)
      throw ctxt.weirdStringException(str, Money.class, "amount is out of range");
    return amount;
  }

  class MoneySerializer extends StdScalarSerializer<Money> {
    public MoneySerializer() {
      super(Money.class);
    }

    @Override
    public void serialize(Money value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeString(value.asString());
    }
  }

    @Override
  public String getModuleName() {
    return "FtgoCommonMOdule";
  }

  public MoneyModule() {
    addDeserializer(Money.class, new MoneyDeserializer());
    addSerializer(Money.class, new MoneySerializer());
  }

}
