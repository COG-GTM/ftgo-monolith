package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AccountControllerTest {

  @Test
  public void createMapsConcurrentDuplicateUsernameToUsernameTakenException() {
    UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    AccountController controller = new AccountController(userAccountRepository, passwordEncoder,
            mock(ConsumerRepository.class), mock(RestaurantRepository.class), mock(CourierRepository.class));
    String username = "concurrent-operator";
    String password = "long-enough-password";
    when(userAccountRepository.findByUsername(username)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(password)).thenReturn("encoded-password");
    when(userAccountRepository.save(any(UserAccount.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate username"));

    assertThatThrownBy(() -> controller.create(
            new FtgoUser("operator", "encoded-operator-password", Role.OPERATOR, null),
            new CreateAccountRequest(username, password, Role.OPERATOR, null)))
            .isInstanceOf(UsernameTakenException.class)
            .hasMessage("Username is already taken: " + username);

    verify(userAccountRepository).findByUsername(username);
    verify(userAccountRepository).save(any(UserAccount.class));
  }
}
