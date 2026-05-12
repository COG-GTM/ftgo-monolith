package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class StandaloneConsumerService {

  @Autowired
  private ConsumerEntityRepository consumerRepository;

  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    Optional<ConsumerEntity> consumer = consumerRepository.findById(consumerId);
    consumer.orElseThrow(ConsumerNotFoundException::new).validateOrderByConsumer(orderTotal);
  }

  public ConsumerEntity create(PersonName name) {
    return consumerRepository.save(new ConsumerEntity(name));
  }

  public Optional<ConsumerEntity> findById(long consumerId) {
    return consumerRepository.findById(consumerId);
  }
}
