package org.social.apigateway.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.apigateway.services.EmailService;
import org.social.apigateway.services.UserService;
import org.social.common.dto.RegisterRequest;
import org.social.common.entities.Role;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
import org.social.common.repositories.RoleRepository;
import org.social.common.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
        // Kiểm tra 2 mật khẩu có khớp không
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp!");
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã được sử dụng!");
        }

        // Tạo mã kích hoạt ngẫu nhiên + đặt thời hạn 24h
        String maKichHoat = UUID.randomUUID().toString();
        LocalDateTime thoiGianHetHan = LocalDateTime.now().plusHours(24);

        // Tạo User mới (thông tin chi tiết sẽ cập nhật sau ở phần chỉnh sửa hồ sơ)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserName(" ");
        user.setIsActive(false); // Chưa kích hoạt
        user.setActive(true);
        user.setActiveCode(maKichHoat);
        user.setExpireDate(thoiGianHetHan);

        // Gán quyền USER mặc định (maQuyen = 2, hoặc tìm theo tên)
        Optional<Role> roleUser = roleRepository.findByName("ROLE_USER");
//        roleUser.ifPresent(role -> user.setRole(List.of(role)));

        roleUser.ifPresent(user::setRole);
        userRepository.save(user);

        // Gửi email kích hoạt
        emailService.guiEmailKichHoat(user.getEmail(), maKichHoat);
    }

    @Override
    public boolean kichHoatTaiKhoan(String maKichHoat) {
        Optional<User> optUser = userRepository.findByActiveCode(maKichHoat);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();

        // Kiểm tra mã kích hoạt có còn trong thời hạn không
        if (user.getExpireDate() == null
                || LocalDateTime.now().isAfter(user.getExpireDate())) {
            return false; // Mã đã hết hạn
        }

        user.setIsActive(true);
        user.setActiveCode(null); // Xóa mã sau khi kích hoạt
        user.setExpireDate(null); // Xóa thời hạn
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

//        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority(role.getTenQuyen()))
//                .toList();


        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().getName())
        );
        // Này là UserDetail của security nha
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }
}
