package com.cmtc.cms.service.impl;

import com.cmtc.cms.dto.user.UserCreateRequest;
import com.cmtc.cms.dto.user.UserResponse;
import com.cmtc.cms.dto.user.UserUpdateRequest;
import com.cmtc.cms.exception.DuplicateFieldException;
import com.cmtc.cms.exception.ResourceNotFoundException;
import com.cmtc.cms.model.PasswordResetToken;

import com.cmtc.cms.model.User;

import com.cmtc.cms.repository.PasswordResetTokenRepository;

import com.cmtc.cms.repository.UserRepository;


import com.cmtc.cms.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // Username validation
        if (userRepository.existsByUsername(request.getUsername())) {
        	 throw new DuplicateFieldException("username "+ request.getUsername()+
        	            " already exists. Please choose another one.");
        }

        // Email validation
        if (userRepository.existsByEmail(request.getEmail())) {
        	
        	 throw new DuplicateFieldException("Email already exists. Please use a different one.");
           
        }

        // Mobile number validation
        if (userRepository.existsByMobileNo(request.getMobileNo())) {
            throw new  DuplicateFieldException("Mobile number already exists. Please use a different one.");
        }

     // Password validation
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty. Please enter a valid password.");
        }
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }


        // Build and save new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setActive(true);
        user.setEnabled(true);
        user.setMobileNo(request.getMobileNo());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }



    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDto(user);
    }
    
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!existingUser.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken.");
        }
        if (!existingUser.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already taken.");
        }

        existingUser.setUsername(request.getUsername());
        existingUser.setFullName(request.getFullName());
        existingUser.setEmail(request.getEmail());
        existingUser.setRole(request.getRole());
        // Password update is handled by resetPassword method or a dedicated change password endpoint

        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(false);  // ✅ Soft delete
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        System.out.println("  user : "+user.getId());
        passwordResetTokenRepository.deleteByUser(user);
System.out.println("  deleted   "+passwordResetTokenRepository.count());
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, token); // Adjust frontend URL
        System.out.println( " resetLink ::  "+resetLink);
//        notificationService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
//        emailService.sendPasswordResetLink(user.getEmail(),user.getFullName(),resetLink);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token."));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Password reset token has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    private UserResponse convertToDto(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMobileNo(user.getMobileNo());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        return dto;
    }
    
    // Always user Roll NORMAL_VISITOR
    public UserResponse registerVisitor(UserCreateRequest request) {
    	if (userRepository.existsByUsername(request.getUsername())) {
       	 throw new DuplicateFieldException("username "+ request.getUsername()+
       	            " already exists. Please choose another one.");
       }

       // Email validation
       if (userRepository.existsByEmail(request.getEmail())) {
       	
       	 throw new DuplicateFieldException("Email already exists. Please use a different one.");
          
       }

       // Mobile number validation
       if (userRepository.existsByMobileNo(request.getMobileNo())) {
           throw new  DuplicateFieldException("Mobile number already exists. Please use a different one.");
       }

    // Password validation
       if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
           throw new IllegalArgumentException("Password cannot be empty. Please enter a valid password.");
       }
       if (request.getPassword().length() < 6) {
           throw new IllegalArgumentException("Password must be at least 6 characters long.");
       }
         
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNo(request.getMobileNo());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setRole(request.getRole());
        user.setEnabled(true);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        
        return convertToDto(savedUser);
    }


}
