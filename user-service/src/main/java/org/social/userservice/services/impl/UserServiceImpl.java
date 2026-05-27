package org.social.userservice.services.impl;

import org.social.common.dto.user.mappers.UserMapper;
import org.social.common.dto.user.views.UserProfileDTO;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserProfileDTO getUserProfile(long id, String requestingEmail) {
        User user = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));

        if (!user.getEmail().equals(requestingEmail)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return UserMapper.mapUserToProfile(user);
    }
}

