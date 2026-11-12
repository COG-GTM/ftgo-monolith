package net.chrisrichardson.ftgo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class OperatorAccountInitializer implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(OperatorAccountInitializer.class);

  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final String username;
  private final String password;

  public OperatorAccountInitializer(UserAccountRepository userAccountRepository,
                                    PasswordEncoder passwordEncoder,
                                    @Value("${ftgo.security.operator.username:operator}") String username,
                                    @Value("${ftgo.security.operator.password:}") String password) {
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = passwordEncoder;
    this.username = username;
    this.password = password;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (password == null || password.trim().isEmpty()) {
      logger.warn("No operator account bootstrapped because ftgo.security.operator.password is blank");
      return;
    }
    if (userAccountRepository.findByUsername(username).isPresent()) {
      return;
    }
    userAccountRepository.save(new UserAccount(username, passwordEncoder.encode(password), Role.OPERATOR, null));
  }
}
