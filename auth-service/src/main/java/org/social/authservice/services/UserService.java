package org.social.authservice.services;

import org.social.common.dto.RegisterRequest;
import org.social.common.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    Optional<User> findByEmail(String email);
}
