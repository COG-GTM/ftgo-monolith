package net.chrisrichardson.ftgo.common.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class FtgoUserDetailsService implements UserDetailsService {

  private final AppUserRepository appUserRepository;

  public FtgoUserDetailsService(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    return appUserRepository.findByUsername(username)
            .map(FtgoUserDetails::of)
            .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));
  }
}
