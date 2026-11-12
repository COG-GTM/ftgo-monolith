package net.chrisrichardson.ftgo.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FtgoUserDetailsService implements UserDetailsService {

  private final UserAccountRepository userAccountRepository;

  public FtgoUserDetailsService(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserAccount account = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    return new FtgoUser(account.getUsername(), account.getPasswordHash(), account.getRole(), account.getEntityId());
  }
}
