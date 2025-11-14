package com.cmtc.cms.serviceImpl;

import com.cmtc.cms.model.User; // Assuming your User entity is here
import com.cmtc.cms.repository.UserRepository; // Assuming your UserRepository is here
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import this

import java.util.Collections; // Import this for single authority

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,DisabledException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        if (!user.isEnabled()) {
            throw new DisabledException("User disabled. Please contact admin.");
        }
        // CRITICAL CHANGE HERE: Add "ROLE_" prefix to the authority
        // If your User entity's getRole() returns "PORTAL_ADMIN", it will become "ROLE_PORTAL_ADMIN"
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, // Pass enabled status
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}

