package com.cmtc.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cmtc.cms.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByIsActiveTrue();
    List<User> findByIsActiveTrueOrderByCreatedAtDesc();
    long countByIsActiveTrue();
    boolean existsByMobileNo(String mobileNo);

    
}