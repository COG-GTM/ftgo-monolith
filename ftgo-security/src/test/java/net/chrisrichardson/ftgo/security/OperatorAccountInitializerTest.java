package net.chrisrichardson.ftgo.security;

import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class OperatorAccountInitializerTest {

  @Test
  public void concurrentBootstrapInsertDoesNotAbortStartup() {
    UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    String username = "operator";
    String password = "operator-password";
    when(userAccountRepository.findByUsername(username)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(password)).thenReturn("encoded-password");
    when(userAccountRepository.save(any(UserAccount.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate username"));
    OperatorAccountInitializer initializer = new OperatorAccountInitializer(
            userAccountRepository, passwordEncoder, username, password);

    initializer.run(null);

    verify(userAccountRepository).findByUsername(username);
    verify(userAccountRepository).save(any(UserAccount.class));
  }
}
