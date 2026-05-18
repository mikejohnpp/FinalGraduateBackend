package org.social.userservice.services.impl;

import org.social.common.dto.user.mappers.UserMapper;
import org.social.common.dto.user.views.UserNoAuthenticateDTO;
import org.social.common.dto.user.views.UserWithAuthenticateDTO;
import org.social.common.repositories.UserRepository;
import org.social.common.dto.user.views.RoleDTO;
import org.social.common.dto.user.views.UserDTO;
import org.social.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UserDTO> getAll() {
        var u = userRepository.findAll()
                .stream()
                .map(UserMapper::mapUserToUserDTO)
                .toList();
        return u;
    }

    @Override
    public UserWithAuthenticateDTO getUserWithAuthenticate(long id) {
        var user = userRepository.findById(id);
        return UserMapper.mapUserToUserWithAuthenticate(user.get());
    }

    @Override
    public List<UserWithAuthenticateDTO> getListUserWithAuthenticate() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapUserToUserWithAuthenticate)
                .toList();
    }
}
