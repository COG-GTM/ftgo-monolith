package net.chrisrichardson.ftgo.endtoendtests;

import net.chrisrichardson.ftgo.common.security.AuthTokenService;
import net.chrisrichardson.ftgo.endtoendtests.common.AbstractEndToEndTests;

public class EndToEndTests extends AbstractEndToEndTests  {

  private String host = System.getenv("DOCKER_HOST_IP");
  private int applicationPort = 8081;
  private AuthTokenService authTokenService = createAuthTokenService();

  private AuthTokenService createAuthTokenService() {
    String secret = System.getenv("FTGO_AUTH_TOKEN_SECRET");
    if (secret == null || secret.trim().isEmpty()) {
      throw new IllegalStateException("FTGO_AUTH_TOKEN_SECRET must be set to run end-to-end tests");
    }
    return AuthTokenService.fromSecret(secret);
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getApplicationPort() {
    return applicationPort;
  }

  @Override
  protected AuthTokenService getAuthTokenService() {
    return authTokenService;
  }
}
