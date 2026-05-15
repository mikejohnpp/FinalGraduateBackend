package org.social.apigateway.services;

import org.social.dto.RegisterRequest;
import org.social.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    /**
     * Đăng ký tài khoản mới.
     * Trả về true nếu thành công, ném exception nếu email đã tồn tại.
     */
    void register(RegisterRequest request);

    /**
     * Kích hoạt tài khoản qua mã xác nhận gửi qua email.
     */
    boolean kichHoatTaiKhoan(String maKichHoat);

    Optional<User> findByEmail(String email);
}
