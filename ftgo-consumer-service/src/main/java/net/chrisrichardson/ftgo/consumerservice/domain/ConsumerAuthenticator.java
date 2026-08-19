package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.security.AccessTokens;
import net.chrisrichardson.ftgo.common.security.AuthenticatedConsumer;
import net.chrisrichardson.ftgo.common.security.UnauthenticatedException;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the consumer identity of a request from its {@code Authorization: Bearer <token>}
 * header. The identity is never taken from the request body or query string.
 */
@Transactional(readOnly = true)
public class ConsumerAuthenticator {

  private final ConsumerRepository consumerRepository;

  public ConsumerAuthenticator(ConsumerRepository consumerRepository) {
    this.consumerRepository = consumerRepository;
  }

  public AuthenticatedConsumer authenticate(String authorizationHeader) {
    String token = AccessTokens.bearerToken(authorizationHeader);
    if (token == null)
      throw new UnauthenticatedException("A consumer access token is required");

    Consumer consumer = consumerRepository.findByAccessToken(token)
            .orElseThrow(() -> new UnauthenticatedException("Invalid consumer access token"));

    return new AuthenticatedConsumer(consumer.getId());
  }
}
