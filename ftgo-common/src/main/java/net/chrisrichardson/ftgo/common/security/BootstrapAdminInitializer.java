package net.chrisrichardson.ftgo.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the initial administrator account from configuration - typically supplied
 * as FTGO_SECURITY_BOOTSTRAP_ADMIN_USERNAME / FTGO_SECURITY_BOOTSTRAP_ADMIN_PASSWORD
 * environment variables. Nothing is created when they are not configured, so the
 * application never ships with a default account.
 */
@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(BootstrapAdminInitializer.class);

  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final String username;
  private final String password;

  public BootstrapAdminInitializer(AppUserRepository appUserRepository,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${ftgo.security.bootstrap-admin.username:}") String username,
                                   @Value("${ftgo.security.bootstrap-admin.password:}") String password) {
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;
    this.username = username;
    this.password = password;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (username.isEmpty() || password.isEmpty()) {
      logger.info("No bootstrap administrator configured; skipping account creation");
      return;
    }
    if (appUserRepository.findByUsername(username).isPresent()) {
      logger.info("Bootstrap administrator {} already exists", username);
      return;
    }
    appUserRepository.save(new AppUser(username, passwordEncoder.encode(password), UserRole.ADMIN, null));
    logger.info("Created bootstrap administrator {}", username);
  }
}
