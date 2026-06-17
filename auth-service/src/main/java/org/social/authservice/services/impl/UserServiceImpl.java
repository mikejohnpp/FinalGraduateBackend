package org.social.authservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.authservice.services.EmailService;
import org.social.authservice.services.UserService;
import org.social.common.dto.RegisterRequest;
import org.social.common.entities.Role;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.repositories.RoleRepository;
import org.social.common.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED).withData(Map.of(
                    "confirmPassword", "Mật khẩu xác nhận không khớp!"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED).withData(Map.of(
                    "email", "Email đã được sử dụng!"));
        }

        String maKichHoat = UUID.randomUUID().toString();
        LocalDateTime thoiGianHetHan = LocalDateTime.now().plusHours(24);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserName(request.getUserName());
        user.setIsActive(false);
        user.setActive(true);
        user.setActiveCode(maKichHoat);
        user.setExpireDate(thoiGianHetHan);

        Optional<Role> roleUser = roleRepository.findByName("ROLE_USER");
        roleUser.ifPresent(user::setRole);
        userRepository.save(user);

        emailService.guiEmailKichHoat(user.getEmail(), maKichHoat);
    }

    @Override
    public boolean kichHoatTaiKhoan(String maKichHoat) {
        Optional<User> optUser = userRepository.findByActiveCode(maKichHoat);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();

        if (user.getExpireDate() == null
                || LocalDateTime.now().isAfter(user.getExpireDate())) {
            return false;
        }

        user.setIsActive(true);
        user.setActiveCode(null);
        user.setExpireDate(null);
        userRepository.save(user);
        return true;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        if (!user.getIsActive()) {
            throw new BusinessException("Tài khoản chưa được kích hoạt");
        }

        if (!user.getActive()) {
            throw new BusinessException("Tài khoản đã bị khóa");
        }

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().getName()));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }
}
