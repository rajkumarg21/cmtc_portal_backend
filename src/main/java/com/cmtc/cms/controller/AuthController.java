package com.cmtc.cms.controller;
import com.cmtc.cms.dto.user.AuthResponse;
import com.cmtc.cms.dto.user.ForgotPasswordRequest;
import com.cmtc.cms.dto.user.LoginRequest;
import com.cmtc.cms.dto.user.ResetPasswordRequest;
import com.cmtc.cms.model.User;
import com.cmtc.cms.repository.UserRepository;
import com.cmtc.cms.security.JwtUtil;
import com.cmtc.cms.service.CaptchaService;
import com.cmtc.cms.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/*Author : Rajkumar Saad
 * Date :
 * 
*/
import java.time.LocalDateTime;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository; // Still needed for direct user update for lastLoginAt

    @Autowired
    private UserService userService; // Using the interface

    @Autowired
    private CaptchaService captchaService; // Using the interface
  
    
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user); // Update last login time

            String jwt = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            return ResponseEntity.ok(new AuthResponse(jwt, user.getUsername(), user.getRole().name()));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        } catch (Exception ex) {
            System.err.println("Authentication error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication failed: " + ex.getMessage());
        }
   }
     
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        if (!captchaService.verifyCaptcha(request.getRecaptchaToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("reCAPTCHA verification failed. Please try again.");
        }

        try {
            userService.createPasswordResetTokenForUser(request.getEmail());
            return ResponseEntity.ok("Password reset link sent to your email if an account exists.");
        } catch (UsernameNotFoundException | com.cmtc.cms.exception.ResourceNotFoundException ex) {
            return ResponseEntity.ok("This account is not exists.");
        } catch (Exception ex) {
            System.err.println("Forgot password error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request. Please try again later.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
                System.err.println("Reset password error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error resetting password. Please try again later.");
        }
    }
}