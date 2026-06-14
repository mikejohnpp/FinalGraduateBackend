package org.social.userservice.services;

import org.social.common.dto.search.SearchResultDTO;
import org.social.common.dto.user.request.ProfileUpdateRequest;
import org.social.common.dto.user.views.UserProfileDTO;
import org.social.common.dto.admin.UserAdminDTO;
import org.social.common.dto.admin.requests.AdminUserCreateRequest;
import org.social.common.dto.admin.requests.AdminUserUpdateRequest;
import org.social.common.dto.PageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileDTO getUserProfile(long id, String requestingEmail);
    UserProfileDTO updateProfile(long id, ProfileUpdateRequest request);
    String uploadAvatar(long id, MultipartFile file);
    String uploadCover(long id, MultipartFile file);
    SearchResultDTO search(String q);

    // Admin methods
    PageResponse<UserAdminDTO> getAllUsers(int page, int size, String search);
    UserAdminDTO createUserAdmin(AdminUserCreateRequest request);
    UserAdminDTO updateUserAdmin(long id, AdminUserUpdateRequest request);
    void deleteUserAdmin(long id);
}
