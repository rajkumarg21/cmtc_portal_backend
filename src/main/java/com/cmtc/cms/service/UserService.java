package com.cmtc.cms.service;

import com.cmtc.cms.dto.user.UserCreateRequest;
import com.cmtc.cms.dto.user.UserResponse;
import com.cmtc.cms.dto.user.UserUpdateRequest;

import jakarta.validation.Valid;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void toggleUserStatus(Long id, boolean enabled);
    void deleteUser(Long id);
    void createPasswordResetTokenForUser(String email);
    void resetPassword(String token, String newPassword);
	UserResponse registerVisitor(@Valid UserCreateRequest request);
}
