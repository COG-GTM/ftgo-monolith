package net.chrisrichardson.ftgo.common.security;

/**
 * The identity of a consumer that has been authenticated by the API layer.
 * Services must only ever be given an instance produced from a credential -
 * never one built from a request body or query parameter.
 */
public class AuthenticatedConsumer {

  private final long consumerId;

  public AuthenticatedConsumer(long consumerId) {
    this.consumerId = consumerId;
  }

  public long getConsumerId() {
    return consumerId;
  }
}
