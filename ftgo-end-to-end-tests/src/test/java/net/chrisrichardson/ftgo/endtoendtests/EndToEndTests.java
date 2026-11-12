package net.chrisrichardson.ftgo.endtoendtests;

import net.chrisrichardson.ftgo.endtoendtests.common.AbstractEndToEndTests;

public class EndToEndTests extends AbstractEndToEndTests  {

  private String host = System.getenv("DOCKER_HOST_IP");
  private int applicationPort = 8081;
  private String operatorUsername = System.getenv("FTGO_OPERATOR_USERNAME") == null
          ? "operator" : System.getenv("FTGO_OPERATOR_USERNAME");
  private String operatorPassword = System.getenv("FTGO_OPERATOR_PASSWORD");

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getApplicationPort() {
    return applicationPort;
  }

  @Override
  public String getOperatorUsername() {
    return operatorUsername;
  }

  @Override
  public String getOperatorPassword() {
    return operatorPassword;
  }
}
