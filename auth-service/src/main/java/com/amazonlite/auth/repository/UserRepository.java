package com.amazonlite.auth.repository;

import com.amazonlite.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Crucial for Spring Security authentication during login
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}