package org.social.authservice.services;

import org.social.common.dto.RegisterRequest;
import org.social.common.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    void register(RegisterRequest request);

    boolean kichHoatTaiKhoan(String maKichHoat);

    Optional<User> findByEmail(String email);
}
