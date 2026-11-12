package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final ConsumerRepository consumerRepository;
  private final RestaurantRepository restaurantRepository;
  private final CourierRepository courierRepository;

  public AccountController(UserAccountRepository userAccountRepository,
                           PasswordEncoder passwordEncoder,
                           ConsumerRepository consumerRepository,
                           RestaurantRepository restaurantRepository,
                           CourierRepository courierRepository) {
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = passwordEncoder;
    this.consumerRepository = consumerRepository;
    this.restaurantRepository = restaurantRepository;
    this.courierRepository = courierRepository;
  }

  @PostMapping
  public CreateAccountResponse create(@AuthenticationPrincipal FtgoUser caller,
                                      @RequestBody CreateAccountRequest request) {
    if (caller == null || !caller.isOperator()) {
      throw new AccessDeniedException("Only operators can create accounts");
    }
    if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
      throw new IllegalArgumentException("Username must not be blank");
    }
    if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
      throw new IllegalArgumentException("Password must not be blank");
    }
    if (request.getPassword().length() < 8) {
      throw new IllegalArgumentException("Password must be at least 8 characters");
    }
    if (request.getRole() == null) {
      throw new IllegalArgumentException("Role is required");
    }
    validateEntity(request.getRole(), request.getEntityId());
    if (userAccountRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new UsernameTakenException(request.getUsername());
    }

    UserAccount account = userAccountRepository.save(new UserAccount(
            request.getUsername(), passwordEncoder.encode(request.getPassword()),
            request.getRole(), request.getEntityId()));
    return new CreateAccountResponse(account.getId(), account.getUsername(), account.getRole(), account.getEntityId());
  }

  private void validateEntity(Role role, Long entityId) {
    if (role == Role.OPERATOR) {
      if (entityId != null) {
        throw new IllegalArgumentException("Operator accounts must not have an entity id");
      }
      return;
    }
    if (entityId == null) {
      throw new IllegalArgumentException("Entity id is required for " + role);
    }

    boolean exists;
    switch (role) {
      case CONSUMER:
        exists = consumerRepository.existsById(entityId);
        break;
      case RESTAURANT:
        exists = restaurantRepository.existsById(entityId);
        break;
      case COURIER:
        exists = courierRepository.existsById(entityId);
        break;
      default:
        throw new IllegalArgumentException("Unsupported role: " + role);
    }
    if (!exists) {
      throw new IllegalArgumentException("Entity does not exist for " + role + ": " + entityId);
    }
  }
}
