package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierCredentials;
import net.chrisrichardson.ftgo.courierservice.domain.CourierCredentialsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class FtgoUserDetailsService implements UserDetailsService {

  private final CourierCredentialsRepository courierCredentialsRepository;
  private final String operatorUsername;
  private final String operatorPasswordHash;

  public FtgoUserDetailsService(CourierCredentialsRepository courierCredentialsRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${ftgo.security.operator.username:operator}") String operatorUsername,
                                @Value("${ftgo.security.operator.password:}") String operatorPassword) {
    this.courierCredentialsRepository = courierCredentialsRepository;
    this.operatorUsername = operatorUsername;
    this.operatorPasswordHash = operatorPassword == null || operatorPassword.trim().isEmpty()
            ? null : passwordEncoder.encode(operatorPassword);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (operatorUsername.equalsIgnoreCase(username)) {
      if (operatorPasswordHash == null) {
        throw new UsernameNotFoundException(username);
      }
      return new CourierUser(operatorUsername, operatorPasswordHash,
              AuthorityUtils.createAuthorityList("ROLE_OPERATOR"), null);
    }

    CourierCredentials credentials = courierCredentialsRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    return new CourierUser(credentials.getUsername(), credentials.getPasswordHash(),
            AuthorityUtils.createAuthorityList("ROLE_COURIER"), credentials.getCourierId());
  }
}
