package com.cmtc.cms.controller;

import com.cmtc.cms.dto.user.UserCreateRequest;
import com.cmtc.cms.dto.user.UserResponse;
import com.cmtc.cms.dto.user.UserUpdateRequest;
import com.cmtc.cms.exception.DuplicateFieldException;

import com.cmtc.cms.model.User;
import com.cmtc.cms.repository.UserRepository;
import com.cmtc.cms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    @Autowired
    private UserService userService;
    
   
   @Autowired
   UserRepository userRepository;

   
   @PostMapping
   @PreAuthorize("permitAll()")
   public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
       try {
           UserResponse newUser = userService.createUser(request);
           return new ResponseEntity<>(newUser, HttpStatus.CREATED);
       } catch (DuplicateFieldException ex) {
           // custom validation messages from service (clear to user)
           return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("error", ex.getMessage()));
       } catch (Exception ex) {
           // unexpected errors (DB, server, etc.)
           return ResponseEntity
                   .status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(Map.of("error", "Something went wrong. Please try again later."));
       }
   }


    // ✅ PROTECTED ROUTES — NEED TOKEN WITH PORTAL_ADMIN
    // Changed from hasAuthority to hasRole for consistency with ROLE_ prefix
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('PORTAL_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('PORTAL_ADMIN')")
    public ResponseEntity<Void> enableUser(@PathVariable Long id) {
        userService.toggleUserStatus(id, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('PORTAL_ADMIN')")
    public ResponseEntity<Void> disableUser(@PathVariable Long id) {
        userService.toggleUserStatus(id, false);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PORTAL_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(user);
    }
   

}

