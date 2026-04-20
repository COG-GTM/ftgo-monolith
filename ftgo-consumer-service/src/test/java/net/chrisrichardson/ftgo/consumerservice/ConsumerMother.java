package net.chrisrichardson.ftgo.consumerservice;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;

public class ConsumerMother {

  public static final String FIRST_NAME = "John";
  public static final String LAST_NAME = "Doe";
  public static final PersonName CONSUMER_NAME = new PersonName(FIRST_NAME, LAST_NAME);

  public static Consumer makeConsumer() {
    return new Consumer(CONSUMER_NAME);
  }
}
