package net.chrisrichardson.ftgo.common.security;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppUserRepository extends CrudRepository<AppUser, Long> {

  Optional<AppUser> findByUsername(String username);
}
