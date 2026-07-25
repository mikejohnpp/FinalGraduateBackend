package org.social.userservice.services.impl;

import org.social.common.dto.search.SearchResultDTO;
import org.social.common.dto.user.mappers.UserMapper;
import org.social.common.dto.user.request.ProfileUpdateRequest;
import org.social.common.dto.user.views.UserProfileDTO;
import org.social.common.dto.admin.UserAdminDTO;
import org.social.common.dto.admin.requests.AdminUserCreateRequest;
import org.social.common.dto.admin.requests.AdminUserUpdateRequest;
import org.social.common.dto.PageResponse;
import org.social.common.entities.Role;
import org.social.common.entities.FriendStatus;
import org.social.common.entities.Group;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.social.common.repositories.GroupRepository;
import org.social.common.repositories.RoleRepository;
import org.social.common.repositories.UserFriendRepository;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFriendRepository userFriendRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserProfileDTO getUserProfile(long id, String requestingEmail) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));

        int friendCount = userFriendRepository.countByIdUserIdAndStatus((int) id, FriendStatus.ACCEPTED);

        boolean isOwner = user.getEmail().equals(requestingEmail);
        if (!isOwner) {
            user.setEmail(null);
            user.setPhoneNumber(null);
        }

        return UserMapper.mapUserToProfile(user, friendCount);
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(long id, ProfileUpdateRequest request) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));

        if (request.userName() != null)
            user.setUserName(request.userName());
        if (request.nickName() != null)
            user.setNickName(request.nickName());
        if (request.bio() != null)
            user.setBio(request.bio());

        if (request.location() != null)
            user.setLocation(request.location());
        if (request.education() != null)
            user.setEducation(request.education());
        if (request.workplace() != null)
            user.setWorkplace(request.workplace());
        if (request.hometown() != null)
            user.setHometown(request.hometown());
        if (request.dateOfBirth() != null) {
            try {
                user.setDateOfBirth(LocalDate.parse(request.dateOfBirth()));
            } catch (Exception ignored) {
            }
        }
        if (request.relationship() != null)
            user.setRelationship(request.relationship());
        if (request.gender() != null)
            user.setGender(request.gender());
        if (request.pronouns() != null)
            user.setPronouns(request.pronouns());
        if (request.language() != null)
            user.setLanguage(request.language());
        if (request.avatar() != null)
            user.setAvatar(request.avatar());
        if (request.coverPhoto() != null)
            user.setCoverPhoto(request.coverPhoto());

        userRepository.save(user);

        int friendCount = userFriendRepository.countByIdUserIdAndStatus((int) id, FriendStatus.ACCEPTED);
        return UserMapper.mapUserToProfile(user, friendCount);
    }

    @Override
    @Transactional
    public String uploadAvatar(long id, MultipartFile file) {
        return uploadFileAndSetField(id, file, "avatars", true);
    }

    @Override
    @Transactional
    public String uploadCover(long id, MultipartFile file) {
        return uploadFileAndSetField(id, file, "covers", false);
    }

    private String uploadFileAndSetField(long id, MultipartFile file, String subDir, boolean isAvatar) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));

        try {
            Path uploadDir = Paths.get("uploads", subDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID() + extension;
            Path filePath = uploadDir.resolve(newFilename);

            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/uploads/" + subDir + "/" + newFilename;

            if (isAvatar) {
                user.setAvatar(fileUrl);
            } else {
                user.setCoverPhoto(fileUrl);
            }
            userRepository.save(user);
            return fileUrl;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Lỗi khi upload file");
        }
    }

    @Override
    public SearchResultDTO search(String q) {
        String query = (q == null || q.isBlank()) ? "" : q.trim();
        PageRequest limit = PageRequest.of(0, 8);

        List<User> users = userRepository.findByIsActiveTrueAndUserNameContainingIgnoreCase(query, limit);
        List<Group> groups = groupRepository.findByIsActiveTrueAndNameContainingIgnoreCase(query, limit);

        List<SearchResultDTO.UserSearchDTO> userDTOs = users.stream()
                .map(u -> new SearchResultDTO.UserSearchDTO(u.getId().longValue(), u.getUserName(), u.getNickName(),
                        u.getAvatar()))
                .toList();

        List<SearchResultDTO.GroupSearchDTO> groupDTOs = groups.stream()
                .map(g -> new SearchResultDTO.GroupSearchDTO(g.getId(), g.getName(), g.getAvatar(), 0))
                .toList();

        return new SearchResultDTO(userDTOs, groupDTOs);
    }

    // --- Admin Methods ---

    @Override
    public PageResponse<UserAdminDTO> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;

        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.findAll((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("userName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserAdminDTO> dtoList = userPage.getContent().stream().map(u -> {
            UserAdminDTO dto = new UserAdminDTO();
            dto.setId(u.getId());
            dto.setUserName(u.getUserName());
            dto.setEmail(u.getEmail());
            dto.setNickName(u.getNickName());
            dto.setPhoneNumber(u.getPhoneNumber());
            dto.setGender(u.getGender());
            dto.setDateOfBirth(u.getDateOfBirth());
            dto.setIsActive(u.getIsActive());
            dto.setRoleName(u.getRole() != null ? u.getRole().getName() : null);
            return dto;
        }).toList();

        return new PageResponse<>(dtoList, userPage.getNumber(), userPage.getSize(),
                userPage.getTotalElements(), userPage.getTotalPages(),
                userPage.hasNext(), userPage.hasPrevious());
    }

    @Override
    @Transactional
    public UserAdminDTO createUserAdmin(AdminUserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Email đã tồn tại");
        }
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Username đã tồn tại");
        }

        Role role = roleRepository.findById(request.getRoleId().longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.getRoleId()));

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPassword(user.getPasswordHash()); // maintain consistency if needed
        user.setRole(role);
        user.setNickName(request.getNickName());
        user.setIsActive(true);
        user.setActive(true);
        user.setIsDelete(false);

        User saved = userRepository.save(user);

        UserAdminDTO dto = new UserAdminDTO();
        dto.setId(saved.getId());
        dto.setUserName(saved.getUserName());
        dto.setEmail(saved.getEmail());
        dto.setNickName(saved.getNickName());
        dto.setIsActive(saved.getIsActive());
        dto.setRoleName(saved.getRole().getName());
        return dto;
    }

    @Override
    @Transactional
    public UserAdminDTO updateUserAdmin(long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Email đã tồn tại");
        }
        if (!user.getUserName().equals(request.getUserName())
                && userRepository.existsByUserName(request.getUserName())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Username đã tồn tại");
        }

        Role role = roleRepository.findById(request.getRoleId().longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.getRoleId()));

        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setNickName(request.getNickName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User saved = userRepository.save(user);

        UserAdminDTO dto = new UserAdminDTO();
        dto.setId(saved.getId());
        dto.setUserName(saved.getUserName());
        dto.setEmail(saved.getEmail());
        dto.setNickName(saved.getNickName());
        dto.setPhoneNumber(saved.getPhoneNumber());
        dto.setGender(saved.getGender());
        dto.setDateOfBirth(saved.getDateOfBirth());
        dto.setIsActive(saved.getIsActive());
        dto.setRoleName(saved.getRole().getName());
        return dto;
    }

    @Override
    @Transactional
    public void deleteUserAdmin(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setIsActive(false);
        user.setIsDelete(true);
        userRepository.save(user);
    }
}
