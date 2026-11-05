package net.chrisrichardson.ftgo.common.security;

public final class AuthenticatedConsumer {

  private final long consumerId;

  public AuthenticatedConsumer(long consumerId) {
    this.consumerId = consumerId;
  }

  public long getConsumerId() {
    return consumerId;
  }
}
