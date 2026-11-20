package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public class ConsumerService {

  @Autowired
  private ConsumerRepository consumerRepository;

  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    Optional<Consumer> consumer = consumerRepository.findById(consumerId);
    consumer.orElseThrow(ConsumerNotFoundException::new).validateOrderByConsumer(orderTotal);
  }

  public Consumer create(PersonName name) {
    if (name == null) {
      throw new IllegalArgumentException("name is required");
    }
    if (name.getFirstName() == null || name.getFirstName().trim().isEmpty()) {
      throw new IllegalArgumentException("name.firstName must not be blank");
    }
    if (name.getLastName() == null || name.getLastName().trim().isEmpty()) {
      throw new IllegalArgumentException("name.lastName must not be blank");
    }
    Consumer consumer = consumerRepository.save(new Consumer(name));
    return consumer;
  }

  public Optional<Consumer> findById(long consumerId) {
    return consumerRepository.findById(consumerId);
  }
}
