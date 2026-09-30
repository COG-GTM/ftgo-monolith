package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import java.util.Optional;

@Transactional
public class ConsumerService {

  @Autowired
  private ConsumerRepository consumerRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    Optional<Consumer> consumer = consumerRepository.findById(consumerId);
    consumer.orElseThrow(ConsumerNotFoundException::new).validateOrderByConsumer(orderTotal);
  }

  public ConsumerRegistration create(PersonName name) {
    String password = Base64.getUrlEncoder().withoutPadding().encodeToString(KeyGenerators.secureRandom(32).generateKey());
    Consumer consumer = consumerRepository.save(new Consumer(name, passwordEncoder.encode(password)));
    return new ConsumerRegistration(consumer, password);
  }

  public Optional<Consumer> findById(long consumerId) {
    return consumerRepository.findById(consumerId);
  }
}
