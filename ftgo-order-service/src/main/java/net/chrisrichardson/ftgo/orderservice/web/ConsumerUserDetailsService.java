package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class ConsumerUserDetailsService implements UserDetailsService {

  private final ConsumerRepository consumerRepository;

  public ConsumerUserDetailsService(ConsumerRepository consumerRepository) {
    this.consumerRepository = consumerRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    long consumerId;
    try {
      consumerId = Long.parseLong(username);
    } catch (NumberFormatException e) {
      throw new UsernameNotFoundException("Unknown consumer");
    }
    Optional<Consumer> consumer = consumerRepository.findById(consumerId);
    return consumer
            .filter(c -> c.getPasswordHash() != null)
            .map(c -> new ConsumerUserDetails(c.getId(), c.getPasswordHash()))
            .orElseThrow(() -> new UsernameNotFoundException("Unknown consumer"));
  }
}
