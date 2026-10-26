package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

/**
 * Resolves the consumer making a request from the API key presented in the
 * Authorization header. Requests without a valid key have no identity.
 */
@Transactional(readOnly = true)
public class ConsumerAuthenticator {

  private static final String BEARER_PREFIX = "Bearer ";

  @Autowired
  private ConsumerRepository consumerRepository;

  public Consumer authenticate(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      throw new ConsumerAuthenticationException("An API key is required");
    }
    String apiKey = header.substring(BEARER_PREFIX.length()).trim();
    if (apiKey.isEmpty()) {
      throw new ConsumerAuthenticationException("An API key is required");
    }
    return consumerRepository.findByApiKeyHash(ConsumerApiKeys.hash(apiKey))
            .orElseThrow(() -> new ConsumerAuthenticationException("Invalid API key"));
  }

  public long authenticatedConsumerId(HttpServletRequest request) {
    return authenticate(request).getId();
  }
}
