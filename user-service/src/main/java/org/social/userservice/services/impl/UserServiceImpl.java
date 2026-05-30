package org.social.userservice.services.impl;

import org.social.common.dto.user.mappers.UserMapper;
import org.social.common.dto.user.request.ProfileUpdateRequest;
import org.social.common.dto.user.views.UserProfileDTO;
import org.social.common.entities.FriendStatus;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.social.common.repositories.UserFriendRepository;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFriendRepository userFriendRepository;

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

        if (request.bio() != null) user.setBio(request.bio());
        if (request.location() != null) user.setLocation(request.location());
        if (request.education() != null) user.setEducation(request.education());
        if (request.workplace() != null) user.setWorkplace(request.workplace());
        if (request.hometown() != null) user.setHometown(request.hometown());
        if (request.dateOfBirth() != null) {
            try {
                user.setDateOfBirth(LocalDate.parse(request.dateOfBirth()));
            } catch (Exception ignored) {}
        }
        if (request.relationship() != null) user.setRelationship(request.relationship());
        if (request.gender() != null) user.setGender(request.gender());
        if (request.pronouns() != null) user.setPronouns(request.pronouns());
        if (request.language() != null) user.setLanguage(request.language());

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
}

